package br.furb.ariel.middleware.message.model;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Data;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.Map;

@Data
@MongoEntity(collection = "messages")
public class Message {

    private ObjectId id;
    private String messageId;
    private String answerId;
    private String route;
    private MessageStatus status;
    private Destination origin;
    private Destination destination;
    private Map<String, Object> data;
    private Date createdAt;
    private Date deliveredAt;
    private Date expiresIn;

}
