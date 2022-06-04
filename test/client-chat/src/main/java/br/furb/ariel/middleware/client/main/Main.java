package br.furb.ariel.middleware.client.main;

import br.furb.ariel.middleware.client.broker.Broker;
import br.furb.ariel.middleware.client.config.Config;
import br.furb.ariel.middleware.client.dto.Task;
import br.furb.ariel.middleware.client.dto.Task.Rate;
import br.furb.ariel.middleware.client.simulator.ClientSimulatorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Delivery;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class Main {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Broker broker = new Broker();
    private final String id = UUID.randomUUID().toString();

    public Main() throws IOException, InterruptedException, TimeoutException {
        System.out.println("Starting Chat Client - " + this.id);

        this.broker.createBroadcastExchange(Config.RABBITMQ_BROADCAST_EXCHANGE, true);
        this.broker.consumeExchange(Config.RABBITMQ_BROADCAST_EXCHANGE, "", Config.RABBITMQ_BROADCAST_EXCHANGE + "." + this.id, this::handleBroadcast);
    }

    private void handleBroadcast(Delivery message) {
        try {
            Task task = this.mapper.readValue(message.getBody(), Task.class);

            synchronized (ClientSimulatorService.getInstance()) {
                int clients = task.getClients();
                Rate rate = task.getRate();

                if (clients == 0) {
                    System.out.println("Stopping");
                    ClientSimulatorService.getInstance().stop();
                } else {
                    System.out.println("Starting with " + clients + " clients with " + rate + " rate");
                    ClientSimulatorService.getInstance().start(clients, rate.getTimeRate().toMillis());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

            System.out.println("Stopping");
            ClientSimulatorService.getInstance().stop();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException, TimeoutException {
        new Main();
    }
}

