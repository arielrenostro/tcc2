package br.furb.ariel.middleware.websocket.dto;

import lombok.Data;

@Data
public class WebsocketMessageEvent {

    private final String id;
    private final String message;

}
