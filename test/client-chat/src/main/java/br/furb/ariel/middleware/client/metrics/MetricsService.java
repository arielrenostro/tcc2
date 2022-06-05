package br.furb.ariel.middleware.client.metrics;

import br.furb.ariel.middleware.client.utils.NamedThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MetricsService {

    private static final MetricsService INSTANCE = new MetricsService();

    private final ExecutorService executor = Executors.newFixedThreadPool(2, new NamedThreadFactory("metrics-"));
    private final MetricsRepository repository = new MetricsRepository();

    private MetricsService() {
    }

    public static MetricsService getInstance() {
        return INSTANCE;
    }

    public void publish(Metric metric) {
        this.executor.submit(() -> this.innerPublish(metric));
    }

    private void innerPublish(Metric metric) {
        try {
            this.repository.publish(metric);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
