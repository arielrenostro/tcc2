package br.furb.ariel.middleware.client.metrics;

import br.furb.ariel.middleware.client.config.Config;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;

import java.util.concurrent.TimeUnit;

public class MetricsRepository {

    private final ThreadLocal<InfluxDB> clientRef = new ThreadLocal<>();

    public void publish(Metric metric) {
        InfluxDB client = getClient();

        Point point = Point.measurement(metric.getName()) //
                .time(metric.getDate().getTime(), TimeUnit.MILLISECONDS).tag(metric.getTags()) //
                .fields(metric.getFields()) //
                .build();

        client.write(point);
    }

    private synchronized InfluxDB getClient() {
        InfluxDB influxDB = this.clientRef.get();
        if (influxDB == null) {
            influxDB = InfluxDBFactory.connect(Config.INFLUXDB_CONNECTION_STRING);
            influxDB.createDatabase(Config.INFLUXDB_DATABASE);
            influxDB.setDatabase(Config.INFLUXDB_DATABASE);
            influxDB.enableBatch(20, 5, TimeUnit.SECONDS);
            this.clientRef.set(influxDB);
        }
        return influxDB;
    }
}
