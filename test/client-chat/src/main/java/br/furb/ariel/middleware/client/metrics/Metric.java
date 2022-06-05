package br.furb.ariel.middleware.client.metrics;

import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public abstract class Metric {

    private final String name;
    private final Date date = new Date();
    private final Map<String, String> tags;
    private final Map<String, Object> fields;

}
