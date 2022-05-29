package br.furb.ariel.middleware.job;

import br.furb.ariel.middleware.client.ClientService;
import br.furb.ariel.middleware.config.Config;
import br.furb.ariel.middleware.metrics.model.ConnectionsMetric;
import br.furb.ariel.middleware.metrics.service.MetricsService;
import br.furb.ariel.middleware.websocket.WebsocketService;
import io.quarkus.scheduler.Scheduled;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class MetricsJob {

    @Inject
    MetricsService metricsService;

    @Inject
    WebsocketService websocketService;

    @Inject
    Logger logger;

    @Scheduled(every = Config.METRICS_JOB_INTERVAL, identity = "Metrics")
    void metrics() {
        this.logger.info("Publishing metrics");
        int connections = this.websocketService.getConnectionsAmount();
        this.metricsService.publish(new ConnectionsMetric(connections));
    }
}
