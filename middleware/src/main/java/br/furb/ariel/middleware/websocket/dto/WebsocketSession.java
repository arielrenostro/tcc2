package br.furb.ariel.middleware.websocket.dto;

import lombok.Data;

import javax.websocket.Session;
import java.time.Instant;

@Data
public class WebsocketSession {

    private final Session session;
    private final String clientId;

    private Instant lastPing;
    private Instant lastPong;
    private Instant lastUpdate;

}
