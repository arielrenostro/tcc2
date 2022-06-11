package br.furb.ariel.middleware.client.metrics;

import java.util.Map;

public class ConnectionMetric extends Metric {

    public ConnectionMetric() {
        super( //
                "connection", //
                Map.of(), //
                Map.of("amount", 1)//
        );
    }
}
