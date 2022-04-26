package br.furb.ariel.middleware.core;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import org.bson.types.ObjectId;

public abstract class BaseRepository<T> implements PanacheMongoRepository<T> {

    public T findById(String id) {
        return this.findById(new ObjectId(id));
    }
}
