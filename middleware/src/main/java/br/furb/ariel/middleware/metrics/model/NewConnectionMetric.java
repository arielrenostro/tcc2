package br.furb.ariel.middleware.metrics.model;

import java.util.Collections;
import java.util.Map;

public class NewConnectionMetric extends Metric {

    public NewConnectionMetric() {
        super( //
                "newConnection", //
                Collections.emptyMap(),  //
                Map.of("amount", 1) //
        );
    }
}
