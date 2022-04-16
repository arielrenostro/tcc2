package br.furb.ariel.middleware.container;

import javax.inject.Singleton;
import java.util.UUID;

@Singleton
public class ContainerService {

    private final String id = UUID.randomUUID().toString();

    public String getId() {
        return id;
    }
}
