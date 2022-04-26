package br.furb.ariel.middleware.core;

import java.util.List;

public abstract class BaseService<T, D extends BaseRepository<T>> {

    protected abstract D getRepository();

    public List<T> findAll() {
        return this.getRepository().findAll().list();
    }

    public T findById(String id) {
        return this.getRepository().findById(id);
    }

    public void persist(T entity) {
        this.getRepository().persist(entity);
    }

}
