# Documento de Diseño Técnico — TecnoStore Sistema de Gestión

## Overview

TecnoStore es una aplicación de consola desarrollada en **Java 21** que reemplaza el manejo manual en hojas de cálculo de una tienda minorista de celulares. El sistema permite controlar el inventario, registrar clientes, procesar ventas y generar reportes, todo desde la terminal. Los datos se almacenan en una base de datos **MySQL** usando **JDBC** sin ningún framework externo.

Este documento describe cómo está organizado el código, qué clases existen, qué hacen, cómo se relacionan entre sí y cómo fluye la información desde el menú hasta la base de datos. Está pensado para un desarrollador que está aprendiendo Java, por lo que cada decisión de diseño se explica en lenguaje simple.

**Tecnologías:**
- Java 21 (sin frameworks externos)
- MySQL 8+ con JDBC (sin ORM)
- NetBeans IDE
- jqwik (biblioteca de property-based testing sobre JUnit 5)

---

## Architecture

El sistema está dividido en **capas**. Cada capa tiene una responsabilidad clara y solo habla con la capa inmediatamente inferior o con las utilidades transversales. Esto sigue el Principio de Responsabilidad Única (SRP) de SOLID.

```
┌─────────────────────────────────────────────────────┐
│                  CAPA DE PRESENTACIÓN                │
│          Main.java  ─  Menú de consola               │
│   (lee entradas del usuario, muestra resultados)     │
└──────────────────────┬──────────────────────────────┘
                       │ llama a
┌──────────────────────▼──────────────────────────────┐
│                 CAPA DE SERVICIO                     │
│   GestorCelulares │ GestorClientes │ GestorVentas    │
│   (lógica de negocio: validaciones, cálculos, IVA)  │
└──────────────────────┬──────────────────────────────┘
                       │ llama a
┌──────────────────────▼──────────────────────────────┐
│               CAPA DE PERSISTENCIA                   │
│    CelularDAO │ ClienteDAO │ VentaDAO                │
│    ConexionDB (Singleton)                            │
│    (SQL con PreparedStatement, manejo de JDBC)       │
└──────────────────────┬──────────────────────────────┘
                       │ usa
┌──────────────────────▼──────────────────────────────┐
│                CAPA DEL MODELO                       │
│    Celular │ Cliente │ Venta │ ItemVenta             │
│    CategoriaGama (enum)                              │
│    (clases de datos puras, solo atributos y          │
│     métodos get/set)                                 │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│               CAPA DE UTILIDADES (transversal)       │
│    Validador │ ReporteUtils │ ArchivoUtils            │
│    (herramientas usadas por el servicio)             │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│               CAPA DE PATRONES (transversal)         │
│    FactoryCelular │ EstrategiaDescuento (interface)  │
│    SinDescuento │ DescuentoPorcentaje                │
│    (patrones de diseño reutilizables)                │
└─────────────────────────────────────────────────────┘
```

**¿Por qué capas?** Si mañana quieres cambiar MySQL por otro motor de base de datos, solo tocas la capa de persistencia. Si quieres cambiar cómo se calcula el IVA, solo tocas el servicio. Las capas se protegen entre sí.

### Estructura de paquetes Java

```
com.tecnostore/
├── modelo/
│   ├── Celular.java
│   ├── Cliente.java
│   ├── Venta.java
│   ├── ItemVenta.java
│   └── CategoriaGama.java          ← enum
│
├── servicio/
│   ├── GestorCelulares.java
│   ├── GestorClientes.java
│   └── GestorVentas.java
│
├── persistencia/
│   ├── ConexionDB.java              ← Singleton
│   ├── CelularDAO.java
│   ├── ClienteDAO.java
│   └── VentaDAO.java
│
├── util/
│   ├── Validador.java
│   ├── ReporteUtils.java
│   └── ArchivoUtils.java
│
├── patron/
│   ├── FactoryCelular.java          ← Factory Method
│   ├── EstrategiaDescuento.java     ← interface Strategy
│   ├── SinDescuento.java
│   └── DescuentoPorcentaje.java
│
└── Main.java                        ← punto de entrada
```

