package com.example.sensor.api;

import com.example.sensor.config.JPAFactory;
import com.example.sensor.domain.LastMeasurement;
import com.example.sensor.domain.Sensor;
import com.example.sensor.repo.SensorRepository;
import com.example.sensor.service.SensorService;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.Instant;
import java.util.Map;

@Path("/api/sensor")
@Produces(MediaType.APPLICATION_JSON)
public class SensorResource {

    @GET
    @Path("/result")
    public Response getResult() {
        EntityManager em = JPAFactory.get().createEntityManager();
        try {
            SensorService svc = new SensorService(new SensorRepository(em));
            Map<String, Object> payload = svc.getResultPayload();
            return Response.ok(payload).build();
        } finally {
            em.close();
        }
    }

    // POST /api/sensor (contoh payload minimal)
    // {
    // "node":"5faea...",
    // "title":"Temperature","unit":"°C","sensorType":"BME280","icon":"osem-thermometer",
    // "lastMeasurement":{"createdAt":"2024-02-05T02:59:12.394Z","value":"29.99"}
    // }
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(Map<String, Object> body) {
        EntityManager em = JPAFactory.get().createEntityManager();
        try {
            em.getTransaction().begin();

            Sensor s = new Sensor();
            s.setNode((String) body.getOrDefault("node", "unknown"));
            s.setTitle((String) body.get("title"));
            s.setUnit((String) body.get("unit"));
            s.setSensorType((String) body.get("sensorType"));
            s.setIcon((String) body.get("icon"));
            em.persist(s);

            Map<String, Object> lm = (Map<String, Object>) body.get("lastMeasurement");
            if (lm != null) {
                LastMeasurement last = new LastMeasurement();
                last.setSensor(s);
                String createdAt = (String) lm.get("createdAt");
                last.setCreatedAt(createdAt != null ? Instant.parse(createdAt) : Instant.now());
                last.setValue((String) lm.get("value"));
                em.persist(last);
            }

            em.getTransaction().commit();
            return Response.status(Response.Status.CREATED).entity(Map.of("id", s.getId())).build();
        } catch (Exception e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            return Response.status(400).entity(Map.of("error", e.getMessage())).build();
        } finally {
            em.close();
        }
    }

    // DELETE /api/sensor?id=123
    @DELETE
    public Response delete(@QueryParam("id") Long id) {
        if (id == null)
            return Response.status(400).entity(Map.of("error", "id is required")).build();
        EntityManager em = JPAFactory.get().createEntityManager();
        try {
            em.getTransaction().begin();
            new SensorRepository(em).delete(id);
            em.getTransaction().commit();
            return Response.noContent().build();
        } catch (Exception e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            return Response.status(400).entity(Map.of("error", e.getMessage())).build();
        } finally {
            em.close();
        }
    }
}
