package br.furb.ariel.middleware.client.metrics;

import java.util.Map;

public class MessageReceivedMetric extends Metric {

    public MessageReceivedMetric(boolean ok, long ellapsed) {
        super( //
                "messageReceived", //
                Map.of("ok", String.valueOf(ok)), //
                Map.of("amount", 1, "ellapsed", ellapsed) //
        );
    }
}