**Regla de dependencias:** `modelo` no depende de nadie. `persistencia` depende de `modelo`. `servicio` depende de `modelo` y `persistencia`. `util` y `patron` son independientes y pueden ser usados por `servicio`.

---

## Components and Interfaces

### Capa de servicio

Los gestores contienen la **lógica de negocio**. No tienen código SQL ni código de consola dentro de los métodos de cálculo. Delegan la persistencia a los DAOs y usan el Validador para verificar datos.

#### GestorCelulares

```java
package com.tecnostore.servicio;

public class GestorCelulares {

    private final CelularDAO celularDAO;

    public GestorCelulares(CelularDAO celularDAO)

    // Valida datos y guarda en BD. Lanza IllegalArgumentException si los datos son inválidos.
    public void registrarCelular(String marca, String modelo, double precio,
                                  int stock, String sistemaOperativo, CategoriaGama gama)

    // Retorna null si no existe.
    public Celular buscarPorId(int id)

    // Retorna lista vacía si no hay celulares.
    public List<Celular> listarTodos()

    // Lanza IllegalArgumentException si id no existe o datos inválidos.
    public void actualizarCelular(int id, String marca, String modelo, double precio,
                                   int stock, String sistemaOperativo, CategoriaGama gama)

    // Lanza IllegalArgumentException si id no existe.
    public void eliminarCelular(int id)
}
```

**Decisión de diseño:** `GestorCelulares` recibe el `CelularDAO` por el constructor (inyección de dependencias). Esto cumple con el Principio de Inversión de Dependencias (DIP) y facilita las pruebas.

#### GestorClientes

```java
package com.tecnostore.servicio;

public class GestorClientes {

    private final ClienteDAO clienteDAO;

    public GestorClientes(ClienteDAO clienteDAO)

    // Valida todos los campos antes de guardar.
    // El control de 3 intentos para el correo se maneja en Main.java.
    public void registrarCliente(String nombre, String identificacion,
                                  String correo, String telefono)

    // Retorna null si no existe.
    public Cliente buscarPorIdentificacion(String identificacion)

    // Retorna lista vacía si no hay clientes.
    public List<Cliente> listarTodos()
}
```

#### GestorVentas

```java
package com.tecnostore.servicio;

public class GestorVentas {

    private static final double TASA_IVA = 0.19;
    private static final AtomicInteger contadorVentasDelDia = new AtomicInteger(0);

    private final VentaDAO ventaDAO;
    private final ClienteDAO clienteDAO;
    private final CelularDAO celularDAO;
    private EstrategiaDescuento estrategiaDescuento;

    public GestorVentas(VentaDAO ventaDAO, ClienteDAO clienteDAO, CelularDAO celularDAO)

    // Cambia la estrategia de descuento en tiempo de ejecución (patrón Strategy).
    public void setEstrategiaDescuento(EstrategiaDescuento estrategia)

    // Método sincronizado. Actualiza stock dentro de una transacción atómica.
    // itemsSolicitados: cada int[] tiene [idCelular, cantidad]
    public synchronized void registrarVenta(String identificacionCliente,
                                             List<int[]> itemsSolicitados)

    // Calcula subtotal, totalSinIva, iva y totalConIva. Redondea a 2 decimales.
    public double calcularTotalConIva(List<ItemVenta> items)

    // Retorna lista vacía si no hay ventas.
    public List<Venta> listarTodas()

    public int getContadorVentasDelDia()
}
```

**Decisión de diseño:** El método `registrarVenta` es `synchronized` porque el Requerimiento 9 pide que sea thread-safe. Aunque el sistema actual es de un solo hilo, el modificador prepara el código para uso futuro con múltiples hilos.

---

### Capa de persistencia

#### ConexionDB (Singleton)

```java
package com.tecnostore.persistencia;

public class ConexionDB {

    private static ConexionDB instancia;     // la única instancia
    private Connection conexion;             // la conexión JDBC abierta

    // Constructor privado: nadie puede hacer "new ConexionDB()" desde afuera
    private ConexionDB()

    // Thread-safe: synchronized evita que dos hilos creen dos instancias al mismo tiempo
    public static synchronized ConexionDB getInstancia()

    public Connection getConexion()

    public void cerrarConexion()
}
```

