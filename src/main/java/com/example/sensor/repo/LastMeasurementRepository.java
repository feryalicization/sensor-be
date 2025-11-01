package com.example.sensor.repo;

import com.example.sensor.domain.LastMeasurement;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class LastMeasurementRepository {
    private final EntityManager em;

    public LastMeasurementRepository(EntityManager em) {
        this.em = em;
    }

    public List<LastMeasurement> findAll() {
        TypedQuery<LastMeasurement> q = em.createQuery("SELECT l FROM LastMeasurement l", LastMeasurement.class);
        return q.getResultList();
    }

    public LastMeasurement find(Long id) {
        return em.find(LastMeasurement.class, id);
    }

    public void save(LastMeasurement lm) {
        em.persist(lm);
    }

    public void delete(Long id) {
        LastMeasurement lm = em.find(LastMeasurement.class, id);
        if (lm != null) {
            em.remove(lm);
        }
    }
}
