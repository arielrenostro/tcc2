package br.furb.ariel.middleware.metrics.repository;

import br.furb.ariel.middleware.config.Config;
import br.furb.ariel.middleware.container.ContainerService;
import br.furb.ariel.middleware.metrics.model.Metric;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

@Singleton
public class MetricsRepository {

    private final ThreadLocal<InfluxDB> clientRef = new ThreadLocal<>();

    @Inject
    Config config;

    @Inject
    ContainerService containerService;

    public void publish(Metric metric) {
        InfluxDB client = getClient();

        Point point = Point.measurement(metric.getName()) //
                .time(metric.getDate().getTime(), TimeUnit.MILLISECONDS)
                .tag(metric.getTags()) //
                .tag("instanceId", containerService.getId()) //
                .fields(metric.getFields()) //
                .build();

        client.write(point);
    }

    private synchronized InfluxDB getClient() {
        InfluxDB influxDB = this.clientRef.get();
        if (influxDB == null) {
            influxDB = InfluxDBFactory.connect(config.getInfluxDBConnectionString());
            influxDB.createDatabase(config.getInfluxDBDatabase());
            influxDB.setDatabase(config.getInfluxDBDatabase());
            this.clientRef.set(influxDB);
        }
        return influxDB;
    }
}
