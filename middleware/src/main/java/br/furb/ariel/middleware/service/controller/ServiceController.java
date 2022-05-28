package br.furb.ariel.middleware.service.controller;

import br.furb.ariel.middleware.exception.MiddlewareException;
import br.furb.ariel.middleware.service.model.Service;
import br.furb.ariel.middleware.service.service.ServiceService;
import org.bson.types.ObjectId;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
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

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteById(@PathParam("id") String id) {
        Service service = this.service.deleteById(new ObjectId(id));
        if (service != null) {
            return Response.ok(service).build();
        }
        return Response.status(Status.NO_CONTENT).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Service updateById(@PathParam("id") String id, Service service) throws MiddlewareException {
        service.setId(new ObjectId(id));
        this.service.update(service);
        return service;
    }
}
