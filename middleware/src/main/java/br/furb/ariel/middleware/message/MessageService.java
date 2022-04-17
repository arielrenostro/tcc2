package br.furb.ariel.middleware.message;

import br.furb.ariel.middleware.broker.Broker;
import br.furb.ariel.middleware.message.dto.MessageDTO;
import br.furb.ariel.middleware.message.model.Destination;
import br.furb.ariel.middleware.message.model.DestinationType;
import br.furb.ariel.middleware.message.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Singleton
public class MessageService {

    @Inject
    Logger logger;

    @Inject
    Broker broker;

    @Inject
    ObjectMapper objectMapper;

    private List<Message> getPendingMessagesByClient(String clientId) {
        // TODO
        Message message = new Message();
        message.setId(UUID.randomUUID().toString());
        message.setData(new HashMap<>());
        message.setCreatedAt(new Date());
        message.setDestination(new Destination());
        message.getDestination().setType(DestinationType.CLIENT);
        message.getDestination().setId(clientId);
        message.setOrigin(new Destination());
        return List.of(message);
    }

    public void sendPendingClientMessages(String containerId, String clientId) throws IOException, InterruptedException, TimeoutException {
        List<Message> messages = getPendingMessagesByClient(clientId);
        this.logger.info("Found " + messages.size() + " pending messages for client " + clientId);
        for (Message message : messages) {
            publishClientMessage(containerId, message);
        }
    }

    //    private void publishMessage(Message message) throws IOException, InterruptedException, TimeoutException {
    //        Destination destination = message.getDestination();
    //        switch (destination.getType()) {
    //        case CLIENT:
    //            publishClientMessage(message);
    //            break;
    //        case SERVICE:
    //            // TODO
    //            //            publishServiceMessage(message);
    //            break;
    //        default:
    //            throw new UnsupportedOperationException("Unimplemented type " + destination.getType());
    //        }
    //    }

    private void publishClientMessage(String containerId, Message message) throws IOException, InterruptedException, TimeoutException {
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
        this.broker.publishExchange("middleware.to-send", containerId, headers, this.objectMapper.writeValueAsBytes(dto));
    }
}
