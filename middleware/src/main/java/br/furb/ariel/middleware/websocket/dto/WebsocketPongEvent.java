package br.furb.ariel.middleware.websocket.dto;

import lombok.Data;

import javax.websocket.PongMessage;

@Data
public class WebsocketPongEvent {

    private final String id;
    private final PongMessage pongMessage;

}
