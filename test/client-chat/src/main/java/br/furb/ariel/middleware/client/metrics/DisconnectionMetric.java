package br.furb.ariel.middleware.client.metrics;

import java.util.Map;

public class DisconnectionMetric extends Metric {

    public DisconnectionMetric() {
        super( //
                "disconnection", //
                Map.of(), //
                Map.of("amount", 1)//
        );
    }
}
