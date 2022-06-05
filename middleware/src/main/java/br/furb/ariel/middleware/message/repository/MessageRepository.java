package br.furb.ariel.middleware.message.repository;

import br.furb.ariel.middleware.core.BaseRepository;
import br.furb.ariel.middleware.message.model.DestinationType;
import br.furb.ariel.middleware.message.model.Message;
import br.furb.ariel.middleware.message.model.MessageStatus;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import io.quarkus.panache.common.Parameters;
import org.bson.Document;

import javax.inject.Singleton;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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

    public long confirmMessageById(String messageId) {
        return this.update( //
                        "status = ?1, deliveredAt = ?2", //
                        MessageStatus.DELIVERED, new Date() //
                ) //
                .where("messageId", messageId);
    }

    public void buildCollection() {
        MongoCollection<Message> collection = this.mongoCollection();
        for (Document index : collection.listIndexes()) {
            String name = String.valueOf(index.get("name"));
            if (Objects.equals("messages_messageId", name)) {
                return;
            }
        }
        collection.createIndex( //
                Indexes.ascending("messageId"), //
                new IndexOptions().unique(true).name("messages_messageId") //
        );
    }
}
