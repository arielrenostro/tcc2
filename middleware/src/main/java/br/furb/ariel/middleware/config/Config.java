package br.furb.ariel.middleware.config;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Singleton;
import java.time.Duration;

@Singleton
public class Config {

    public static final String RABBITMQ_EXCHANGE_TO_SEND = "middleware.to-send";
    public static final String RABBITMQ_EXCHANGE_NOTIFICATION = "middleware.notification";

    public static final Duration CACHE_CLIENT_REGISTER_TIMEOUT = Duration.ofSeconds(30);

    public static final Duration WEBSOCKET_PING_TIMEOUT = Duration.ofSeconds(5);
    public static final Duration WEBSOCKET_PING_INTERNAL = Duration.ofSeconds(20);
    public static final String WEBSOCKET_CHECK_PING_JOB_INTERVAL = "10s";

    @ConfigProperty(name = "RABBITMQ_HOST")
    String rabbitmqHost;

    @ConfigProperty(name = "RABBITMQ_PORT")
    Integer rabbitmqPort;

    @ConfigProperty(name = "RABBITMQ_VHOST")
    String rabbitmqVhost;

    @ConfigProperty(name = "RABBITMQ_USERNAME")
    String rabbitmqUsername;

    @ConfigProperty(name = "RABBITMQ_PASSWORD")
    String rabbitmqPassword;

    @ConfigProperty(name = "RABBITMQ_SSL")
    boolean rabbitmqSSL;

    @ConfigProperty(name = "RABBITMQ_TIMEOUT_PUBLISH")
    Long rabbitmqTimeoutPublish;

    @ConfigProperty(name = "CONSUMERS_TO_SEND")
    int consumersToSend;

    public Config() {

    }

    public String getRabbitmqHost() {
        return rabbitmqHost;
    }

    public Integer getRabbitmqPort() {
        return rabbitmqPort;
    }

    public String getRabbitmqVhost() {
        return rabbitmqVhost;
    }

    public String getRabbitmqUsername() {
        return rabbitmqUsername;
    }

    public String getRabbitmqPassword() {
        return rabbitmqPassword;
    }

    public boolean isRabbitmqSSL() {
        return rabbitmqSSL;
    }

    public Long getRabbitmqTimeoutPublish() {
        return rabbitmqTimeoutPublish;
    }

    public int getConsumersToSend() {
        return consumersToSend;
    }

}
