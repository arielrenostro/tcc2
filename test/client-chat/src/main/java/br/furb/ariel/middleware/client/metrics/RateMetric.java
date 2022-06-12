package br.furb.ariel.middleware.client.metrics;

import java.math.BigDecimal;
import java.util.Map;

public class RateMetric extends Metric {

    public RateMetric(BigDecimal rate, String tag) {
        super( //
                "rate", //
                Map.of("type", tag), //
                Map.of("value", rate)//
        );
    }
}
