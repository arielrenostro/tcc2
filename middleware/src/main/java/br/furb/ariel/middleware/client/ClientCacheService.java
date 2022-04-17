package br.furb.ariel.middleware.client;

import br.furb.ariel.middleware.cache.CacheService;
import br.furb.ariel.middleware.config.Config;
import br.furb.ariel.middleware.container.ContainerService;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ClientCacheService extends CacheService {

    @Inject
    ContainerService containerService;

    public ClientCacheService() {
        super("client");
    }

    public void register(String clientId) {
        String containerId = this.containerService.getId();
        setex(clientId, containerId, Config.CACHE_CLIENT_REGISTER_TIMEOUT);
    }

    public void deregister(String clientId) {
        del(clientId);
    }

    public String findContainerClient(String clientId) {
        return get(clientId);
    }
}
