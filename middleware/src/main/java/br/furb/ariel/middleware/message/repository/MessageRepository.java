package br.furb.ariel.middleware.message.repository;

import br.furb.ariel.middleware.core.BaseRepository;
import br.furb.ariel.middleware.message.model.DestinationType;
import br.furb.ariel.middleware.message.model.Message;
import io.quarkus.panache.common.Parameters;

import javax.inject.Singleton;
import java.util.Date;
import java.util.List;

@Singleton
public class MessageRepository extends BaseRepository<Message> {

    public List<Message> findPendingMessagesByClient(String clientId) {
        return this.list( //
                "{'destination.type': :type, 'destination._id': :id, 'expiresIn': {$gt: :ttl}}", //
                Parameters //
                        .with("type", DestinationType.CLIENT) //
                        .and("id", clientId) //
                        .and("ttl", new Date()) //
        );
    }

    public List<Message> findPendingMessagesByService(String serviceId) {
        return this.list( //
                "{'destination.type': :type, 'destination._id': :id, 'expiresIn': {$gt: :ttl}}", //
                Parameters //
                        .with("type", DestinationType.SERVICE) //
                        .and("id", serviceId) //
                        .and("ttl", new Date()) //
        );
    }

    public Message findByMessageId(String id) {
        return this.find("messageId", id).firstResult();
    }
}
