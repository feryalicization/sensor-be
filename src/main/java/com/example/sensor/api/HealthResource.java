package com.example.sensor.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/health")
public class HealthResource {

    @GET
    @Path("/text")
    @Produces(MediaType.TEXT_PLAIN)
    public String text() {
        return "OK";
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response json() {
        // simple JSON without Jackson object mapping
        String payload = "{\"status\":\"UP\"}";
        return Response.ok(payload, MediaType.APPLICATION_JSON).build();
    }
}
