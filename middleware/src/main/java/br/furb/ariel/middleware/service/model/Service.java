package br.furb.ariel.middleware.service.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Service {

    private String id;
    private String name;
    private ServiceSendType sendType;
    private String exchangeName;
    private String routeKey;
    private List<String> messageTypes = new ArrayList<>();

}
