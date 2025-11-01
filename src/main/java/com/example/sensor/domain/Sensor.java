package com.example.sensor.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sensors")
public class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String node;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String unit;

    @Column(nullable = false)
    private String sensorType;

    @Column(nullable = false)
    private String icon;

    @OneToMany(mappedBy = "sensor", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<LastMeasurement> measurements = new ArrayList<>();

    // --- getters & setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getSensorType() {
        return sensorType;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<LastMeasurement> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(List<LastMeasurement> measurements) {
        this.measurements = measurements;
    }

    // helper opsional
    public void addMeasurement(LastMeasurement lm) {
        lm.setSensor(this);
        this.measurements.add(lm);
    }
}
