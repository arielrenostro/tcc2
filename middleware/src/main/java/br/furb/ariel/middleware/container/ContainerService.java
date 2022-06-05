package br.furb.ariel.middleware.container;

import br.furb.ariel.middleware.broker.Broker;
import br.furb.ariel.middleware.config.Config;
import br.furb.ariel.middleware.message.service.MessageService;
import br.furb.ariel.middleware.service.service.ServiceService;
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

    @Inject
    ServiceService serviceService;

    @Inject
    MessageService messageService;

    @Inject
    Config config;

    public void onStart(@Observes StartupEvent event) throws IOException, TimeoutException, InterruptedException {
        this.logger.info("Starting container with id: " + id);

        this.messageService.buildCollection();

        consumeToSend();
        consumeNotification();
    }

    public void onStop(@Observes ShutdownEvent event) {
        this.websocketService.deregisterClients();
    }

    private void consumeToSend() throws IOException, InterruptedException, TimeoutException {
        String exchange = Config.RABBITMQ_EXCHANGE_TO_SEND;
        this.broker.createRoutedExchange(exchange, true);

        String queueName = exchange + "." + this.id;
        for (int i = 0; i < this.config.getConsumersToSend(); i++) {
            this.logger.info("Consuming queue " + queueName + " " + i);
            this.broker.consumeExchange(exchange, this.id, queueName, this.websocketService.newConsumer());
        }
    }

    private void consumeNotification() throws IOException, InterruptedException, TimeoutException {
        this.broker.createRoutedExchange(Config.RABBITMQ_EXCHANGE_NOTIFICATION, true);
        this.broker.queueBind(Config.RABBITMQ_EXCHANGE_NOTIFICATION, "", Config.RABBITMQ_EXCHANGE_NOTIFICATION);
        //        String queueName = Config.RABBITMQ_EXCHANGE_NOTIFICATION + "." + this.id;
        //        this.broker.consumeExchange(Config.RABBITMQ_EXCHANGE_NOTIFICATION, "", queueName, this.serviceService.newConsumer());

        this.broker.createQueue(Config.RABBITMQ_EXCHANGE_NOTIFICATION, true);
        for (int i = 0; i < this.config.getConsumersNotification(); i++) {
            this.logger.info("Consuming queue " + Config.RABBITMQ_EXCHANGE_NOTIFICATION + " " + i);
            this.broker.consumeQueue(Config.RABBITMQ_EXCHANGE_NOTIFICATION, this.serviceService.newConsumer());
        }
    }

    public String getId() {
        return id;
    }
}
