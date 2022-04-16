package br.furb.ariel.middleware.websocket.dto;

import lombok.Data;

@Data
public class WebsocketErrorEvent {

    private final String id;
    private final Throwable throwable;

}
