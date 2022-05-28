package br.furb.ariel.middleware.core;

import br.furb.ariel.middleware.exception.MiddlewareException;
import br.furb.ariel.middleware.service.model.Service;
import org.bson.types.ObjectId;

import java.util.List;

public abstract class BaseService<T, D extends BaseRepository<T>> {

    protected abstract D getRepository();

    public List<T> findAll() {
        return this.getRepository().findAll().list();
    }

    public T findById(String id) {
        return this.getRepository().findById(id);
    }

    public T deleteById(ObjectId id) {
        T entity = this.getRepository().findById(id);
        if (entity != null) {
            this.getRepository().deleteById(id);
        }
        return entity;
    }

    public void persist(T entity) throws MiddlewareException {
        this.getRepository().persist(entity);
    }

    public void update(T entity) {
        this.getRepository().update(entity);
    }
}
