package com.example.sensor.config;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            String origin = requestContext.getHeaderString("Origin");
            String reqHeaders = requestContext.getHeaderString("Access-Control-Request-Headers");
            String reqMethod = requestContext.getHeaderString("Access-Control-Request-Method");

            Response.ResponseBuilder rb = Response.ok();
            if (origin != null)
                rb.header("Access-Control-Allow-Origin", origin);
            rb.header("Vary", "Origin");

            rb.header("Access-Control-Allow-Methods",
                    reqMethod != null ? reqMethod : "GET,POST,PUT,DELETE,OPTIONS,HEAD");
            rb.header("Access-Control-Allow-Headers",
                    reqHeaders != null ? reqHeaders : "origin, content-type, accept, authorization");
            rb.header("Access-Control-Allow-Credentials", "true");
            rb.header("Access-Control-Max-Age", "86400");

            requestContext.abortWith(rb.build());
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        String origin = requestContext.getHeaderString("Origin");
        if (origin != null) {
            responseContext.getHeaders().putSingle("Access-Control-Allow-Origin", origin);
            responseContext.getHeaders().putSingle("Vary", "Origin");
        } else {
            responseContext.getHeaders().putSingle("Access-Control-Allow-Origin", "*");
        }

        responseContext.getHeaders().putSingle("Access-Control-Allow-Credentials", "true");
        responseContext.getHeaders().putSingle("Access-Control-Allow-Headers",
                "origin, content-type, accept, authorization");
        responseContext.getHeaders().putSingle("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
    }
}
