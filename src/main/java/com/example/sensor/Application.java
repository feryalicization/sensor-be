package com.example.sensor;

import com.example.sensor.bootstrap.JettyServer;

public class Application {
    public static void main(String[] args) throws Exception {
        new JettyServer(8080).start();
    }
}
