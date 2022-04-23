package br.furb.ariel.middleware.service.service;

import br.furb.ariel.middleware.broker.Broker;
import br.furb.ariel.middleware.broker.Consumer;
import br.furb.ariel.middleware.client.ClientService;
import br.furb.ariel.middleware.exception.MiddlewareException;
import br.furb.ariel.middleware.exception.ServiceNotFoundException;
import br.furb.ariel.middleware.message.dto.MessageDTO;
import br.furb.ariel.middleware.message.dto.ToMiddlewareMessageDTO;
import br.furb.ariel.middleware.message.model.Destination;
import br.furb.ariel.middleware.message.model.DestinationType;
import br.furb.ariel.middleware.message.model.Message;
import br.furb.ariel.middleware.message.service.MessageService;
import br.furb.ariel.middleware.service.dto.ServiceNotificationDTO;
import br.furb.ariel.middleware.service.model.Service;
import br.furb.ariel.middleware.service.repository.ServiceRepository;
import br.furb.ariel.middleware.websocket.WebsocketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Delivery;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

@Singleton
public class ServiceService {

    private static final String MIDDLEWARE_ROUTE = "middleware";

    @Inject
    ServiceRepository repository;

    @Inject
    MessageService messageService;

    @Inject
    ClientService clientService;

    @Inject
    Broker broker;

    @Inject
    ObjectMapper mapper;

    @Inject
    Logger logger;

    public void handleNotification(String serviceId, byte[] message) {
        ServiceNotificationDTO notification;
        try {
            notification = this.mapper.readValue(message, ServiceNotificationDTO.class);
        } catch (Exception e) {
            this.logger.error("Failure to desserialize JSON: " + e.getMessage() + " - " + new String(message));
            return;
        }

        try {
            switch (notification.getType()) {
            case GET_PENDING_MESSAGES:
                sendPendingMessages(serviceId);
                break;
            case CONFIRM_MESSAGE:
                confirmMessage(serviceId, notification.getAnswerId());
                break;
            case SEND_MESSAGE:
                sendMessageToClient(serviceId, notification);
                break;
            default:
                throw new RuntimeException("Unimplemented notification type " + notification.getType());
            }
        } catch (MiddlewareException e) {
            this.logger.error(e.getMessage());
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
        }
    }

    private void sendPendingMessages(String serviceId) throws IOException, InterruptedException, TimeoutException {
        this.logger.info("Sending pending messages to service " + serviceId);

        Service service = this.repository.findById(serviceId);
        if (service == null) {
            this.logger.warn("Service " + serviceId + " not found");
            return;
        }
        List<Message> messages = this.messageService.getPendingMessagesByService(serviceId);
        for (Message message : messages) {
            send(service, message);
        }
    }

    private void confirmMessage(String serviceId, String answerId) throws MiddlewareException {
        this.logger.info("Confirming message " + answerId + " from service " + serviceId);
        this.messageService.confirmMessage(answerId);
    }

    private void sendMessageToClient(String serviceId, ServiceNotificationDTO notification) throws IOException, InterruptedException, TimeoutException, MiddlewareException {
        validateMessage(serviceId, notification);

        String clientId = notification.getClientId();
        this.logger.info("New message from " + serviceId + " to " + clientId);

        MessageDTO messageDTO = MessageDTO.from(notification).build();
        this.clientService.sendNewMessage(serviceId, clientId, messageDTO);
    }

    private void send(Service service, Message message) throws IOException, InterruptedException, TimeoutException {
        Destination destination = message.getDestination();
        if (!Objects.equals(DestinationType.SERVICE, destination.getType())) {
            return;
        }

        MessageDTO dto = MessageDTO.from(message).build();
        switch (service.getSendType()) {
        case QUEUE:
            this.broker.publishExchange(service.getExchangeName(), service.getRouteKey(), null, this.mapper.writeValueAsBytes(dto));
            break;
        case EXCHANGE:
            this.broker.publishQueue(service.getExchangeName(), null, this.mapper.writeValueAsBytes(dto));
            break;
        default:
            throw new RuntimeException("Unimplemented service send type " + service.getSendType());
        }
    }

    public void processClientMessage(String clientId, MessageDTO messageDTO) throws IOException, InterruptedException, TimeoutException, MiddlewareException {
        validateMessage(messageDTO);

        String route = messageDTO.getRoute();
        if (Objects.equals(MIDDLEWARE_ROUTE, route)) {
            processMiddlewareMessage(messageDTO);
        } else {
            processServiceMessage(clientId, messageDTO, route);
        }
    }

    private void processServiceMessage(String clientId, MessageDTO messageDTO, String route) throws IOException, InterruptedException, TimeoutException, ServiceNotFoundException {
        Service service = this.repository.findByRoute(route);
        if (service == null) {
            throw new ServiceNotFoundException(route);
        }

        Message message = this.messageService.persistNewClientMessage(clientId, service.getId(), messageDTO);
        send(service, message);
    }

    private void processMiddlewareMessage(MessageDTO messageDTO) throws MiddlewareException {
        ToMiddlewareMessageDTO toMiddleware = this.mapper.convertValue(messageDTO.getData(), ToMiddlewareMessageDTO.class);
        switch (toMiddleware.getType()) {
        case CONFIRM_MESSAGE:
            this.messageService.confirmMessage(messageDTO.getAnswerId());
            break;
        default:
            throw new RuntimeException("Unimplemented ToMiddleware message type " + toMiddleware.getType());
        }
    }

    private void validateMessage(MessageDTO messageDTO) throws MiddlewareException {
        if (messageDTO.getId() == null) {
            throw new MiddlewareException("Message without ID");
        }
        if (messageDTO.getTtl() == null) {
            throw new MiddlewareException("Message without TTL");
        }
        if (messageDTO.getData().isEmpty()) {
            throw new MiddlewareException("Message without Data");
        }
    }

    private void validateMessage(String serviceId, ServiceNotificationDTO notificationDTO) throws MiddlewareException {
        if (notificationDTO.getId() == null) {
            throw new MiddlewareException("Notification from " + serviceId + " without ID");
        }
        if (notificationDTO.getTtl() == null) {
            throw new MiddlewareException("Notification from " + serviceId + " without TTL");
        }
        if (notificationDTO.getData().isEmpty()) {
            throw new MiddlewareException("Notification from " + serviceId + " without Data");
        }
        if (notificationDTO.getClientId() == null) {
            throw new MiddlewareException("Notification from " + serviceId + " without ClientId");
        }
    }

    public NotificationMessageConsumer newConsumer() {
        return new NotificationMessageConsumer();
    }

    public class NotificationMessageConsumer implements Consumer.Handler {

        private final Logger logger = Logger.getLogger(WebsocketService.SendMessageConsumer.class);

        @Override
        public void run(Delivery message) {
            String serviceId = getServiceId(message);
            this.logger.info("Received a notification from service " + serviceId);
            handleNotification(serviceId, message.getBody());
        }

        private String getServiceId(Delivery message) {
            BasicProperties properties = message.getProperties();
            Map<String, Object> headers = properties.getHeaders();
            return String.valueOf(headers.get("serviceId"));
        }
    }
}
