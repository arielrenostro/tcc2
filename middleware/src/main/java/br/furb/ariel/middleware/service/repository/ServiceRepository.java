package br.furb.ariel.middleware.service.repository;

import br.furb.ariel.middleware.core.BaseRepository;
import br.furb.ariel.middleware.service.model.Service;

import javax.inject.Singleton;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class ServiceRepository extends BaseRepository<Service> {

    private static final ConcurrentHashMap<String, Map<String, Object>> CACHE = new ConcurrentHashMap<>();

    @Override
    public void persist(Service service) {
        super.persist(service);
        CACHE.clear();
    }

    @Override
    public void update(Service service) {
        super.update(service);
        CACHE.clear();
    }

    public Service findByRoute(String route) {
        String key = "findByRote_" + route;
        Service cached = getCached(key);
        if (cached != null) {
            return cached;
        }

        Service service = this.find("routes", route).firstResult();
        updateCache(key, service, Duration.ofHours(1));
        return service;
    }

    @SuppressWarnings("unchecked")
    private <T> T getCached(String key) {
        Map<String, Object> element = CACHE.get(key);
        if (element != null) {
            long ttl = (Long) element.get("ttl");
            if (ttl > System.currentTimeMillis()) {
                return (T) element.get("value");
            }
        }
        return null;
    }

    private void updateCache(String key, Object obj, Duration expire) {
        Map<String, Object> element = Map.of( //
                "value", obj, //
                "ttl", System.currentTimeMillis() + expire.toMillis() //
        );
        CACHE.put(key, element);
    }
}
