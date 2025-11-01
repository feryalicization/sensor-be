package com.example.sensor.repo;

import com.example.sensor.domain.Sensor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class SensorRepository {
    private final EntityManager em;

    public SensorRepository(EntityManager em) {
        this.em = em;
    }

    public List<Sensor> findAll() {
        // fetch join biar measurements kepanggil
        TypedQuery<Sensor> q = em.createQuery(
                "select distinct s from Sensor s left join fetch s.measurements", Sensor.class);
        return q.getResultList();
    }

    public Sensor find(Long id) {
        return em.find(Sensor.class, id);
    }

    public void save(Sensor s) {
        em.persist(s);
    }

    public void delete(Long id) {
        Sensor s = em.find(Sensor.class, id);
        if (s != null)
            em.remove(s);
    }
}