Lee el archivo `config.properties` en el classpath usando `Properties` y `InputStream`:

```properties
db.host=localhost
db.puerto=3306
db.nombre=tecnostore_db
db.usuario=root
db.contrasena=tu_contrasena
```

**¿Por qué Singleton?** La conexión JDBC es costosa de abrir. Abrirla una sola vez y reutilizarla durante toda la ejecución es más eficiente y evita quedarse sin conexiones disponibles.

#### CelularDAO

```java
package com.tecnostore.persistencia;

public class CelularDAO {

    private final Connection conexion;

    public CelularDAO(Connection conexion)

    public void insertar(Celular celular) throws SQLException      // INSERT INTO celulares
    public Celular buscarPorId(int id) throws SQLException          // SELECT WHERE id = ?
    public List<Celular> listarTodos() throws SQLException          // SELECT *
    public void actualizar(Celular celular) throws SQLException     // UPDATE WHERE id = ?
    public void eliminar(int id) throws SQLException                // DELETE WHERE id = ?
}
```

#### ClienteDAO

```java
package com.tecnostore.persistencia;

public class ClienteDAO {

    private final Connection conexion;

    public ClienteDAO(Connection conexion)

    public void insertar(Cliente cliente) throws SQLException
    public Cliente buscarPorIdentificacion(String identificacion) throws SQLException
    public List<Cliente> listarTodos() throws SQLException
    public boolean existeIdentificacion(String identificacion) throws SQLException
}
```

#### VentaDAO

```java
package com.tecnostore.persistencia;

public class VentaDAO {

    private final Connection conexion;

    public VentaDAO(Connection conexion)

    // Inserta en 'ventas', en 'detalle_ventas' y actualiza stock,
    // todo dentro de una sola transacción (commit o rollback).
    public void insertarConTransaccion(Venta venta) throws SQLException

    // Trae ventas con sus ítems y clientes para los reportes.
    public List<Venta> listarTodas() throws SQLException
}
```

**¿Qué es una transacción?** Es un conjunto de operaciones que deben ocurrir todas o ninguna. En código:

```java
conexion.setAutoCommit(false);   // inicia la transacción
try {
    // insertar venta, insertar ítems, actualizar stock
    conexion.commit();            // confirma todo
} catch (SQLException e) {
    conexion.rollback();          // deshace todo
}
```

---

### Capa de utilidades

#### Validador

Clase con métodos estáticos. Centraliza todas las validaciones para no repetir código en varias partes.

```java
package com.tecnostore.util;

public class Validador {

    public static boolean esPrecioValido(double precio)         // precio > 0 y <= 999_999_999.99
    public static boolean esStockValido(int stock)              // stock >= 0 y <= 999_999
    public static boolean esCorreoValido(String correo)         // un @, punto después del @
    public static boolean esTelefonoValido(String telefono)     // 7-15 dígitos
    public static boolean esTextoValido(String texto, int max)  // no nulo, no vacío, <= max chars
    public static boolean esCantidadValida(int cantidad)        // 1 <= cantidad <= 100
}
```

#### ReporteUtils

Clase con métodos estáticos que usan **Stream API**. Recibe listas de datos ya cargadas, no hace consultas SQL.

```java
package com.tecnostore.util;

public class ReporteUtils {

    // Filtra celulares con stock < 5, ordena por stock ascendente.
    public static void mostrarStockBajo(List<Celular> celulares)

    // Top N más vendidos (N = min(3, modelos con al menos 1 venta)).
    // Empate: orden alfabético por nombre del modelo.
    public static void mostrarTopMasVendidos(List<Venta> ventas)

    // Agrupa ventas por período MM/AAAA, suma precio_unitario × cantidad,
    // ordena cronológicamente.
    public static void mostrarVentasPorMes(List<Venta> ventas)
}
```

Ejemplo de cómo se usa Stream API internamente en `mostrarStockBajo`:

```java
celulares.stream()
    .filter(c -> c.getStock() < 5)
    .sorted(Comparator.comparingInt(Celular::getStock))
    .forEach(c -> System.out.println(c));
```

