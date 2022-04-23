package br.furb.ariel.middleware.service.repository;

import br.furb.ariel.middleware.core.BaseRepository;
import br.furb.ariel.middleware.service.model.Service;

import javax.inject.Singleton;

@Singleton
public class ServiceRepository extends BaseRepository<Service> {

    public Service findByRoute(String route) {
        // TODO
        return null;
    }
}
