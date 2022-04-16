package br.furb.ariel.middleware.message.model;

import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class Message {

    private String id;
    private String answerId;
    private MessageStatus status;
    private Destination origin;
    private Destination destination;
    private Map<String, Object> data;
    private Date createdAt;
    private Date deliveredAt;
    private Date expiresIn;

}