#### ArchivoUtils

```java
package com.tecnostore.util;

public class ArchivoUtils {

    private static final String NOMBRE_ARCHIVO = "reporte_ventas.txt";

    // Crea o sobreescribe "reporte_ventas.txt" en el directorio de trabajo.
    // Usa try-with-resources para garantizar el cierre del archivo.
    // Si no hay ventas, escribe "No hay ventas registradas".
    // Muestra la ruta absoluta del archivo generado.
    public static void generarReporte(List<Venta> ventas)
}
```

---

### Capa de patrones

#### FactoryCelular (Patrón Factory Method)

**¿Qué problema resuelve?** Centraliza la construcción de objetos `Celular`. En lugar de escribir `new Celular(...)` en diez lugares diferentes, siempre se llama a `FactoryCelular.crear(...)`. Si la forma de construir un Celular cambia, solo se modifica en un lugar.

```java
package com.tecnostore.patron;

public class FactoryCelular {

    // Método estático que recibe los datos y retorna un Celular construido.
    // Aplica validaciones básicas antes de retornar.
    public static Celular crear(String marca, String modelo, double precio,
                                 int stock, String sistemaOperativo, CategoriaGama gama)
}
```

Flujo de uso:

```
GestorCelulares.registrarCelular(datos)
        │
        ▼
FactoryCelular.crear(datos)   ← construye el objeto Celular
        │
        ▼
CelularDAO.insertar(celular)  ← lo guarda en la base de datos
```

#### EstrategiaDescuento (Patrón Strategy)

**¿Qué problema resuelve?** El cálculo del precio final puede variar. El patrón Strategy permite cambiar el algoritmo de cálculo sin modificar `GestorVentas`.

```java
package com.tecnostore.patron;

// Interfaz (contrato que deben cumplir todas las estrategias)
public interface EstrategiaDescuento {
    double aplicar(double totalSinIva);
}

// Implementación 1: sin descuento
public class SinDescuento implements EstrategiaDescuento {
    public double aplicar(double totalSinIva)  // retorna totalSinIva sin cambios
}

// Implementación 2: descuento por porcentaje
public class DescuentoPorcentaje implements EstrategiaDescuento {
    private final double porcentaje;   // por ejemplo, 0.10 para 10%
    public DescuentoPorcentaje(double porcentaje)
    public double aplicar(double totalSinIva)  // retorna totalSinIva * (1 - porcentaje)
}
```

Uso en `GestorVentas`:

```java
// Sin descuento (comportamiento por defecto)
gestor.setEstrategiaDescuento(new SinDescuento());

// Con 10% de descuento
gestor.setEstrategiaDescuento(new DescuentoPorcentaje(0.10));
```

---

## Data Models

### CategoriaGama (enum)

```java
package com.tecnostore.modelo;

public enum CategoriaGama {
    ALTA,
    MEDIA,
    BAJA
}
```

Un enum es como una lista de opciones fijas. Usar un enum evita errores por escribir mal el texto (por ejemplo, "alta" vs "ALTA" vs "Alta").

---

### Celular

Representa un teléfono en el inventario.

| Atributo           | Tipo Java       | Restricción                                    |
|--------------------|-----------------|------------------------------------------------|
| `id`               | `int`           | Asignado por la base de datos (auto-increment) |
| `marca`            | `String`        | Entre 1 y 100 caracteres                       |
| `modelo`           | `String`        | Entre 1 y 100 caracteres                       |
| `precio`           | `double`        | Entre 0.01 y 999,999,999.99                    |
| `stock`            | `int`           | Entre 0 y 999,999                              |
| `sistemaOperativo` | `String`        | No vacío                                       |
| `gama`             | `CategoriaGama` | Valor del enum                                 |

```java
// Constructor completo (para reconstruir desde la base de datos)
public Celular(int id, String marca, String modelo, double precio,
               int stock, String sistemaOperativo, CategoriaGama gama)

// Constructor sin id (para crear uno nuevo antes de guardar)
public Celular(String marca, String modelo, double precio,
               int stock, String sistemaOperativo, CategoriaGama gama)

// Getters y setters para todos los atributos
public int getId()
public String getMarca()
public String getModelo()
public double getPrecio()
public int getStock()
public String getSistemaOperativo()
public CategoriaGama getGama()

public void setMarca(String marca)
public void setModelo(String modelo)
public void setPrecio(double precio)
public void setStock(int stock)
public void setSistemaOperativo(String sistemaOperativo)
public void setGama(CategoriaGama gama)

public String toString()
```

