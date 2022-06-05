package br.furb.ariel.middleware.client.metrics;

import java.util.Map;

public class MiddlewareTimeoutMetric extends Metric {

    public MiddlewareTimeoutMetric() {
        super( //
                "middlewareTimeout", //
                Map.of(), //
                Map.of("amount", 1)//
        );
    }
}
