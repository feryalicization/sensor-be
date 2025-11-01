-- Seed sensors
INSERT INTO sensors (node, title, unit, sensorType, icon) VALUES
('5faea98f9b2df8001b92dfac','Pollutant Measurement','µg/m³','SDS 011','osem-cloud'),
('5faea98f9b2df8001b92dfac','Temperature','°C','BME280','osem-thermometer'),
('5faea98f9b2df8001b92dfac','Humidity','%','BME280','osem-humidity'),
('5faea98f9b2df8001b92dfac','Pressure','Pa','BME280','osem-barometer');

-- Seed last_measurements (ambild ID by title)
INSERT INTO last_measurements (sensor_id, createdAt, value)
SELECT id, '2024-02-05 02:56:40.838', '7.63' FROM sensors WHERE title='Pollutant Measurement';

INSERT INTO last_measurements (sensor_id, createdAt, value)
SELECT id, '2024-02-05 02:59:12.394', '29.99' FROM sensors WHERE title='Temperature';

INSERT INTO last_measurements (sensor_id, createdAt, value)
SELECT id, '2024-02-05 02:59:12.394', '73.48' FROM sensors WHERE title='Humidity';

INSERT INTO last_measurements (sensor_id, createdAt, value)
SELECT id, '2024-02-05 02:59:12.394', '101500.91' FROM sensors WHERE title='Pressure';