---

### Cliente

Representa una persona que compra en la tienda.

| Atributo         | Tipo Java | Restricción                                  |
|------------------|-----------|----------------------------------------------|
| `id`             | `int`     | Asignado por la base de datos                |
| `nombre`         | `String`  | Entre 1 y 100 caracteres                     |
| `identificacion` | `String`  | Entre 1 y 20 caracteres, único en el sistema |
| `correo`         | `String`  | Formato válido con @ y punto                 |
| `telefono`       | `String`  | Entre 7 y 15 dígitos                         |

```java
public Cliente(int id, String nombre, String identificacion,
               String correo, String telefono)

public Cliente(String nombre, String identificacion,
               String correo, String telefono)

// Getters y setters para todos los atributos
public int getId()
public String getNombre()
public String getIdentificacion()
public String getCorreo()
public String getTelefono()

public void setNombre(String nombre)
public void setIdentificacion(String identificacion)
public void setCorreo(String correo)
public void setTelefono(String telefono)

public String toString()
```

---

### ItemVenta

Representa una línea dentro de una venta: un celular específico con su cantidad y precio al momento de la compra.

| Atributo          | Tipo Java | Restricción                          |
|-------------------|-----------|--------------------------------------|
| `id`              | `int`     | Asignado por la base de datos        |
| `celular`         | `Celular` | No nulo                              |
| `cantidad`        | `int`     | Entre 1 y 100                        |
| `precioUnitario`  | `double`  | Precio del celular en el momento     |
| `subtotal`        | `double`  | Calculado: cantidad × precioUnitario |

> **¿Por qué guardar `precioUnitario`?** El precio de un celular puede cambiar después de la venta. Guardamos el precio en el momento exacto de la compra para que el historial sea correcto.

```java
public ItemVenta(Celular celular, int cantidad, double precioUnitario)

public ItemVenta(int id, Celular celular, int cantidad, double precioUnitario)

public int getId()
public Celular getCelular()
public int getCantidad()
public double getPrecioUnitario()
public double getSubtotal()         // retorna cantidad * precioUnitario, redondeado a 2 dec.

public void setCelular(Celular celular)
public void setCantidad(int cantidad)
public void setPrecioUnitario(double precioUnitario)

public String toString()
```

---

### Venta

Representa una transacción completa de compra.

| Atributo       | Tipo Java         | Restricción                           |
|----------------|-------------------|---------------------------------------|
| `id`           | `int`             | Asignado por la base de datos         |
| `cliente`      | `Cliente`         | No nulo                               |
| `items`        | `List<ItemVenta>` | Al menos un elemento                  |
| `fecha`        | `LocalDateTime`   | Fecha y hora del sistema al crear     |
| `totalSinIva`  | `double`          | Suma de todos los subtotales          |
| `iva`          | `double`          | 19% del totalSinIva                   |
| `totalConIva`  | `double`          | totalSinIva + iva                     |

```java
public Venta(Cliente cliente, List<ItemVenta> items,
             double totalSinIva, double iva, double totalConIva)

public Venta(int id, Cliente cliente, List<ItemVenta> items,
             LocalDateTime fecha, double totalSinIva, double iva, double totalConIva)

public int getId()
public Cliente getCliente()
public List<ItemVenta> getItems()
public LocalDateTime getFecha()
public double getTotalSinIva()
public double getIva()
public double getTotalConIva()

public void agregarItem(ItemVenta item)
public String toString()
```

### Diagrama de base de datos MySQL — tecnostore_db

