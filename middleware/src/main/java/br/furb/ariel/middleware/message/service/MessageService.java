package br.furb.ariel.middleware.message.service;

import br.furb.ariel.middleware.exception.MiddlewareException;
import br.furb.ariel.middleware.message.dto.MessageDTO;
import br.furb.ariel.middleware.message.model.Destination;
import br.furb.ariel.middleware.message.model.DestinationType;
import br.furb.ariel.middleware.message.model.Message;
import br.furb.ariel.middleware.message.model.MessageStatus;
import br.furb.ariel.middleware.message.repository.MessageRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Singleton
public class MessageService {

    @Inject
    MessageRepository repository;

    public List<Message> getPendingMessagesByClient(String clientId) {
        return this.repository.findPendingMessagesByClient(clientId);
    }

    public List<Message> getPendingMessagesByService(String serviceId) {
        return this.repository.findPendingMessagesByService(serviceId);
    }

    public Message persistNewClientMessage(String clientId, String serviceId, MessageDTO messageDTO) {
        Message message = createMessage(messageDTO);

        message.setOrigin(new Destination());
        message.getOrigin().setId(clientId);
        message.getOrigin().setType(DestinationType.CLIENT);

        message.setDestination(new Destination());
        message.getDestination().setId(serviceId);
        message.getDestination().setType(DestinationType.SERVICE);

        this.repository.save(message);

        return message;
    }

    public Message persistNewServiceMessage(String serviceId, String clientId, MessageDTO messageDTO) {
        Message message = createMessage(messageDTO);

        message.setOrigin(new Destination());
        message.getOrigin().setId(serviceId);
        message.getOrigin().setType(DestinationType.SERVICE);

        message.setDestination(new Destination());
        message.getDestination().setId(clientId);
        message.getDestination().setType(DestinationType.CLIENT);

        this.repository.save(message);

        return message;
    }

    private Message createMessage(MessageDTO messageDTO) {
        String id = messageDTO.getId();
        String answerId = messageDTO.getAnswerId();
        Date ttl = messageDTO.getTtl();
        Map<String, Object> data = messageDTO.getData();

        Message message = new Message();
        message.setId(id);
        message.setAnswerId(answerId);
        message.setStatus(MessageStatus.PENDING);
        message.setCreatedAt(new Date());
        message.setExpiresIn(ttl);
        message.setData(data);
        return message;
    }

    public void confirmMessage(String messageId) throws MiddlewareException {
        Message message = this.repository.findById(messageId);
        if (message == null) {
            throw new MiddlewareException("Message " + messageId + " not found");
        }
        message.setStatus(MessageStatus.DELIVERED);
        this.repository.save(message);
    }
}
