package br.furb.ariel.middleware.client.config;

public class Config {

    public static final String RABBITMQ_HOST = System.getenv("RABBITMQ_HOST");
    public static final int RABBITMQ_PORT = getInt("RABBITMQ_PORT", 5672);
    public static final String RABBITMQ_VHOST = System.getenv("RABBITMQ_VHOST");
    public static final String RABBITMQ_USERNAME = System.getenv("RABBITMQ_USERNAME");
    public static final String RABBITMQ_PASSWORD = System.getenv("RABBITMQ_PASSWORD");
    public static final boolean RABBITMQ_SSL = getBoolean("RABBITMQ_SSL", false);

    public static final String WEBSOCKET_URL = System.getenv("WEBSOCKET_URL");

    public static final String INFLUXDB_CONNECTION_STRING = System.getenv("INFLUXDB_CONNECTION_STRING");
    public static final String INFLUXDB_DATABASE = System.getenv("INFLUXDB_DATABASE");
    public static final boolean DEBUG = getBoolean("DEBUG", false);

    public static final int RABBITMQ_PUBLISH_TIMEOUT = 5000;
    public static final String RABBITMQ_BROADCAST_EXCHANGE = "client-chat.broadcast";
    public static final int MAX_MESSAGE_SIZE = 1024;
    public static final int MIN_MESSAGE_SIZE = 6;

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