```sql
CREATE DATABASE IF NOT EXISTS tecnostore_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE tecnostore_db;

CREATE TABLE IF NOT EXISTS celulares (
    id                INT              NOT NULL AUTO_INCREMENT,
    marca             VARCHAR(100)     NOT NULL,
    modelo            VARCHAR(100)     NOT NULL,
    precio            DECIMAL(15, 2)   NOT NULL,
    stock             INT              NOT NULL DEFAULT 0,
    sistema_operativo VARCHAR(100)     NOT NULL,
    gama              ENUM('ALTA','MEDIA','BAJA') NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS clientes (
    id                INT              NOT NULL AUTO_INCREMENT,
    nombre            VARCHAR(100)     NOT NULL,
    identificacion    VARCHAR(20)      NOT NULL,
    correo            VARCHAR(255)     NOT NULL,
    telefono          VARCHAR(15)      NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_identificacion (identificacion)
);

CREATE TABLE IF NOT EXISTS ventas (
    id                INT              NOT NULL AUTO_INCREMENT,
    id_cliente        INT              NOT NULL,
    fecha             DATETIME         NOT NULL,
    total_sin_iva     DECIMAL(15, 2)   NOT NULL,
    iva               DECIMAL(15, 2)   NOT NULL,
    total_con_iva     DECIMAL(15, 2)   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_venta_cliente
        FOREIGN KEY (id_cliente) REFERENCES clientes (id)
);

CREATE TABLE IF NOT EXISTS detalle_ventas (
    id                INT              NOT NULL AUTO_INCREMENT,
    id_venta          INT              NOT NULL,
    id_celular        INT              NOT NULL,
    cantidad          INT              NOT NULL,
    precio_unitario   DECIMAL(15, 2)   NOT NULL,
    subtotal          DECIMAL(15, 2)   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_detalle_venta
        FOREIGN KEY (id_venta) REFERENCES ventas (id),
    CONSTRAINT fk_detalle_celular
        FOREIGN KEY (id_celular) REFERENCES celulares (id)
);
```

**Relaciones:**

```
clientes          ventas              detalle_ventas      celulares
─────────         ──────────          ──────────────      ─────────
id ◄──────────── id_cliente           id                  id ◄────┐
nombre            id ◄──────────────► id_venta            marca   │
identificacion    fecha               id_celular ─────────────────┘
correo            total_sin_iva       cantidad
telefono          iva                 precio_unitario
                  total_con_iva       subtotal
```

- Un cliente puede tener muchas ventas (1:N)
- Una venta puede tener muchos ítems de detalle (1:N)
- Un celular puede aparecer en muchos ítems de detalle (1:N)

### Flujo principal de ejecución — proceso de una venta

Este flujo muestra exactamente qué pasa en el código cuando el empleado registra una venta, desde que escribe en la consola hasta que los datos quedan en la base de datos.

```
[CONSOLA]                [Main.java]       [GestorVentas]      [DAOs/BD]
    │                        │                   │                  │
    │── "3. Ventas" ────────►│                   │                  │
    │◄── "Ingrese ID:" ──────│                   │                  │
    │── "123456" ───────────►│                   │                  │
    │                        │── registrar() ───►│                  │
    │                        │                   │── buscarCliente ─►│
    │                        │                   │◄─ Cliente obj. ───│
    │◄── "Agregue celular:" ─│                   │                  │
    │── id + cantidad ──────►│── agrega item ───►│── verificar stock►│
    │                        │                   │◄─ stock ok ───────│
    │◄── "¿Confirmar? S/N" ──│                   │                  │
    │── "S" ────────────────►│── confirmar() ───►│                  │
    │                        │                   │── calcular IVA   │
    │                        │                   │                  │
    │                        │                   │── BEGIN TX ──────►│
    │                        │                   │── INSERT ventas ──►│
    │                        │                   │── INSERT detalle ─►│
    │                        │                   │── UPDATE stock ───►│
    │                        │                   │── COMMIT ─────────►│
    │◄── Comprobante ────────│                   │                  │
```

**Pasos detallados:**

