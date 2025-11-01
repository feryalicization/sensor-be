# Sensor Environment REST API (Java + Jetty + JAX-RS + EclipseLink)

This project provides a RESTful API service for managing sensor environment data.  
It supports both **H2 Embedded Database** (for local development) and **MariaDB** (for production), with configuration through a `sensorbackend.properties` file.  
The application is fully **containerized using Docker** and can be built and run via a single `docker-compose` command.

---

## 📘 Project Overview

The system exposes REST endpoints to:

- Retrieve sensor readings (`GET /api/sensor/result`)
- Create new sensor entries (`POST /api/sensor`)
- Delete sensor data (`DELETE /api/sensor?id={id}`)
- Check health (`GET /health/text`)

Each sensor has metadata (`title`, `unit`, `sensorType`, etc.) and its last measurement (`createdAt`, `value`).  
The backend is built using **Jetty + JAX-RS (Jersey)** with **EclipseLink JPA** as ORM.

---

## 🏗️ Project Architecture

````text
sensor-backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/sensor/
│   │   │       ├── api/               → REST API Endpoints (JAX-RS)
│   │   │       ├── config/            → JPA & Jetty Configuration
│   │   │       ├── domain/            → Entity Models (Sensor, LastMeasurement)
│   │   │       ├── repo/              → Repository Layer (EclipseLink Queries)
│   │   │       ├── service/           → Business Logic Layer
│   │   │       └── Application.java   → Main Jetty Server Entrypoint
│   │   └── resources/
│   │       ├── META-INF/
│   │       │   └── persistence.xml    → JPA Persistence Config
│   │       ├── db/
│   │       │   └── migration/         → Flyway Migration Scripts
│   │       └── sensorbackend.properties → Database Configuration
│   └── test/
│       └── java/
│           └── com/example/sensor/    → JUnit Tests
│
├── Dockerfile                         → Backend Docker Build Definition
├── docker-compose.yml                 → Multi-Service Compose (Backend, DB, Frontend)
├── pom.xml                            → Maven Build Configuration
└── README.md                          → Project Documentation


---

## 🧰 Technology Stack

| Layer              | Technology                              |
| ------------------ | --------------------------------------- |
| Web Server         | **Eclipse Jetty 11**                    |
| REST Framework     | **Jakarta JAX-RS (Jersey 3.1)**         |
| ORM / JPA Provider | **EclipseLink 4.0.4**                   |
| Databases          | **MariaDB** / **H2 Embedded**           |
| Migration          | **Flyway 10.x**                         |
| Build System       | **Apache Maven 3.9+**                   |
| Testing            | **JUnit 5 (Jupiter)**                   |
| Containerization   | **Docker + Docker Compose**             |
| Frontend           | (Your React/Next.js app on port `3000`) |

---

## 🧩 Prerequisites

- Java 17+
- Maven 3.9+
- Docker & Docker Compose
- MariaDB 10.6+ (if not using the Docker version)

---

## ⚙️ Environment Configuration

File: `sensorbackend.properties`

```properties
# Database mode: H2 (default) or MariaDB
simpletask.jdbc.database=MariaDB

# For MariaDB
simpletask.jdbc.url=jdbc:mariadb://mariadb:3306/sensordb
simpletask.jdbc.user=root
simpletask.jdbc.password=rootpassword


````

Database Setup
Option 1 — Using Flyway (automatic migration)
mvn -q flyway:migrate \
 -Dflyway.url=jdbc:mariadb://localhost:3306/sensordb \
 -Dflyway.user=root \
 -Dflyway.password=rootpassword

Build Instructions

1. Clone the repository
   git clone https://github.com/<your-username>/sensor-backend.git
   cd sensor-backend

2. Build with Maven
   mvn clean package -DskipTests

3. Run manually (for local test)
   java -jar target/sensor-backend-1.0.0.jar
