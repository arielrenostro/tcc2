package br.furb.ariel.middleware.metrics.model;

import java.util.Collections;
import java.util.Map;

public class SendServiceMessageMetric extends Metric {

    public SendServiceMessageMetric(int size) {
        super( //
                "sendServiceMessage", //
                Collections.emptyMap(), //
                Map.of("amount", 1, "size", size) //
        );
    }
}
