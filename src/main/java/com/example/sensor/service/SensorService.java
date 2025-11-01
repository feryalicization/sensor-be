package com.example.sensor.service;

import com.example.sensor.domain.LastMeasurement;
import com.example.sensor.domain.Sensor;
import com.example.sensor.repo.SensorRepository;

import java.util.*;
import java.util.stream.Collectors;

public class SensorService {
    private final SensorRepository repo;

    public SensorService(SensorRepository repo) {
        this.repo = repo;
    }

    public Map<String, Object> getResultPayload() {
        List<Sensor> sensors = repo.findAll();

        String node = sensors.isEmpty() ? "unknown" : sensors.get(0).getNode();

        List<Map<String, Object>> sensorDtos = sensors.stream().map(s -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("_id", String.valueOf(s.getId()));
            m.put("title", s.getTitle());
            m.put("unit", s.getUnit());
            m.put("sensorType", s.getSensorType());
            m.put("icon", s.getIcon());

            LastMeasurement last = (s.getMeasurements() == null || s.getMeasurements().isEmpty())
                    ? null
                    : s.getMeasurements().get(0); // karena @OrderBy DESC

            Map<String, Object> lastDto = new LinkedHashMap<>();
            if (last != null) {
                lastDto.put("createdAt", last.getCreatedAt().toString());
                lastDto.put("value", last.getValue());
            } else {
                lastDto.put("createdAt", null);
                lastDto.put("value", null);
            }
            m.put("lastMeasurement", lastDto);
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("node", node);
        result.put("sensors", sensorDtos);
        return result;
    }
}
