package com.example.sensor.config;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
    @Override
    public Response toResponse(Throwable e) {
        Throwable root = e;
        while (root.getCause() != null)
            root = root.getCause();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", 500);
        body.put("message", root.getClass().getName() + ": " + String.valueOf(root.getMessage()));

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
