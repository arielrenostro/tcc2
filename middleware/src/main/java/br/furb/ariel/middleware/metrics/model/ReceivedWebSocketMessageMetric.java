package br.furb.ariel.middleware.metrics.model;

import java.util.Collections;
import java.util.Map;

public class ReceivedWebSocketMessageMetric extends Metric {

    public ReceivedWebSocketMessageMetric(int size) {
        super( //
                "receivedWebSocketMessage", //
                Collections.emptyMap(), //
                Map.of("amount", 1, "size", size) //
        );
    }
}
