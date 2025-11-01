package com.example.sensor.api;

import com.example.sensor.config.JPAFactory;
import com.example.sensor.domain.LastMeasurement;
import com.example.sensor.domain.Sensor;
import com.example.sensor.repo.SensorRepository;
import com.example.sensor.service.SensorService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Path("/api/sensor")
@Produces(MediaType.APPLICATION_JSON)
public class SensorResource {

    private static final DateTimeFormatter ISO_MILLIS = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    /**
     * ------------------------- GET /api/sensor/result -------------------------
     */
    /**
     * Query params:
     * page (default 0), size (default 10), q (optional; title/sensorType/unit,
     * case-insensitive), sort (id|title; default id)
     * Response:
     * {
     * "code": 200,
     * "message": "OK",
     * "data": { "node": "...", "sensors": [ ... ] },
     * "page": { "page":0,"size":10,"totalItems":N,"totalPages":M }
     * }
     */
    @GET
    @Path("/result")
    public Response getResult(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @QueryParam("q") @DefaultValue("") String q,
            @QueryParam("sort") @DefaultValue("id") String sort) {
        EntityManager em = JPAFactory.get().createEntityManager();
        try {
            if (page < 0)
                page = 0;
            if (size <= 0 || size > 200)
                size = 10;

            // WHERE + params
            String where = "";
            Map<String, Object> params = new HashMap<>();
            if (q != null && !q.trim().isEmpty()) {
                where = " WHERE (LOWER(s.title) LIKE :kw OR LOWER(s.sensorType) LIKE :kw OR LOWER(s.unit) LIKE :kw) ";
                params.put("kw", "%" + q.toLowerCase(Locale.ROOT) + "%");
            }

            // Count
            TypedQuery<Long> countQuery = em.createQuery("SELECT COUNT(s) FROM Sensor s" + where, Long.class);
            if (params.containsKey("kw"))
                countQuery.setParameter("kw", params.get("kw"));
            Long total = countQuery.getSingleResult();

            // Sorting
            String order = "s.id";
            if ("title".equalsIgnoreCase(sort))
                order = "s.title";

            // Page data
            TypedQuery<Sensor> query = em.createQuery(
                    "SELECT s FROM Sensor s" + where + " ORDER BY " + order + " ASC", Sensor.class);
            if (params.containsKey("kw"))
                query.setParameter("kw", params.get("kw"));
            // query.setFirstResult(page * size);
            if (page < 1)
                page = 1; // make page 1 = first page
            query.setFirstResult((page - 1) * size);
            query.setMaxResults(size);

            List<Sensor> sensors = query.getResultList();
            // Always fetch at least one node to display
            String node = "unknown";
            try {
                node = em.createQuery("SELECT s.node FROM Sensor s ORDER BY s.id ASC", String.class)
                        .setMaxResults(1)
                        .getSingleResult();
            } catch (Exception ignore) {
                /* leave as 'unknown' */ }

            List<Map<String, Object>> sensorItems = sensors.stream()
                    .map(s -> toSensorMapWithLastMeasurement(em, s))
                    .collect(Collectors.toList());

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("node", node);
            data.put("sensors", sensorItems);

            Map<String, Object> pageMeta = new LinkedHashMap<>();
            pageMeta.put("page", page);
            pageMeta.put("size", size);
            pageMeta.put("totalItems", total);
            pageMeta.put("totalPages", (int) Math.ceil(total / (double) size));

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("code", 200);
            response.put("message", "OK");
            response.put("data", data);
            response.put("page", pageMeta);

            return Response.ok(response).build();

        } catch (Exception e) {
            Throwable t = root(e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("code", 400, "message", t.getClass().getSimpleName() + ": " + t.getMessage()))
                    .build();
        } finally {
            em.close();
        }
    }

