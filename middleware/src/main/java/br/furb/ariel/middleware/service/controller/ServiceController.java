package br.furb.ariel.middleware.service.controller;

import br.furb.ariel.middleware.exception.MiddlewareException;
import br.furb.ariel.middleware.service.model.Service;
import br.furb.ariel.middleware.service.service.ServiceService;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/services")
public class ServiceController {

    @Inject
    ServiceService service;

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Service> findAll() {
        return this.service.findAll();
    }

    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Service save(Service service) {
        service.setId(null);
        try {
            this.service.persist(service);
            return service;
        } catch (MiddlewareException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Service findById(@PathParam("id") String id) {
        return this.service.findById(id);
    }
}
