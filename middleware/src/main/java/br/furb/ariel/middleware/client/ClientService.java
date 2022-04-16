package br.furb.ariel.middleware.client;

import br.furb.ariel.middleware.broker.Broker;
import br.furb.ariel.middleware.message.dto.MessageDTO;
import br.furb.ariel.middleware.message.MessageService;
import br.furb.ariel.middleware.message.model.Destination;
import br.furb.ariel.middleware.message.model.DestinationType;
import br.furb.ariel.middleware.message.model.Message;
import br.furb.ariel.middleware.websocket.dto.WebsocketSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.websocket.Session;
import java.util.List;
import java.util.Objects;

@Singleton
public class ClientService {

    @Inject
    Logger logger;

    @Inject
    ClientCacheService clientCacheService;

    @Inject
    MessageService messageService;

    @Inject
    Broker broker;

    @Inject
    ObjectMapper objectMapper;

    public void register(WebsocketSession websocketSession) throws JsonProcessingException {
        Session session = websocketSession.getSession();
        String clientId = websocketSession.getClientId();
        this.logger.info("Session " + session.getId() + " is registered as client " + clientId);
        // TODO
        //        this.clientCacheService.register(uuid);

        List<Message> messages = this.messageService.getPendingMessagesByClient(clientId);
        for (Message message : messages) {
            publishMessage(message);
        }
    }

    public void unregister(WebsocketSession websocketSession) {
        Session session = websocketSession.getSession();
        this.logger.info("Session " + session.getId() + " are deregistered");
        // TODO
        // this.clientCacheService.unregister(websocketSession.getUuid());
    }

    private void publishMessage(Message message) throws JsonProcessingException {
        Destination destination = message.getDestination();
        switch (destination.getType()) {
        case CLIENT:
            publishClientMessage(message);
            break;
        case SERVICE:
            publishServiceMessage(message);
            break;
        default:
            throw new UnsupportedOperationException("Unimplemented type " + destination.getType());
        }
    }

    private void publishClientMessage(Message message) throws JsonProcessingException {
        Destination destination = message.getDestination();
        if (!Objects.equals(DestinationType.CLIENT, destination.getType())) {
            return;
        }

        String clientId = destination.getId();
        String containerId = this.clientCacheService.findClient(clientId);
        if (containerId == null) {
            this.logger.warn("Client " + clientId + " is not connected to any instance");
            return;
        }

        MessageDTO dto = MessageDTO.from(message).build();
        this.broker.publishExchange("middleware.to-send", containerId, this.objectMapper.writeValueAsString(dto));
    }

    private void sendOk(Session session, String messageId) throws JsonProcessingException {
        MessageDTO message = MessageDTO.ok(messageId).build();
        sendMessage(session, message);
    }

    private void sendMessage(Session session, MessageDTO message) throws JsonProcessingException {
        String json = this.objectMapper.writeValueAsString(message);
        session.getAsyncRemote().sendText(json);
    }
}
