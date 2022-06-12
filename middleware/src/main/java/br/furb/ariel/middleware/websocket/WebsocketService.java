package br.furb.ariel.middleware.websocket;

import br.furb.ariel.middleware.broker.Consumer;
import br.furb.ariel.middleware.client.ClientService;
import br.furb.ariel.middleware.exception.MiddlewareException;
import br.furb.ariel.middleware.message.dto.MessageDTO;
import br.furb.ariel.middleware.metrics.model.NewConnectionMetric;
import br.furb.ariel.middleware.metrics.model.ReceivedWebSocketMessageMetric;
import br.furb.ariel.middleware.metrics.model.SendWebSocketMessageMetric;
import br.furb.ariel.middleware.metrics.service.MetricsService;
import br.furb.ariel.middleware.service.service.ServiceService;
import br.furb.ariel.middleware.websocket.dto.WebsocketErrorEvent;
import br.furb.ariel.middleware.websocket.dto.WebsocketMessageEvent;
import br.furb.ariel.middleware.websocket.dto.WebsocketPongEvent;
import br.furb.ariel.middleware.websocket.dto.WebsocketSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Delivery;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import lombok.SneakyThrows;
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
    ServiceService serviceService;

    @Inject
    MetricsService metricsService;

    @Inject
    ObjectMapper mapper;

    @ConsumeEvent(value = "websocket-onopen", blocking = true)
    public Uni<Void> onOpen(String sessionId) throws IOException, InterruptedException, TimeoutException {
        this.logger.info("New Session " + sessionId);
        this.metricsService.publish(new NewConnectionMetric());

        Session session = getSession(sessionId);
        String clientId = getId(session);

        try {
            if (clientId != null) {
                WebsocketSession websocketSession = new WebsocketSession(session, clientId);
                this.websocketSessionById.put(sessionId, websocketSession);
                this.clientService.register(websocketSession);
            } else {
                this.logger.warn("Session " + sessionId + " without client id");
                session.close(new CloseReason(CloseCodes.NORMAL_CLOSURE, "Missing UUID"));
            }
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
            session.close(new CloseReason(CloseCodes.UNEXPECTED_CONDITION, "Internal Error"));
        }

        return Uni.createFrom().voidItem();
    }

    @SneakyThrows
    @ConsumeEvent(value = "websocket-onmessage", blocking = true)
    public Uni<Void> onMessage(WebsocketMessageEvent event) {
        String message = event.getMessage();

        this.metricsService.publish(new ReceivedWebSocketMessageMetric(message.length()));

        WebsocketSession websocketSession = getWebsocketSession(event.getId());
        try {
            this.clientService.updateRegistration(websocketSession);
        } catch (Exception e) {
            this.logger.error("Failure to update client registration: " + e.getMessage());
        }

        MessageDTO messageDTO = null;
        try {
            messageDTO = this.mapper.readValue(message, MessageDTO.class);
            this.serviceService.processClientMessage(websocketSession.getClientId(), messageDTO);

            MessageDTO response = MessageDTO.ok(messageDTO.getId()).build();
            send(websocketSession, this.mapper.writeValueAsString(response));
        } catch (JsonProcessingException e) {
            this.logger.error("Failure to desserialize JSON: " + e.getMessage() + " - " + message);
            MessageDTO response = MessageDTO.error(null, e.getMessage()).build();
            send(websocketSession, this.mapper.writeValueAsString(response));

        } catch (MiddlewareException e) {
            MessageDTO response = MessageDTO.error(messageDTO.getId(), e.getMessage()).build();
            send(websocketSession, this.mapper.writeValueAsString(response));

        } catch (Exception e) {
            this.logger.error("Failure to send message to service: " + e.getMessage(), e);
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

    public int getConnectionsAmount() {
        return this.websocketSessionById.size();
    }

    public void deregisterClients() {
        Set<WebsocketSession> websocketSessions = Set.copyOf(this.websocketSessionById.values());
        websocketSessions.parallelStream().forEach(websocketSession -> this.clientService.deregister(websocketSession));
    }

    public void send(String clientId, String message) throws IOException {
        Optional<WebsocketSession> optional = this.websocketSessionById.values() //
                .stream() //
                .filter(websocketSession -> Objects.equals(clientId, websocketSession.getClientId())) //
                .findFirst();
        if (optional.isPresent()) {
            send(optional.get(), message);
        }
    }

    public void send(WebsocketSession websocketSession, String message) throws IOException {
        this.metricsService.publish(new SendWebSocketMessageMetric(message.length()));

        Session session = websocketSession.getSession();
        if (!session.isOpen()) {
            this.logger.error("Trying to send a message to a closed websocket session. ClientId: " + websocketSession.getClientId());
            return;
        }
        session.getBasicRemote().sendText(message);
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
            try {
                send(clientId, new String(message.getBody()));
            } catch (Exception e) {
                this.logger.error(e.getMessage(), e);
            }
        }

        private String getClientId(Delivery message) {
            BasicProperties properties = message.getProperties();
            Map<String, Object> headers = properties.getHeaders();
            return String.valueOf(headers.get("clientId"));
        }
    }
}
