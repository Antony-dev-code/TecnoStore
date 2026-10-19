-- Crea la base de datos de TecnoStore con sus tablas y algunos datos de prueba.
-- Hay que ejecutarlo antes de usar el programa.

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- La base de datos
DROP DATABASE IF EXISTS TecnoStoreAnt;
CREATE DATABASE TecnoStoreAnt
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE TecnoStoreAnt;

-- Datos comunes de clientes y empleados
CREATE TABLE persona (
    id             INT          NOT NULL AUTO_INCREMENT,
    nombre         VARCHAR(100) NOT NULL,
    identificacion VARCHAR(20)  NOT NULL,
    telefono       VARCHAR(15)  NOT NULL,
    correo         VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_persona_identificacion (identificacion)
);

-- Empleados
CREATE TABLE empleado (
    id   INT                                        NOT NULL,
    tipo ENUM('CAJERO','BODEGUERO','ADMINISTRADOR') NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_empleado_persona FOREIGN KEY (id) REFERENCES persona (id)
);

-- Clientes
CREATE TABLE cliente (
    id  INT         NOT NULL,
    rut VARCHAR(20) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_cliente_persona FOREIGN KEY (id) REFERENCES persona (id)
);

-- Marcas
CREATE TABLE marca (
    id     INT          NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_marca_nombre (nombre)
);

-- Precios
CREATE TABLE precio (
    id     INT            NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100)   NOT NULL,
    valor  DECIMAL(15, 2) NOT NULL,
    PRIMARY KEY (id)
);

-- Celulares
CREATE TABLE celular (
    id                INT                        NOT NULL AUTO_INCREMENT,
    modelo            VARCHAR(100)               NOT NULL,
    id_marca_fk       INT                        NOT NULL,
    sistema_operativo ENUM('ANDROID','IOS')       NOT NULL,
    gama              ENUM('ALTA','MEDIA','BAJA') NOT NULL,
    id_precio_fk      INT                        NOT NULL,
    stock             INT                        NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_celular_marca  FOREIGN KEY (id_marca_fk)  REFERENCES marca  (id),
    CONSTRAINT fk_celular_precio FOREIGN KEY (id_precio_fk) REFERENCES precio (id)
);

-- Ventas
CREATE TABLE venta (
    id             INT            NOT NULL AUTO_INCREMENT,
    id_cliente_fk  INT            NOT NULL,
    fecha          DATETIME       NOT NULL,
    total          DECIMAL(15, 2) NOT NULL,
    id_empleado_fk INT            NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_venta_cliente  FOREIGN KEY (id_cliente_fk)  REFERENCES cliente  (id),
    CONSTRAINT fk_venta_empleado FOREIGN KEY (id_empleado_fk) REFERENCES empleado (id)
);

