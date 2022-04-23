package br.furb.ariel.middleware.message.repository;

import br.furb.ariel.middleware.core.BaseRepository;
import br.furb.ariel.middleware.message.model.Destination;
import br.furb.ariel.middleware.message.model.DestinationType;
import br.furb.ariel.middleware.message.model.Message;

import javax.inject.Singleton;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Singleton
public class MessageRepository extends BaseRepository<Message> {

    public List<Message> findPendingMessagesByClient(String clientId) {
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

    public List<Message> findPendingMessagesByService(String serviceId) {
        // TODO
        Message message = new Message();
        message.setId(UUID.randomUUID().toString());
        message.setData(new HashMap<>());
        message.setCreatedAt(new Date());
        message.setDestination(new Destination());
        message.getDestination().setType(DestinationType.SERVICE);
        message.getDestination().setId(serviceId);
        message.setOrigin(new Destination());
        return List.of(message);
    }
}
