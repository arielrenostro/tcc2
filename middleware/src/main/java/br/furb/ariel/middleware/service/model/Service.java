package br.furb.ariel.middleware.service.model;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Data;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

@Data
@MongoEntity(collection = "services")
public class Service {

    private ObjectId id;
    private String name;
    private ServiceSendType sendType;
    private String exchangeName;
    private String routeKey;
    private List<String> routes = new ArrayList<>();

}
