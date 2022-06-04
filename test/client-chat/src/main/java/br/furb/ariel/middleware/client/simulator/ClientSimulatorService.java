package br.furb.ariel.middleware.client.simulator;

import br.furb.ariel.middleware.client.utils.RandomUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

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
        for (int i = 0; i < clients; i++) {
            String uuid = null;
            while (uuid == null || this.clientIds.contains(uuid)) {
                uuid = UUID.randomUUID().toString();
            }

            Simulator simulator = new Simulator(uuid, millisBetweenMessages);
            simulator.start();

            this.tasks.add(simulator);
        }
        this.running = true;
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