1. `Main.java` muestra el menú y lee la opción del usuario con `Scanner`.
2. Si elige "Ventas", llama a `GestorVentas.registrarVenta(identificacion, items)`.
3. `GestorVentas` llama a `ClienteDAO.buscarPorIdentificacion()` para verificar que el cliente existe.
4. Por cada celular agregado, llama a `CelularDAO.buscarPorId()` y verifica el stock disponible.
5. Calcula subtotales, total sin IVA, IVA (19%) y total con IVA usando `Math.round(valor * 100.0) / 100.0`.
6. Llama a `VentaDAO.insertarConTransaccion(venta)` que ejecuta INSERT y UPDATE dentro de una transacción.
7. Si todo sale bien: `commit()` y muestra el comprobante en consola.
8. Si algo falla: `rollback()` y muestra el mensaje de error.

---

## Correctness Properties

*Una propiedad es una característica o comportamiento que debe ser verdadero en todas las ejecuciones válidas del sistema. Las propiedades son la forma de expresar formalmente qué debe hacer el software y sirven como base para las pruebas automáticas.*

### Property 1: Validez del precio de un celular

*Para cualquier* celular registrado exitosamente en el sistema, su precio debe ser siempre mayor que cero y menor o igual a 999,999,999.99.

**Validates: Requirements 2.2, 2.4**

---

### Property 2: Validez del stock de un celular

*Para cualquier* celular registrado exitosamente en el sistema, su stock debe ser siempre mayor o igual a cero y menor o igual a 999,999.

**Validates: Requirements 2.2, 2.4**

---

### Property 3: Unicidad de identificación de cliente

*Para cualquier* par de clientes registrados en el sistema, sus números de identificación deben ser distintos entre sí; intentar registrar un segundo cliente con la misma identificación debe ser rechazado.

**Validates: Requirements 3.4**

---

### Property 4: Cálculo correcto del IVA

*Para cualquier* lista de ítems de venta con precios y cantidades válidos, el campo `totalConIva` del resultado debe ser exactamente igual a `round(totalSinIva * 1.19, 2)`, donde `round(x, 2)` redondea a exactamente 2 decimales.

**Validates: Requirements 4.5**

---

### Property 5: Consistencia del subtotal y reducción de stock

*Para cualquier* ítem de venta, el `subtotal` debe ser igual a `round(precioUnitario * cantidad, 2)`. Además, *para cualquier* celular incluido en una venta confirmada exitosamente, su stock después de la venta debe ser exactamente igual al stock antes de la venta menos la cantidad vendida.

**Validates: Requirements 4.5, 4.6**

---

### Property 6: Atomicidad de la transacción de venta

*Para cualquier* intento de registrar una venta que falle en algún paso intermedio (al insertar un ítem o al actualizar el stock), ningún cambio debe quedar persistido en la base de datos; el estado completo del sistema debe ser idéntico al estado anterior al intento.

**Validates: Requirements 4.7**

---

### Property 7: Validación correcta del correo electrónico

*Para cualquier* cadena de texto que contenga exactamente un "@", al menos un carácter antes del "@", y al menos un "." después del "@" con caracteres en ambos lados, `Validador.esCorreoValido()` debe retornar `true`. *Para cualquier* cadena que no cumpla estas condiciones, debe retornar `false`.

**Validates: Requirements 3.3**

---

### Property 8: Filtro de stock bajo en reportes

*Para cualquier* lista de celulares, el reporte de stock bajo debe contener exactamente todos los celulares con stock estrictamente menor a 5, ordenados por stock ascendente, y no debe contener ningún celular con stock mayor o igual a 5.

**Validates: Requirements 5.1**

---

### Property 9: Completitud y corrección del archivo de reporte

*Para cualquier* lista de ventas no vacía, el archivo `reporte_ventas.txt` generado debe contener exactamente una línea de resumen por cada venta de la lista (con nombre del cliente y total con IVA con 2 decimales), la fecha de generación y el gran total calculado correctamente.

**Validates: Requirements 6.1, 6.2**

---

## Error Handling

