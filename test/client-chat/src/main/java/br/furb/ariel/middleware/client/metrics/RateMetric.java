package br.furb.ariel.middleware.client.metrics;

import java.math.BigDecimal;
import java.util.Map;

public class RateMetric extends Metric {

    public RateMetric(BigDecimal rate) {
        super( //
                "rate", //
                Map.of(), //
                Map.of("value", rate)//
        );
    }
}
