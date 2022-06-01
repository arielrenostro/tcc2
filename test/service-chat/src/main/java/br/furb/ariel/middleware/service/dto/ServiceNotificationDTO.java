package br.furb.ariel.middleware.service.dto;

import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class ServiceNotificationDTO {

    private String id;
    private String answerId;
    private ServiceNotificationType type;
    private Map<String, Object> data;
    private String clientId;
    private Date ttl;

}
