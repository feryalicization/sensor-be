package com.example.sensor.config;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

public class JPAFactory {
    private static EntityManagerFactory emf;

    public static synchronized EntityManagerFactory get() {
        if (emf == null) {
            String choice = AppConfig.get("simpletask.jdbc.database", "H2");
            Map<String, String> overrides = new HashMap<>();

            if ("MariaDB".equalsIgnoreCase(choice)) {
                overrides.put("jakarta.persistence.jdbc.driver", "org.mariadb.jdbc.Driver");
                overrides.put("jakarta.persistence.jdbc.url",
                        AppConfig.get("simpletask.jdbc.url", "jdbc:mariadb://127.0.0.1:3306/sensordb"));
                overrides.put("jakarta.persistence.jdbc.user", AppConfig.get("simpletask.jdbc.user", "root"));
                overrides.put("jakarta.persistence.jdbc.password", AppConfig.get("simpletask.jdbc.password", ""));
            } else {
                overrides.put("jakarta.persistence.jdbc.driver", "org.h2.Driver");
                overrides.put("jakarta.persistence.jdbc.url", "jdbc:h2:mem:sensordb;DB_CLOSE_DELAY=-1");
                overrides.put("jakarta.persistence.jdbc.user", "sa");
                overrides.put("jakarta.persistence.jdbc.password", "");
            }

            emf = Persistence.createEntityManagerFactory("sensorPU", overrides);
        }
        return emf;
    }
}
