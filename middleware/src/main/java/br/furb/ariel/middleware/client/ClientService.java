package br.furb.ariel.middleware.client;

import br.furb.ariel.middleware.message.MessageService;
import br.furb.ariel.middleware.websocket.dto.WebsocketSession;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.websocket.Session;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Singleton
public class ClientService {

    @Inject
    Logger logger;

    @Inject
    ClientCacheService clientCacheService;

    @Inject
    MessageService messageService;

    public void register(WebsocketSession websocketSession) throws IOException, InterruptedException, TimeoutException {
        Session session = websocketSession.getSession();
        String clientId = websocketSession.getClientId();

        this.logger.info("Session " + session.getId() + " is registered as client " + clientId);

        this.clientCacheService.register(clientId);
        String containerId = this.clientCacheService.findContainerClient(clientId);
        this.messageService.sendPendingClientMessages(containerId, clientId);
    }

    public void deregister(WebsocketSession websocketSession) {
        Session session = websocketSession.getSession();
        String clientId = websocketSession.getClientId();
        this.logger.info("Session " + session.getId() + " and client " + clientId + " are deregistered");
        this.clientCacheService.deregister(clientId);
    }

    public void updateRegistration(WebsocketSession websocketSession) {
        String clientId = websocketSession.getClientId();
        this.clientCacheService.register(clientId);
    }
}
