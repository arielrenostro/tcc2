package br.furb.ariel.middleware.client;

import br.furb.ariel.middleware.container.ContainerService;
import br.furb.ariel.middleware.cache.CacheService;

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
        this.set(clientId, containerId);
    }

    public void unregister(String clientId) {
        this.del(clientId);
    }

    public String findClient(String clientId) {
        return this.get(clientId);
    }
}
