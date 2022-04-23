package br.furb.ariel.middleware.client;

import br.furb.ariel.middleware.broker.Broker;
import br.furb.ariel.middleware.message.dto.MessageDTO;
import br.furb.ariel.middleware.message.model.Destination;
import br.furb.ariel.middleware.message.model.DestinationType;
import br.furb.ariel.middleware.message.model.Message;
import br.furb.ariel.middleware.message.service.MessageService;
import br.furb.ariel.middleware.websocket.dto.WebsocketSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.websocket.Session;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

@Singleton
public class ClientService {

    @Inject
    Broker broker;

    @Inject
    ObjectMapper mapper;

    @Inject
    Logger logger;

    @Inject
    ClientCacheService clientCacheService;

    @Inject
    MessageService messageService;

    public void register(WebsocketSession websocketSession) throws IOException, InterruptedException, TimeoutException {
        Session session = websocketSession.getSession();
        String clientId = websocketSession.getClientId();

        this.logger.info("Session " + session.getId() + " is registered as client " + clientId);

        this.clientCacheService.register(clientId);
        String containerId = this.clientCacheService.findContainerClient(clientId);
        if (containerId == null) {
            this.logger.warn("Client " + clientId + " is not connected to any instance");
            return;
        }
        sendPendingMessages(containerId, clientId);
    }

    public void deregister(WebsocketSession websocketSession) {
        Session session = websocketSession.getSession();
        String clientId = websocketSession.getClientId();
        this.logger.info("Session " + session.getId() + " and client " + clientId + " are deregistered");
        this.clientCacheService.deregister(clientId);
    }

    public void updateRegistration(WebsocketSession websocketSession) {
        String clientId = websocketSession.getClientId();
        this.clientCacheService.register(clientId);
    }

    public void sendPendingMessages(String containerId, String clientId) throws IOException, InterruptedException, TimeoutException {
        List<Message> messages = this.messageService.getPendingMessagesByClient(clientId);
        this.logger.info("Found " + messages.size() + " pending messages for client " + clientId);
        for (Message message : messages) {
            publishMessage(containerId, message);
        }
    }

    public void sendNewMessage(String serviceId, String clientId, MessageDTO messageDTO) throws IOException, InterruptedException, TimeoutException {
        Message message = this.messageService.persistNewServiceMessage(serviceId, clientId, messageDTO);
        String containerId = this.clientCacheService.findContainerClient(clientId);
        if (containerId == null) {
            this.logger.warn("Client " + clientId + " is not connected to any instance");
            return;
        }
        publishMessage(containerId, message);
    }

    private void publishMessage(String containerId, Message message) throws IOException, InterruptedException, TimeoutException {
        Destination destination = message.getDestination();
        if (!Objects.equals(DestinationType.CLIENT, destination.getType())) {
            return;
        }

        String clientId = destination.getId();
        if (containerId == null) {
            this.logger.warn("Client " + clientId + " is not connected to any instance");
            return;
        }

        Map<String, Object> headers = Map.of("clientId", clientId);
        MessageDTO dto = MessageDTO.from(message).build();
        this.broker.publishExchange("middleware.to-send", containerId, headers, this.mapper.writeValueAsBytes(dto));
    }
}
