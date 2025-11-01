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
import java.time.ZoneOffset;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

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

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(Map<String, Object> body) {
        EntityManager em = JPAFactory.get().createEntityManager();
        try {
            em.getTransaction().begin();

            Sensor s = new Sensor();
            s.setNode(asStringOrDefault(body.get("node"), "unknown"));
            s.setTitle(asString(body.get("title")));
            s.setUnit(asString(body.get("unit")));
            s.setSensorType(asString(body.get("sensorType")));
            s.setIcon(asString(body.get("icon")));
            em.persist(s);

            @SuppressWarnings("unchecked")
            Map<String, Object> lm = (Map<String, Object>) body.get("lastMeasurement");
            if (lm != null) {
                LastMeasurement last = new LastMeasurement();
                last.setSensor(s);
                Instant createdAtInstant = parseInstantFlexible(lm.get("createdAt"));
                LocalDateTime createdAt = LocalDateTime.ofInstant(createdAtInstant, ZoneOffset.UTC);
                last.setCreatedAt(createdAt);
                last.setValue(asString(lm.get("value")));
                em.persist(last);
            }

            em.getTransaction().commit();
            return Response.status(Response.Status.CREATED).entity(Map.of("id", s.getId())).build();
        } catch (Exception e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            Throwable t = e;
            while (t.getCause() != null)
                t = t.getCause();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", t.getClass().getName() + ": " + t.getMessage()))
                    .build();
        } finally {
            em.close();
        }
    }

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

    /* ---------- Utility helpers below ---------- */

    private static Instant parseInstantFlexible(Object input) {
        if (input == null)
            return Instant.now();
        try {
            if (input instanceof Number)
                return Instant.ofEpochMilli(((Number) input).longValue());
            String s = input.toString().trim();
            if (s.matches("\\d+"))
                return Instant.ofEpochMilli(Long.parseLong(s));
            return Instant.parse(s);
        } catch (Exception e) {
            return Instant.now();
        }
    }

    private static String asString(Object o) {
        return o == null ? null : o.toString();
    }

    private static String asStringOrDefault(Object o, String def) {
        String s = asString(o);
        return (s == null || s.isBlank()) ? def : s;
    }
}
