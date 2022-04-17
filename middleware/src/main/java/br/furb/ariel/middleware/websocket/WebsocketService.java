package br.furb.ariel.middleware.websocket;

import br.furb.ariel.middleware.broker.Consumer;
import br.furb.ariel.middleware.client.ClientService;
import br.furb.ariel.middleware.message.dto.MessageDTO;
import br.furb.ariel.middleware.websocket.dto.WebsocketErrorEvent;
import br.furb.ariel.middleware.websocket.dto.WebsocketMessageEvent;
import br.furb.ariel.middleware.websocket.dto.WebsocketPongEvent;
import br.furb.ariel.middleware.websocket.dto.WebsocketSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Delivery;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.Session;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

@ApplicationScoped
public class WebsocketService {

    private static final String CLIENT_ID_PARAM = "id";

    private final Map<String, Session> sessionsById = new ConcurrentHashMap<>();
    private final Map<String, WebsocketSession> websocketSessionById = new ConcurrentHashMap<>();

    @Inject
    Logger logger;

    @Inject
    ClientService clientService;

    @Inject
    ObjectMapper objectMapper;

    @ConsumeEvent(value = "websocket-onopen", blocking = true)
    public Uni<Void> onOpen(String sessionId) throws IOException, InterruptedException, TimeoutException {
        this.logger.info("New Session " + sessionId);

        Session session = getSession(sessionId);
        String clientId = getId(session);
        if (clientId != null) {
            WebsocketSession websocketSession = new WebsocketSession(session, clientId);
            this.websocketSessionById.put(sessionId, websocketSession);
            this.clientService.register(websocketSession);
        } else {
            this.logger.warn("Session " + sessionId + " without client id");
            session.close(new CloseReason(CloseCodes.NORMAL_CLOSURE, "Missing UUID"));
        }

        return Uni.createFrom().voidItem();
    }

    @ConsumeEvent(value = "websocket-onmessage", blocking = true)
    public Uni<Void> onMessage(WebsocketMessageEvent event) {
        String message = event.getMessage();
        WebsocketSession websocketSession = getWebsocketSession(event.getId());

        this.clientService.updateRegistration(websocketSession);

        try {
            MessageDTO messageDTO = this.objectMapper.readValue(message, MessageDTO.class);
            processMessage(websocketSession, messageDTO);
        } catch (IOException e) {
            this.logger.error("Failure to desserialize JSON: " + e.getMessage() + " - " + message);
        }

        return Uni.createFrom().voidItem();
    }

    @ConsumeEvent(value = "websocket-onclose", blocking = true)
    public Uni<Void> onClose(String id) {
        this.logger.info("Session " + id + " has been closed");

        this.sessionsById.remove(id);
        WebsocketSession websocketSession = this.websocketSessionById.remove(id);
        if (websocketSession != null) {
            this.clientService.deregister(websocketSession);
        }
        return Uni.createFrom().voidItem();
    }

    @ConsumeEvent(value = "websocket-onerror", blocking = true)
    public Uni<Void> onError(WebsocketErrorEvent event) {
        Session session = getSession(event.getId());
        Throwable throwable = event.getThrowable();

        this.logger.error("Error on Session " + session.getId() + ": " + throwable.getMessage(), throwable);

        return Uni.createFrom().voidItem();
    }

    @ConsumeEvent(value = "websocket-onpong", blocking = true)
    public Uni<Void> onPongMessage(WebsocketPongEvent event) {
        WebsocketSession websocketSession = getWebsocketSession(event.getId());
        websocketSession.setLastPong(Instant.now());

        this.clientService.updateRegistration(websocketSession);

        return Uni.createFrom().voidItem();
    }

    public Set<WebsocketSession> getSesions() {
        return Set.copyOf(this.websocketSessionById.values());
    }

    public void deregisterClients() {
        Set<WebsocketSession> websocketSessions = Set.copyOf(this.websocketSessionById.values());
        websocketSessions.parallelStream().forEach(websocketSession -> this.clientService.deregister(websocketSession));
    }

    public void send(String clientId, String message) {
        Optional<WebsocketSession> optional = this.websocketSessionById.values().stream().filter(websocketSession -> Objects.equals(clientId, websocketSession.getClientId())).findFirst();
        optional.ifPresent(websocketSession -> send(websocketSession, message));
    }

    public void send(WebsocketSession websocketSession, String message) {
        Session session = websocketSession.getSession();
        session.getAsyncRemote().sendText(message);
    }

    public void putSession(Session session) {
        this.sessionsById.put(session.getId(), session);
    }

    public Session getSession(String id) {
        Session session = this.sessionsById.get(id);
        if (session == null) {
            throw new RuntimeException("Client already disconnected");
        }
        return session;
    }

    public WebsocketSession getWebsocketSession(String id) {
        WebsocketSession session = this.websocketSessionById.get(id);
        if (session == null) {
            throw new RuntimeException("Client already disconnected");
        }
        return session;
    }

    public SendMessageConsumer newConsumer() {
        return new SendMessageConsumer();
    }

    private void processMessage(WebsocketSession session, MessageDTO messageDTO) {
        try {
            // TODO
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
        }
    }

    private String getId(Session session) {
        Map<String, List<String>> requestParameterMap = session.getRequestParameterMap();
        if (requestParameterMap != null) {
            List<String> idParameters = requestParameterMap.get(CLIENT_ID_PARAM);
            if (idParameters != null && !idParameters.isEmpty()) {
                return idParameters.iterator().next();
            }
        }
        return null;
    }

    public class SendMessageConsumer implements Consumer.Handler {

        private final Logger logger = Logger.getLogger(SendMessageConsumer.class);

        @Override
        public void run(Delivery message) {
            String clientId = getClientId(message);
            this.logger.info("Sending message to client " + clientId);
            send(clientId, new String(message.getBody()));
        }

        private String getClientId(Delivery message) {
            BasicProperties properties = message.getProperties();
            Map<String, Object> headers = properties.getHeaders();
            return String.valueOf(headers.get("clientId"));
        }
    }
}
