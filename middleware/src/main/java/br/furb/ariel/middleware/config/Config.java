package br.furb.ariel.middleware.config;

import java.time.Duration;

public class Config {

    public static final String RABBITMQ_HOST = "localhost";
    public static final Integer RABBITMQ_PORT = 5672;
    public static final String RABBITMQ_VHOST = "/middleware";
    public static final String RABBITMQ_USERNAME = "middleware";
    public static final String RABBITMQ_PASSWORD = "123456";
    public static final boolean RABBITMQ_SSL = false;

    public static final Long RABBITMQ_TIMEOUT_PUBLISH = 5000L;
    public static final String RABBITMQ_EXCHANGE_TO_SEND = "middleware.to-send";

    public static final Duration CACHE_CLIENT_REGISTER_TIMEOUT = Duration.ofSeconds(30);

    public static final Duration WEBSOCKET_PING_TIMEOUT = Duration.ofSeconds(5);
    public static final Duration WEBSOCKET_PING_INTERNAL = Duration.ofSeconds(20);
    public static final String WEBSOCKET_CHECK_PING_JOB_INTERVAL = "10s";

}
