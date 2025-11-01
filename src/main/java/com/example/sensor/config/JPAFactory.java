package com.example.sensor.config;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class JPAFactory {
    private static volatile EntityManagerFactory EMF;

    private JPAFactory() {
    }

    public static synchronized EntityManagerFactory get() {
        if (EMF == null) {
            Map<String, Object> overrides = loadJdbcOverrides();
            EMF = Persistence.createEntityManagerFactory("sensorPU", overrides);
            System.out.println("[JPA] Using DB: " + overrides.getOrDefault("jakarta.persistence.jdbc.url", "(none)") +
                    " as " + overrides.getOrDefault("jakarta.persistence.jdbc.user", "(no-user)"));
        }
        return EMF;
    }

    private static Map<String, Object> loadJdbcOverrides() {
        Properties p = new Properties();

        try (InputStream in = new FileInputStream("sensorbackend.properties")) {
            p.load(in);
        } catch (Exception ignore) {
            try (InputStream in = JPAFactory.class.getClassLoader().getResourceAsStream("sensorbackend.properties")) {
                if (in != null)
                    p.load(in);
            } catch (Exception ignore2) {
            }
        }

        String pick = p.getProperty("simpletask.jdbc.database", "H2").trim();

        Map<String, Object> m = new HashMap<>();
        if ("MariaDB".equalsIgnoreCase(pick)) {
            String url = p.getProperty("simpletask.jdbc.url");
            String user = p.getProperty("simpletask.jdbc.user");
            String pass = p.getProperty("simpletask.jdbc.password");

            if (url == null || user == null) {
                throw new IllegalStateException("MariaDB selected, but url/user not set in sensorbackend.properties");
            }
            m.put("jakarta.persistence.jdbc.driver", "org.mariadb.jdbc.Driver");
            m.put("jakarta.persistence.jdbc.url", url);
            m.put("jakarta.persistence.jdbc.user", user);
            m.put("jakarta.persistence.jdbc.password", pass == null ? "" : pass);

            m.put("eclipselink.target-database", "MySQL");
        } else {
            m.put("jakarta.persistence.jdbc.driver", "org.h2.Driver");
            m.put("jakarta.persistence.jdbc.url", "jdbc:h2:file:./sensordb;DB_CLOSE_DELAY=-1;MODE=MySQL");
            m.put("jakarta.persistence.jdbc.user", "sa");
            m.put("jakarta.persistence.jdbc.password", "");
        }

        return m;
    }
}
