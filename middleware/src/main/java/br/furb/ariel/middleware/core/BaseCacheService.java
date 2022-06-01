package br.furb.ariel.middleware.core;

import io.quarkus.redis.client.RedisClient;
import io.vertx.redis.client.Response;

import javax.inject.Inject;
import java.time.Duration;
import java.util.List;

public abstract class BaseCacheService {

    private final String prefix;

    @Inject
    RedisClient redisClient;

    protected BaseCacheService(String prefix) {
        this.prefix = prefix;
    }

    public String get(String key) {
        String realKey = buildKey(key);
        Response obj = this.redisClient.get(realKey);
        if (obj != null) {
            return obj.toString();
        }
        return null;
    }

    public void setex(String key, String value, Duration duration) {
        String realKey = buildKey(key);
        this.redisClient.setex(realKey, String.valueOf(duration.getSeconds()), value);
    }

    public void del(String key) {
        String realKey = buildKey(key);
        this.redisClient.del(List.of(realKey));
    }

    private String buildKey(String key) {
        return prefix + "_" + key;
    }
}
