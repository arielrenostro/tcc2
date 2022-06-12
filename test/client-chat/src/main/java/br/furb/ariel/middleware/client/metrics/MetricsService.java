package br.furb.ariel.middleware.client.metrics;

public class MetricsService {

    private static final MetricsService INSTANCE = new MetricsService();

    private final MetricsRepository repository = new MetricsRepository();

    private MetricsService() {
    }

    public static MetricsService getInstance() {
        return INSTANCE;
    }

    public void publish(Metric metric) {
        this.repository.publish(metric);
    }
}
