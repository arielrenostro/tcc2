package br.furb.ariel.middleware.websocket;

import br.furb.ariel.middleware.websocket.dto.WebsocketErrorEvent;
import br.furb.ariel.middleware.websocket.dto.WebsocketMessageEvent;
import br.furb.ariel.middleware.websocket.dto.WebsocketPongEvent;
import io.vertx.core.eventbus.EventBus;
import org.quartz.SchedulerException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.PongMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/websocket")
@ApplicationScoped
public class WebsocketResource {

    @Inject
    EventBus bus;

    @Inject
    WebsocketService service;

    @OnOpen
    public void onOpen(Session session) {
        this.service.putSession(session);
        this.bus.send("websocket-onopen", session.getId());
    }

    @OnClose
    public void onClose(Session session) {
        this.bus.send("websocket-onclose", session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        this.bus.send("websocket-onerror", new WebsocketErrorEvent(session.getId(), throwable));
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        this.bus.send("websocket-onmessage", new WebsocketMessageEvent(session.getId(), message));
    }

    @OnMessage
    public void onPongMessage(Session session, PongMessage pongMessage) {
        this.bus.send("websocket-onpong", new WebsocketPongEvent(session.getId(), pongMessage));
    }
}
