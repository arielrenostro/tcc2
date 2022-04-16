package br.furb.ariel.middleware.healthcheck;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/health-check")
public class HealthCheckResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public HealthCheckDTO healthCheck() {
        HealthCheckDTO dto = new HealthCheckDTO();
        dto.setOk(true);
        return dto;
    }
}