    /** ------------------------- POST /api/sensor ------------------------- */
    /**
     * Body minimal:
     * {
     * "node":"...", "title":"...", "unit":"...", "sensorType":"...", "icon":"...",
     * "lastMeasurement": {"createdAt":"2024-02-05T02:59:12.394Z","value":"29.99"}
     * // optional
     * }
     * Response (201):
     * {
     * "code": 201,
     * "message": "Created",
     * "data": { "_id":"...", "title":"...", "unit":"...", "sensorType":"...",
     * "icon":"...",
     * "lastMeasurement":{"createdAt":"...","value":"..."} }
     * }
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(Map<String, Object> body) {
        EntityManager em = JPAFactory.get().createEntityManager();
        try {
            // Validate
            String title = asString(body.get("title"));
            String unit = asString(body.get("unit"));
            String sensorType = asString(body.get("sensorType"));
            String icon = asString(body.get("icon"));
            if (isBlank(title) || isBlank(unit) || isBlank(sensorType) || isBlank(icon)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of(
                                "code", 400,
                                "message", "Missing required fields",
                                "required", new String[] { "title", "unit", "sensorType", "icon" }))
                        .build();
            }

            em.getTransaction().begin();

            Sensor s = new Sensor();
            s.setNode(asStringOrDefault(body.get("node"), "unknown"));
            s.setTitle(title);
            s.setUnit(unit);
            s.setSensorType(sensorType);
            s.setIcon(icon);
            em.persist(s);
            em.flush();

            @SuppressWarnings("unchecked")
            Map<String, Object> lm = (Map<String, Object>) body.get("lastMeasurement");
            LastMeasurement last = null;
            if (lm != null) {
                last = new LastMeasurement();
                last.setSensor(s);

                // Entity uses LocalDateTime: convert from Instant (ISO/epoch)
                Instant inst = parseInstantFlexible(lm.get("createdAt"));
                LocalDateTime ldt = LocalDateTime.ofInstant(inst, ZoneOffset.UTC);
                last.setCreatedAt(ldt);

                last.setValue(asString(lm.get("value")));
                em.persist(last);
                em.flush();
            }

            em.getTransaction().commit();

            Map<String, Object> created = toSensorMap(s, last);
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("code", 201);
            resp.put("message", "Created");
            resp.put("data", created);
            return Response.status(Response.Status.CREATED).entity(resp).build();

        } catch (Exception e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            Throwable t = root(e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("code", 400, "message", t.getClass().getSimpleName() + ": " + t.getMessage()))
                    .build();
        } finally {
            em.close();
        }
    }

    /**
     * ------------------------- DELETE /api/sensor?id=123 -------------------------
     */
    @DELETE
    public Response delete(@QueryParam("id") Long id) {
        if (id == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("code", 400, "message", "id is required"))
                    .build();
        }
        EntityManager em = JPAFactory.get().createEntityManager();
        try {
            em.getTransaction().begin();
            new SensorRepository(em).delete(id);
            em.getTransaction().commit();
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (Exception e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            Throwable t = root(e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("code", 400, "message", t.getClass().getSimpleName() + ": " + t.getMessage()))
                    .build();
        } finally {
            em.close();
        }
    }

    /* ======================== Helpers ======================== */

    private static Optional<LastMeasurement> findLatestMeasurement(EntityManager em, Long sensorId) {
        List<LastMeasurement> list = em.createQuery(
                "SELECT lm FROM LastMeasurement lm WHERE lm.sensor.id = :sid ORDER BY lm.createdAt DESC",
                LastMeasurement.class)
                .setParameter("sid", sensorId)
                .setMaxResults(1)
                .getResultList();
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    private Map<String, Object> toSensorMapWithLastMeasurement(EntityManager em, Sensor s) {
        Optional<LastMeasurement> lastOpt = findLatestMeasurement(em, s.getId());
        return toSensorMap(s, lastOpt.orElse(null));
    }

    private Map<String, Object> toSensorMap(Sensor s, LastMeasurement last) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("_id", String.valueOf(s.getId()));
        item.put("title", s.getTitle());
        item.put("unit", s.getUnit());
        item.put("sensorType", s.getSensorType());
        item.put("icon", s.getIcon());

        Map<String, Object> lm = new LinkedHashMap<>();
        if (last != null) {
            lm.put("createdAt", last.getCreatedAt() != null ? ISO_MILLIS.format(last.getCreatedAt()) : null);
            lm.put("value", last.getValue());
        } else {
            lm.put("createdAt", null);
            lm.put("value", null);
        }
        item.put("lastMeasurement", lm);
        return item;
    }

    private static String asString(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private static String asStringOrDefault(Object o, String def) {
        String s = asString(o);
        return (s == null || s.isBlank()) ? def : s;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * Parse createdAt:
     * - ISO-8601 string (e.g., 2024-02-05T02:59:12.394Z)
     * - epoch millis (Number/string)
     * - null -> now(UTC)
     */
    private static Instant parseInstantFlexible(Object input) {
        if (input == null)
            return Instant.now();
        if (input instanceof Number)
            return Instant.ofEpochMilli(((Number) input).longValue());
        String s = input.toString().trim();
        if (s.matches("\\d+"))
            return Instant.ofEpochMilli(Long.parseLong(s));
        return Instant.parse(s);
    }

    private static Throwable root(Throwable t) {
        while (t.getCause() != null)
            t = t.getCause();
        return t;
    }
}
