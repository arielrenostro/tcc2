package br.furb.ariel.middleware.container;

import br.furb.ariel.middleware.broker.Broker;
import br.furb.ariel.middleware.config.Config;
import br.furb.ariel.middleware.websocket.WebsocketService;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@ApplicationScoped
public class ContainerService {

    private final String id = UUID.randomUUID().toString();

    @Inject
    Logger logger;

    @Inject
    Broker broker;

    @Inject
    WebsocketService websocketService;

    public void onStart(@Observes StartupEvent event) throws IOException, TimeoutException, InterruptedException {
        this.logger.info("Starting container with id: " + id);

        String exchange = Config.RABBITMQ_EXCHANGE_TO_SEND;
        this.broker.createRoutedExchange(exchange, true);

        String queueName = Config.RABBITMQ_EXCHANGE_TO_SEND + "." + this.id;
        this.broker.consumeExchange(exchange, this.id, queueName, this.websocketService.newConsumer());
    }

    public void onStop(@Observes ShutdownEvent event) {
        this.websocketService.deregisterClients();
    }

    public String getId() {
        return id;
    }
}
