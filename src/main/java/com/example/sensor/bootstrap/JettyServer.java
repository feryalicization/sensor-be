package com.example.sensor.bootstrap;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.server.ResourceConfig;
import com.example.sensor.config.AppConfig;
import com.example.sensor.api.SensorResource;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.jersey.jackson.JacksonFeature;

public class JettyServer {
    private final Server server;

    public JettyServer(int port) {
        server = new Server(port);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        ResourceConfig config = new ResourceConfig()
                .register(SensorResource.class)
                .packages("com.example.sensor.api")
                .register(JacksonFeature.class) // penting untuk JSON
                .register(JacksonJaxbJsonProvider.class);

        ServletContainer servletContainer = new ServletContainer(config);
        context.addServlet(new org.eclipse.jetty.servlet.ServletHolder(servletContainer), "/*");

        // load properties once
        AppConfig.load();
    }

    public void start() throws Exception {
        server.start();
        server.join();
    }
}
