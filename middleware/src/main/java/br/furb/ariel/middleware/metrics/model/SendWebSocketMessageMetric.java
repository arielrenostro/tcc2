package br.furb.ariel.middleware.metrics.model;

import java.util.Collections;
import java.util.Map;

public class SendWebSocketMessageMetric extends Metric {

    public SendWebSocketMessageMetric(int size) {
        super( //
                "sendWebSocketMessage", //
                Collections.emptyMap(), //
                Map.of("amount", 1, "size", size) //
        );
    }
}
