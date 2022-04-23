package br.furb.ariel.middleware.job;

import br.furb.ariel.middleware.config.Config;
import br.furb.ariel.middleware.websocket.WebsocketService;
import br.furb.ariel.middleware.websocket.dto.WebsocketSession;
import io.quarkus.scheduler.Scheduled;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.Session;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@ApplicationScoped
public class WebsocketPingPongJob {

    @Inject
    Logger logger;

    @Inject
    WebsocketService service;

    @Scheduled(every = Config.WEBSOCKET_CHECK_PING_JOB_INTERVAL, identity = "WebsocketPing")
    void ping() {
        Set<WebsocketSession> sessions = this.service.getSesions();
        sessions.parallelStream().forEach(websocketSession -> {
            Session session = websocketSession.getSession();
            if (session.isOpen()) {
                try {
                    if (isPongLate(websocketSession)) {
                        this.logger.info("Clossing session " + session.getId() + " because client does not respond");
                        session.close(new CloseReason(CloseCodes.NORMAL_CLOSURE, "Client does not respond"));

                    } else if (isNeedSendPing(websocketSession)) {
                        session.getBasicRemote().sendPing(ByteBuffer.allocate(0));
                        websocketSession.setLastPing(Instant.now());
                    }
                } catch (IOException e) {
                    this.logger.error(e.getMessage(), e);
                }
            }
        });
    }

    private boolean isPongLate(WebsocketSession websocketSession) {
        if (websocketSession.getLastPing() == null) {
            return false;
        }
        Duration timeElipsed = Duration.between(websocketSession.getLastPing(), Instant.now());
        if (timeElipsed.compareTo(Config.WEBSOCKET_PING_TIMEOUT) > 0) {
            if (websocketSession.getLastPong() == null) {
                return true;
            }
            Duration diff = Duration.between(websocketSession.getLastPing(), websocketSession.getLastPong());
            return diff.compareTo(Config.WEBSOCKET_PING_TIMEOUT) > 0;
        }
        return false;
    }

    private boolean isNeedSendPing(WebsocketSession websocketSession) {
        if (websocketSession.getLastPing() == null) {
            return true;
        }
        Duration timeElipsed = Duration.between(websocketSession.getLastPing(), Instant.now());
        return timeElipsed.compareTo(Config.WEBSOCKET_PING_INTERNAL) > 0;
    }
}