-- Las lineas de cada venta
CREATE TABLE detalle_venta (
    id            INT            NOT NULL AUTO_INCREMENT,
    id_venta_fk   INT            NOT NULL,
    id_celular_fk INT            NOT NULL,
    cantidad      INT            NOT NULL,
    subtotal      DECIMAL(15, 2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_detalle_venta   FOREIGN KEY (id_venta_fk)   REFERENCES venta   (id),
    CONSTRAINT fk_detalle_celular FOREIGN KEY (id_celular_fk) REFERENCES celular (id)
);

SET FOREIGN_KEY_CHECKS = 1;

-- Datos de prueba

-- Empleados
INSERT INTO persona (nombre, identificacion, telefono, correo) VALUES
    ('Pedro Ramirez',  '10000001', '3001234567', 'pedro.ramirez@tecnostore.com'),
    ('Maria Gonzalez', '10000002', '3017654321', 'maria.gonzalez@tecnostore.com'),
    ('Carlos Mendoza', '10000003', '3029876543', 'carlos.mendoza@tecnostore.com');

INSERT INTO empleado (id, tipo) VALUES
    (1, 'CAJERO'),
    (2, 'BODEGUERO'),
    (3, 'ADMINISTRADOR');

-- Clientes
INSERT INTO persona (nombre, identificacion, telefono, correo) VALUES
    ('Ana Torres',     '20000001', '3101111111', 'ana.torres@gmail.com'),
    ('Luis Herrera',   '20000002', '3112222222', 'luis.herrera@hotmail.com'),
    ('Sofia Vargas',   '20000003', '3123333333', 'sofia.vargas@gmail.com'),
    ('Diego Castro',   '20000004', '3134444444', 'diego.castro@yahoo.com'),
    ('Valentina Rios', '20000005', '3145555555', 'valentina.rios@gmail.com');

INSERT INTO cliente (id, rut) VALUES
    (4, NULL),
    (5, NULL),
    (6, '800123456-7'),
    (7, NULL),
    (8, NULL);

-- Marcas
INSERT INTO marca (nombre) VALUES
    ('Samsung'),
    ('Apple'),
    ('Xiaomi'),
    ('Motorola'),
    ('Huawei'),
    ('Google'),
    ('Realme');

-- Precios
INSERT INTO precio (nombre, valor) VALUES
    ('Precio Samsung Galaxy S24 Ultra', 4199990.00),
    ('Precio Samsung Galaxy A54',       1299990.00),
    ('Precio Apple iPhone 15 Pro',      5299990.00),
    ('Precio Apple iPhone 14',          3799990.00),
    ('Precio Xiaomi Redmi Note 13 Pro', 1199990.00),
    ('Precio Xiaomi Poco X6 Pro',       1499990.00),
    ('Precio Motorola Moto G84',         899990.00),
    ('Precio Huawei P60 Pro',           3299990.00),
    ('Precio Google Pixel 8 Pro',       4499990.00),
    ('Precio Realme 11 Pro+',            999990.00);

-- Celulares (30 unidades cada uno)
INSERT INTO celular (modelo, id_marca_fk, sistema_operativo, gama, id_precio_fk, stock) VALUES
    ('Galaxy S24 Ultra',  1, 'ANDROID', 'ALTA',  1, 30),
    ('Galaxy A54',        1, 'ANDROID', 'MEDIA', 2, 30),
    ('iPhone 15 Pro',     2, 'IOS',     'ALTA',  3, 30),
    ('iPhone 14',         2, 'IOS',     'ALTA',  4, 30),
    ('Redmi Note 13 Pro', 3, 'ANDROID', 'MEDIA', 5, 30),
    ('Poco X6 Pro',       3, 'ANDROID', 'MEDIA', 6, 30),
    ('Moto G84',          4, 'ANDROID', 'MEDIA', 7, 30),
    ('P60 Pro',           5, 'ANDROID', 'ALTA',  8, 30),
    ('Pixel 8 Pro',       6, 'ANDROID', 'ALTA',  9, 30),
    ('11 Pro+',           7, 'ANDROID', 'MEDIA',10, 30);

-- Venta 1: Ana Torres compra 1x iPhone 15 Pro
INSERT INTO venta (id_cliente_fk, fecha, total, id_empleado_fk) VALUES
    (4, '2024-03-15 10:30:00', 6306988.10, 1);

INSERT INTO detalle_venta (id_venta_fk, id_celular_fk, cantidad, subtotal) VALUES
    (1, 3, 1, 5299990.00);

UPDATE celular SET stock = 29 WHERE id = 3;

-- Venta 2: Luis Herrera compra 2x Galaxy A54 + 1x Redmi Note 13 Pro
INSERT INTO venta (id_cliente_fk, fecha, total, id_empleado_fk) VALUES
    (5, '2024-03-20 15:45:00', 4521964.30, 1);

INSERT INTO detalle_venta (id_venta_fk, id_celular_fk, cantidad, subtotal) VALUES
    (2, 2, 2, 2599980.00),
    (2, 5, 1, 1199990.00);

UPDATE celular SET stock = 28 WHERE id = 2;
UPDATE celular SET stock = 29 WHERE id = 5;

-- Revisar cuantos datos quedaron en cada tabla
SELECT 'persona'       AS tabla, COUNT(*) AS registros FROM persona;
SELECT 'empleado'      AS tabla, COUNT(*) AS registros FROM empleado;
SELECT 'cliente'       AS tabla, COUNT(*) AS registros FROM cliente;
SELECT 'marca'         AS tabla, COUNT(*) AS registros FROM marca;
SELECT 'precio'        AS tabla, COUNT(*) AS registros FROM precio;
SELECT 'celular'       AS tabla, COUNT(*) AS registros FROM celular;
SELECT 'venta'         AS tabla, COUNT(*) AS registros FROM venta;
SELECT 'detalle_venta' AS tabla, COUNT(*) AS registros FROM detalle_venta;
