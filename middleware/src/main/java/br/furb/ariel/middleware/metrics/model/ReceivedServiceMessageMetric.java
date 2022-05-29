package br.furb.ariel.middleware.metrics.model;

import java.util.Collections;
import java.util.Map;

public class ReceivedServiceMessageMetric extends Metric {

    public ReceivedServiceMessageMetric(int size) {
        super( //
                "receivedServiceMessage", //
                Collections.emptyMap(), //
                Map.of("amount", 1, "size", size) //
        );
    }
}
