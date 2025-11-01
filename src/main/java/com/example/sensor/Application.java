package com.example.sensor;

import jakarta.servlet.DispatcherType;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.jackson.JacksonFeature; // <-- add

import org.glassfish.jersey.logging.LoggingFeature; // dev logging
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Application {
    public static void main(String[] args) throws Exception {
        int port = 8080;
        Server server = new Server(port);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        // Jersey
        ResourceConfig rc = new ResourceConfig()
                .packages("com.example.sensor.api", "com.example.sensor.config")
                .register(JacksonFeature.class); // <-- force-enable Jackson JSON

        // DEV logging & tracing to see root cause in logs
        Logger jl = Logger.getLogger("Jersey");
        jl.setLevel(Level.FINE);
        rc.register(new LoggingFeature(jl, Level.FINE, LoggingFeature.Verbosity.PAYLOAD_ANY, 4096));
        rc.property("jersey.config.server.tracing.type", "ALL");
        rc.property("jersey.config.server.tracing.threshold", "VERBOSE");

        ServletHolder jersey = new ServletHolder(new ServletContainer(rc));
        context.addServlet(jersey, "/*");

        // Jetty CORS filter (keep)
        FilterHolder cors = new FilterHolder(CrossOriginFilter.class);
        cors.setInitParameter("allowedOrigins", "http://localhost:3000");
        cors.setInitParameter("allowedMethods", "GET,POST,PUT,DELETE,OPTIONS,HEAD");
        cors.setInitParameter("allowedHeaders", "Origin,Content-Type,Accept,Authorization");
        cors.setInitParameter("exposedHeaders", "Location");
        cors.setInitParameter("allowCredentials", "true");
        cors.setInitParameter("chainPreflight", "false");
        context.addFilter(
                cors,
                "/*",
                EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC, DispatcherType.ERROR, DispatcherType.FORWARD,
                        DispatcherType.INCLUDE));

        server.start();
        server.join();
    }
}
