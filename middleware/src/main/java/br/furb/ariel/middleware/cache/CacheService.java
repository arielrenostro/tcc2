package br.furb.ariel.middleware.cache;

import io.quarkus.redis.client.RedisClient;

import javax.inject.Inject;
import java.util.List;

public abstract class CacheService {

    private final String prefix;

    @Inject
    RedisClient redisClient;

    //    @Inject
    //    ReactiveRedisClient reactiveRedisClient;

    protected CacheService(String prefix) {
        this.prefix = prefix;
    }

    public String get(String key) {
        String realKey = buildKey(key);
        return this.redisClient.get(realKey).toString();
    }

    public void set(String key, String value) {
        String realKey = buildKey(key);
        this.redisClient.set(List.of(realKey, value));
    }

    public void del(String key) {
        String realKey = buildKey(key);
        this.redisClient.del(List.of(realKey));
    }

    private String buildKey(String key) {
        return prefix + "_" + key;
    }
}
