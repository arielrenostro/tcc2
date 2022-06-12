package br.furb.ariel.middleware.service.config;

public class Config {

    public static final String RABBITMQ_HOST = System.getenv("RABBITMQ_HOST");
    public static final int RABBITMQ_PORT = getInt("RABBITMQ_PORT", 5672);
    public static final String RABBITMQ_VHOST = System.getenv("RABBITMQ_VHOST");
    public static final String RABBITMQ_USERNAME = System.getenv("RABBITMQ_USERNAME");
    public static final String RABBITMQ_PASSWORD = System.getenv("RABBITMQ_PASSWORD");
    public static final boolean RABBITMQ_SSL = getBoolean("RABBITMQ_SSL", false);

    public static final String SERVICE_ID = System.getenv("SERVICE_ID");

    public static final boolean DEBUG = getBoolean("DEBUG", false);

    public static final int RABBITMQ_CONSUMERS = 10;
    public static final int RABBITMQ_PUBLISH_TIMEOUT = 5000;
    public static final String RABBITMQ_INPUT_QUEUE = "chat-service.input";
    public static final String RABBITMQ_MIDDLEWARE_NOTIFICATION_EXCHANGE = "middleware.notification";

    private static boolean getBoolean(String env, boolean def) {
        String value = System.getenv(env);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return def;
    }

    private static int getInt(String env, int def) {
        String value = System.getenv(env);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return def;
    }
}
