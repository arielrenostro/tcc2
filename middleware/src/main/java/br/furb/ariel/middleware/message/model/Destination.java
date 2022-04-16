package br.furb.ariel.middleware.message.model;

import lombok.Data;

@Data
public class Destination {

    private DestinationType type;
    private String id;

}
