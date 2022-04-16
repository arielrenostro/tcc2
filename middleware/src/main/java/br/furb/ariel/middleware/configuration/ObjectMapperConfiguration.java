package br.furb.ariel.middleware.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Singleton;

public class ObjectMapperConfiguration {

    @Singleton
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
