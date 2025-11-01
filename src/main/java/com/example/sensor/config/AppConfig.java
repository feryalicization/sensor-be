package com.example.sensor.config;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static final Properties PROPS = new Properties();

    public static void load() {
        try {
            // 1) coba file di working dir
            try (InputStream in = new FileInputStream("sensorbackend.properties")) {
                PROPS.load(in);
                return;
            }
        } catch (Exception ignored) {
        }

        try {
            // 2) coba dari classpath
            try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("sensorbackend.properties")) {
                if (in != null) {
                    PROPS.load(in);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load sensorbackend.properties", e);
        }
    }

    public static String get(String key, String def) {
        return PROPS.getProperty(key, def);
    }
}