| Excepción                  | Dónde ocurre                           | Quién la captura                            | Acción al capturar                                                         |
|----------------------------|----------------------------------------|---------------------------------------------|----------------------------------------------------------------------------|
| `SQLException`             | Métodos de todos los DAOs              | `GestorCelulares`, `GestorClientes`, `GestorVentas` | Muestra "Error al acceder a la base de datos" en consola                |
| `SQLException` en TX       | `VentaDAO.insertarConTransaccion`      | `GestorVentas`                              | Ejecuta `rollback()`, muestra "Error al registrar la venta, no se realizaron cambios" |
| `IOException`              | `ArchivoUtils.generarReporte`          | El propio `ArchivoUtils`                    | Muestra "No se pudo generar el archivo de reporte", imprime traza en `System.err` |
| `SQLException` al iniciar  | `ConexionDB.getInstancia()`            | `Main.java`                                 | Muestra mensaje con la causa y termina con `System.exit(1)`                |
| `IllegalArgumentException` | Gestores (validación de negocio)       | `Main.java`                                 | Muestra el mensaje de la excepción y repite la solicitud de datos           |
| `NumberFormatException`    | `Main.java` al leer enteros del Scanner| `Main.java`                                 | Muestra "Debe ingresar un número válido" y repite                           |

**Regla general:** Las capas bajas (DAO) lanzan excepciones. Las capas altas (servicio, Main) las capturan y las convierten en mensajes amigables para el usuario. Nunca se muestra un stack trace al usuario final; solo se muestra en `System.err` para el desarrollador.

---

## Testing Strategy

### Pruebas basadas en propiedades (Property-Based Testing)

Para este sistema se usará **jqwik** como biblioteca de property-based testing, compatible con JUnit 5 en Java 21. Cada propiedad de corrección listada arriba se implementa como una prueba automática que genera muchas entradas aleatorias (mínimo **100 iteraciones** por propiedad) y verifica que la propiedad se cumpla en todos los casos.

**Etiqueta de referencia por prueba:**

```
Feature: tecnostore-sistema-gestion, Property {número}: {texto de la propiedad}
```

Ejemplo de implementación para la Propiedad 4 (cálculo del IVA):

```java
// Feature: tecnostore-sistema-gestion, Property 4: Cálculo correcto del IVA
@Property
void calculoIvaEsCorrecto(
        @ForAll @DoubleRange(min = 0.01, max = 999999.99) double precioUnitario,
        @ForAll @IntRange(min = 1, max = 100) int cantidad) {

    double subtotal = Math.round(precioUnitario * cantidad * 100.0) / 100.0;
    double totalConIva = Math.round(subtotal * 1.19 * 100.0) / 100.0;

    ItemVenta item = new ItemVenta(new Celular(...), cantidad, precioUnitario);
    // ... armar la venta y verificar que totalConIva coincide
    assertThat(resultado.getTotalConIva()).isEqualTo(totalConIva);
}
```

### Pruebas unitarias con ejemplos concretos

Complementan las pruebas de propiedades con casos específicos y casos límite:

- **Validador:** correos válidos e inválidos concretos, precios límite exactos (0.01, 0.00, 999,999,999.99, 1,000,000,000.00)
- **ReporteUtils:** listas con exactamente 3, 2, 1 y 0 celulares vendidos para verificar el comportamiento del top N; lista vacía para verificar el mensaje de "No hay datos disponibles"
- **ArchivoUtils:** verificar que el archivo se crea, que su contenido coincide con las ventas de prueba, y que el comportamiento con lista vacía genera el mensaje correcto
- **GestorVentas:** stock insuficiente, cliente inexistente, intento de cantidad fuera de rango
- **ConexionDB:** verificar que dos llamadas a `getInstancia()` retornan la misma referencia (Singleton)

### Pruebas de integración

Se ejecutan contra una base de datos MySQL de prueba separada (`tecnostore_test_db`):

- `CelularDAO`, `ClienteDAO` y `VentaDAO` ejecutan correctamente operaciones CRUD contra la BD
- La transacción ejecuta `rollback` correctamente cuando se fuerza un error en el segundo INSERT
- El Singleton de `ConexionDB` retorna siempre la misma instancia incluso con múltiples llamadas

### Equilibrio entre tipos de prueba

Las pruebas de propiedades cubren el espacio de entradas de forma amplia (100+ combinaciones). Las pruebas unitarias cubren los casos límite exactos que son difíciles de generar aleatoriamente. Las pruebas de integración verifican que las piezas encajan correctamente con la base de datos real. Los tres tipos son complementarios y necesarios.
