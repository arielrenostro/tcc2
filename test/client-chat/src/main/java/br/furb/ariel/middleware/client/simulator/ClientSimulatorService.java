package br.furb.ariel.middleware.client.simulator;

import br.furb.ariel.middleware.client.utils.RandomUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ClientSimulatorService {

    private static final ClientSimulatorService INSTANCE = new ClientSimulatorService();

    private final List<Simulator> tasks = new ArrayList<>();
    private final List<String> clientIds = new ArrayList<>();
    private final Random random = new Random();

    private boolean running;

    private ClientSimulatorService() {

    }

    public static ClientSimulatorService getInstance() {
        return INSTANCE;
    }

    public synchronized void start(int clients, long millisBetweenMessages) throws Exception {
        if (this.running) {
            this.stop();
        }

        ExecutorService executors = Executors.newFixedThreadPool(clients);
        try {
            for (int i = 0; i < clients; i++) {
                String uuid = null;
                while (uuid == null || this.clientIds.contains(uuid)) {
                    uuid = UUID.randomUUID().toString();
                }

                Simulator simulator = new Simulator(uuid, millisBetweenMessages);
                this.tasks.add(simulator);

                executors.submit(() -> {
                    try {
                        simulator.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
            this.running = true;
        } finally {
            executors.shutdown();
            executors.awaitTermination(2, TimeUnit.SECONDS);
            executors.shutdownNow();
        }
    }

    public synchronized void stop() {
        for (Simulator simulator : this.tasks) {
            simulator.stop();
        }
        this.tasks.clear();
        this.clientIds.clear();
        this.running = false;
    }

    public void registerClientId(String clientId) {
        synchronized (this.clientIds) {
            this.clientIds.add(clientId);
        }
    }

    public String peekClient(String clientId) {
        String peeked = null;
        while (peeked == null || Objects.equals(peeked, clientId)) {
            int idx = RandomUtils.range(this.random, 0, this.clientIds.size());
            peeked = this.clientIds.get(idx);
        }
        return peeked;
    }
}
