package br.furb.ariel.middleware.metrics.model;

import java.util.Collections;
import java.util.Map;

public class ConnectionsMetric extends Metric {

    public ConnectionsMetric(int connections) {
        super( //
                "connections", //
                Collections.emptyMap(), //
                Map.of("connections", connections) //
        );
    }
}
