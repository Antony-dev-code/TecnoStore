# Documento de Diseño Técnico — TecnoStore Sistema de Gestión

## Overview

TecnoStore es una aplicación de consola desarrollada en **Java 21** que reemplaza el manejo manual en hojas de cálculo de una tienda minorista de celulares. El sistema permite controlar el inventario, registrar clientes, procesar ventas y generar reportes, todo desde la terminal. Los datos se almacenan en una base de datos **MySQL** llamada `TecnoStoreAnt` usando **JDBC** sin ningún framework externo.

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
│  PersonaDAO │ ClienteDAO │ EmpleadoDAO               │
│  MarcaDAO │ PrecioDAO │ CelularDAO │ VentaDAO        │
│  ConexionDB (Singleton, thread-safe)                 │
│  (SQL con PreparedStatement, manejo de JDBC)         │
└──────────────────────┬──────────────────────────────┘
                       │ usa
┌──────────────────────▼──────────────────────────────┐
│                CAPA DEL MODELO                       │
│  Persona │ Cliente │ Empleado                        │
│  Marca │ Precio │ Celular │ Venta │ ItemVenta        │
│  CategoriaGama │ SistemaOperativo │ TipoEmpleado     │
│  (clases de datos puras, solo atributos y get/set)  │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│            CAPA DE UTILIDADES (transversal)          │
│  Validador │ ReporteUtils │ ArchivoUtils             │
│  ConsolaUtils                                        │
│  (herramientas usadas por el servicio y Main)        │
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
│   ├── Persona.java                 ← clase base (abstracta)
│   ├── Cliente.java                 ← extiende Persona
│   ├── Empleado.java                ← extiende Persona
│   ├── Marca.java
│   ├── Precio.java
│   ├── Celular.java
│   ├── Venta.java
│   ├── ItemVenta.java
│   ├── CategoriaGama.java           ← enum: ALTA, MEDIA, BAJA
│   ├── SistemaOperativo.java        ← enum: ANDROID, IOS
│   └── TipoEmpleado.java            ← enum: CAJERO, BODEGUERO, ADMINISTRADOR
│
├── servicio/
│   ├── GestorCelulares.java
│   ├── GestorClientes.java
│   └── GestorVentas.java
│
├── persistencia/
│   ├── ConexionDB.java              ← Singleton thread-safe
│   ├── PersonaDAO.java
│   ├── ClienteDAO.java
│   ├── EmpleadoDAO.java
│   ├── MarcaDAO.java
│   ├── PrecioDAO.java
│   ├── CelularDAO.java
│   └── VentaDAO.java
│
├── util/
│   ├── Validador.java
│   ├── ReporteUtils.java
│   ├── ArchivoUtils.java
│   └── ConsolaUtils.java
│
├── patron/
│   ├── FactoryCelular.java          ← Factory Method
│   ├── EstrategiaDescuento.java     ← interface Strategy
│   ├── SinDescuento.java
│   └── DescuentoPorcentaje.java
│
└── Main.java                        ← punto de entrada
```

**Regla de dependencias:** `modelo` no depende de nadie. `persistencia` depende de `modelo`. `servicio` depende de `modelo` y `persistencia`. `util` y `patron` son independientes y pueden ser usados por `servicio` y `Main`.

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
    // Usa FactoryCelular.crear(...) internamente para construir el objeto.
    public void registrarCelular(String modelo, Marca marca, double precio,
                                  int stock, SistemaOperativo so, CategoriaGama gama)

    // Retorna null si no existe.
    public Celular buscarPorId(int id)

    // Retorna lista vacía si no hay celulares.
    public List<Celular> listarTodos()

    // Lanza IllegalArgumentException si el id no existe o los datos son inválidos.
    public void actualizarCelular(int id, String modelo, Marca marca, double precio,
                                   int stock, SistemaOperativo so, CategoriaGama gama)

    // Lanza IllegalArgumentException si el id no existe.
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

    // Map que lleva el stock comprometido por celular durante la sesión de venta actual.
    // Clave: id del celular. Valor: cantidad ya acumulada en esta venta.
    private Map<Integer, Integer> stockComprometido;

    public GestorVentas(VentaDAO ventaDAO, ClienteDAO clienteDAO, CelularDAO celularDAO)

    // Cambia la estrategia de descuento en tiempo de ejecución (patrón Strategy).
    public void setEstrategiaDescuento(EstrategiaDescuento estrategia)

    // Inicia una nueva sesión de venta: limpia el stockComprometido.
    public void iniciarSesionVenta()

    // Verifica que haya stock disponible considerando el stockComprometido.
    // stockDisponible - stockComprometido[idCelular] >= cantidadSolicitada
    // Si no alcanza, lanza IllegalStateException con mensaje detallado.
    public void agregarItem(int idCelular, int cantidad)

    // Método synchronized. Realiza doble verificación contra la BD antes del commit.
    // Si la segunda verificación falla, ejecuta rollback completo.
    // itemsSolicitados: cada int[] tiene [idCelular, cantidad]
    public synchronized void confirmarVenta(String identificacionCliente,
                                             List<int[]> itemsSolicitados,
                                             Empleado empleado)

    // Calcula subtotal, totalSinIva, iva y totalConIva. Redondea a 2 decimales.
    public double calcularTotalConIva(List<ItemVenta> items)

    // Retorna lista vacía si no hay ventas.
    public List<Venta> listarTodas()

    public int getContadorVentasDelDia()
}
```

**Decisión de diseño sobre stockComprometido:** Durante una sesión de venta, el empleado puede agregar varios celulares antes de confirmar. El sistema necesita saber cuánto stock ya está "reservado" para esa venta sin haberlo descontado todavía de la base de datos. El `Map<Integer, Integer> stockComprometido` cumple ese rol: clave = id del celular, valor = cantidad acumulada en esta venta. Cuando el empleado confirma, se hace una segunda verificación contra la BD (por si otro proceso modificó el stock mientras tanto) y recién entonces se ejecuta el `commit`.

**Decisión de diseño:** El método `confirmarVenta` es `synchronized` porque el Requerimiento 9 pide que sea thread-safe. Aunque el sistema actual es de un solo hilo, el modificador prepara el código para uso futuro con múltiples hilos.

---

### Capa de persistencia

#### ConexionDB (Singleton)

```java
package com.tecnostore.persistencia;

public class ConexionDB {

    private static ConexionDB instancia;     // la única instancia
    private Connection conexion;             // la conexión JDBC abierta

    // Constructor privado: nadie puede hacer "new ConexionDB()" desde afuera.
    private ConexionDB()

    // Thread-safe: synchronized evita que dos hilos creen dos instancias al mismo tiempo.
    public static synchronized ConexionDB getInstancia()

    public Connection getConexion()

    public void cerrarConexion()
}
```

Lee el archivo `config.properties` en el classpath usando `Properties` y `InputStream`:

```properties
db.host=localhost
db.puerto=3306
db.nombre=TecnoStoreAnt
db.usuario=root
db.contrasena=tu_contrasena
```

**¿Por qué Singleton?** La conexión JDBC es costosa de abrir. Abrirla una sola vez y reutilizarla durante toda la ejecución es más eficiente y evita quedarse sin conexiones disponibles.

#### PersonaDAO

```java
package com.tecnostore.persistencia;

public class PersonaDAO {

    private final Connection conexion;

    public PersonaDAO(Connection conexion)

    // INSERT INTO persona. Retorna el id generado por AUTO_INCREMENT.
    public int insertar(Persona persona) throws SQLException

    // SELECT * FROM persona WHERE id = ?
    public Persona buscarPorId(int id) throws SQLException

    // SELECT * FROM persona WHERE identificacion = ?
    public Persona buscarPorIdentificacion(String identificacion) throws SQLException
}
```

**¿Por qué PersonaDAO?** La base de datos tiene una tabla `persona` de la que heredan `cliente` y `empleado`. Cuando se registra un cliente, primero se inserta en `persona` para obtener el id, y luego se inserta en `cliente` con ese mismo id. `PersonaDAO` centraliza esa operación repetida.

#### ClienteDAO

```java
package com.tecnostore.persistencia;

public class ClienteDAO {

    private final Connection conexion;
    private final PersonaDAO personaDAO;

    public ClienteDAO(Connection conexion)

    // Llama a PersonaDAO.insertar() primero, luego INSERT INTO cliente con el id obtenido.
    public void insertar(Cliente cliente) throws SQLException

    // JOIN entre persona y cliente WHERE identificacion = ?
    public Cliente buscarPorIdentificacion(String identificacion) throws SQLException

    // JOIN entre persona y cliente, trae todos los registros.
    public List<Cliente> listarTodos() throws SQLException

    // Verifica existencia sin cargar el objeto completo.
    public boolean existeIdentificacion(String identificacion) throws SQLException
}
```

#### EmpleadoDAO

```java
package com.tecnostore.persistencia;

public class EmpleadoDAO {

    private final Connection conexion;
    private final PersonaDAO personaDAO;

    public EmpleadoDAO(Connection conexion)

    // Llama a PersonaDAO.insertar() primero, luego INSERT INTO empleado.
    public void insertar(Empleado empleado) throws SQLException

    // JOIN entre persona y empleado WHERE persona.id = ?
    public Empleado buscarPorId(int id) throws SQLException

    // JOIN entre persona y empleado, trae todos.
    public List<Empleado> listarTodos() throws SQLException
}
```

#### MarcaDAO

```java
package com.tecnostore.persistencia;

public class MarcaDAO {

    private final Connection conexion;

    public MarcaDAO(Connection conexion)

    public void insertar(Marca marca) throws SQLException           // INSERT INTO marca
    public List<Marca> listarTodas() throws SQLException           // SELECT * FROM marca
    public Marca buscarPorId(int id) throws SQLException           // SELECT WHERE id = ?
    public Marca buscarPorNombre(String nombre) throws SQLException // SELECT WHERE nombre = ?
}
```

#### PrecioDAO

```java
package com.tecnostore.persistencia;

public class PrecioDAO {

    private final Connection conexion;

    public PrecioDAO(Connection conexion)

    public void insertar(Precio precio) throws SQLException        // INSERT INTO precio
    public List<Precio> listarTodos() throws SQLException          // SELECT * FROM precio
    public Precio buscarPorId(int id) throws SQLException          // SELECT WHERE id = ?
}
```

#### CelularDAO

```java
package com.tecnostore.persistencia;

public class CelularDAO {

    private final Connection conexion;

    public CelularDAO(Connection conexion)

    // INSERT INTO celular usando id_marca_fk e id_precio_fk.
    public void insertar(Celular celular) throws SQLException

    // SELECT con JOIN a marca y precio WHERE celular.id = ?
    public Celular buscarPorId(int id) throws SQLException

    // SELECT con JOIN a marca y precio, trae todos.
    public List<Celular> listarTodos() throws SQLException

    // UPDATE celular SET ... WHERE id = ?
    public void actualizar(Celular celular) throws SQLException

    // DELETE FROM celular WHERE id = ?
    public void eliminar(int id) throws SQLException

    // UPDATE celular SET stock = stock - cantidad WHERE id = ? AND stock >= cantidad
    // Lanza SQLException si el stock resultante sería negativo.
    public void actualizarStock(int idCelular, int cantidad) throws SQLException
}
```

#### VentaDAO

```java
package com.tecnostore.persistencia;

public class VentaDAO {

    private final Connection conexion;

    public VentaDAO(Connection conexion)

    // Inserta en 'venta', en 'detalle_venta' y llama a CelularDAO.actualizarStock()
    // para cada ítem, todo dentro de una sola transacción (commit o rollback).
    // Antes del commit realiza doble verificación de stock contra la BD.
    public void insertarConTransaccion(Venta venta) throws SQLException

    // SELECT con JOINs a cliente, empleado y detalle_venta. Trae ventas con sus ítems.
    public List<Venta> listarTodas() throws SQLException
}
```

**¿Qué es una transacción?** Es un conjunto de operaciones que deben ocurrir todas o ninguna. En código:

```java
conexion.setAutoCommit(false);   // inicia la transacción
try {
    // insertar venta, insertar ítems, actualizar stock...
    conexion.commit();            // confirma todo
} catch (SQLException e) {
    conexion.rollback();          // deshace todo si algo falló
}
```

---

### Capa de utilidades

#### Validador

Clase con métodos estáticos. Centraliza todas las validaciones para no repetir código en varios lugares.

```java
package com.tecnostore.util;

public class Validador {

    public static boolean esPrecioValido(double precio)
    // precio > 0 y precio <= 999_999_999.99

    public static boolean esStockValido(int stock)
    // stock >= 0 y stock <= 999_999

    public static boolean esCorreoValido(String correo)
    // contiene exactamente un "@", al menos un char antes del "@",
    // y al menos un "." después del "@" con chars en ambos lados

    public static boolean esTelefonoValido(String telefono)
    // entre 7 y 15 dígitos (solo dígitos)

    public static boolean esTextoValido(String texto, int max)
    // no nulo, no vacío (sin espacios), longitud <= max

    public static boolean esCantidadValida(int cantidad)
    // 1 <= cantidad <= 100
}
```

#### ReporteUtils

Clase con métodos estáticos que usan **Stream API**. Recibe listas de datos ya cargadas, no hace consultas SQL.

```java
package com.tecnostore.util;

public class ReporteUtils {

    // Filtra celulares con stock < 5, ordena por stock ascendente.
    // Si la lista está vacía o ninguno cumple, muestra "No hay datos disponibles".
    public static void mostrarStockBajo(List<Celular> celulares)

    // Top N más vendidos (N = min(3, modelos con al menos 1 venta)).
    // Empate en unidades: orden alfabético por nombre del modelo.
    public static void mostrarTopMasVendidos(List<Venta> ventas)

    // Agrupa ventas por período MM/AAAA, suma precio_unitario × cantidad por período,
    // ordena cronológicamente de más antiguo a más reciente.
    public static void mostrarVentasPorMes(List<Venta> ventas)
}
```

Ejemplo de uso de Stream API en `mostrarStockBajo`:

```java
celulares.stream()
    .filter(c -> c.getStock() < 5)
    .sorted(Comparator.comparingInt(Celular::getStock))
    .forEach(c -> ConsolaUtils.imprimirFila(
        String.valueOf(c.getId()),
        c.getMarca().getNombre() + " " + c.getModelo(),
        String.valueOf(c.getStock())
    ));
```

#### ArchivoUtils

```java
package com.tecnostore.util;

public class ArchivoUtils {

    private static final String NOMBRE_ARCHIVO = "reporte_ventas.dat";

    // Crea o sobreescribe "reporte_ventas.dat" en el directorio de trabajo actual.
    // Usa ObjectOutputStream dentro de try-with-resources para garantizar el cierre.
    // Si la lista está vacía, serializa una lista vacía y muestra aviso.
    // Al terminar exitosamente, muestra la ruta absoluta del archivo generado.
    public static void generarReporte(List<Venta> ventas)

    // Lee y deserializa "reporte_ventas.dat" con ObjectInputStream en try-with-resources.
    // Retorna la lista de Venta leída para que Main.java la muestre en consola.
    // Si el archivo no existe o hay error, muestra "No se pudo acceder al archivo de reporte".
    public static List<Venta> leerReporte()
}
```

**¿Por qué .dat y no .txt?** El sistema guarda los objetos Java completos (no texto plano) usando serialización binaria. Esto es más rápido de leer, preserva exactamente los tipos de datos y no requiere parseo manual. La desventaja es que el archivo no se puede abrir directamente con un editor de texto.

#### ConsolaUtils

Nueva clase utilitaria que centraliza todo el formato visual de la consola. De esta forma, si el día de mañana quieres cambiar los bordes de `═══` a `***`, solo modificas esta clase.

```java
package com.tecnostore.util;

public class ConsolaUtils {

    private static final int ANCHO = 60; // ancho estándar de las cajas

    // Imprime el título enmarcado con ═══ y centrado.
    // Ejemplo:
    // ══════════════════════════════════════
    //            GESTIÓN DE CELULARES
    // ══════════════════════════════════════
    public static void imprimirTitulo(String titulo)

    // Imprime una línea separadora de ───
    public static void imprimirLinea()

    // Imprime la fila de encabezado de una tabla con | como separador.
    // Ejemplo: | ID  | MARCA       | MODELO          | PRECIO   |
    public static void imprimirTablaEncabezado(String... columnas)

    // Imprime una fila de datos con | como separador y String.format() para alinear.
    // Ejemplo: | 1   | Samsung     | Galaxy S24 Ultra | 4.199.990 |
    public static void imprimirFila(String... valores)

    // Imprime el comprobante completo de una venta con bordes, secciones y totales.
    // Incluye: datos del cliente, lista de ítems (celular, cantidad, subtotal),
    // total sin IVA, valor del IVA y total con IVA, alineados a la derecha.
    public static void imprimirComprobante(Venta venta)
}
```

---

### Capa de patrones

#### FactoryCelular (Patrón Factory — RECOMENDADO)

**¿Qué problema resuelve?** Centraliza la construcción de objetos `Celular`. En lugar de escribir `new Celular(...)` en diez lugares diferentes, siempre se llama a `FactoryCelular.crear(...)`. Si la forma de construir un Celular cambia, solo se modifica en un lugar.

```java
package com.tecnostore.patron;

public class FactoryCelular {

    // Método estático que recibe los datos y retorna un Celular construido.
    // Aplica validaciones básicas antes de retornar.
    // Lanza IllegalArgumentException si los datos son inválidos.
    public static Celular crear(String modelo, Marca marca, double precio,
                                 int stock, SistemaOperativo so, CategoriaGama gama)
}
```

Flujo de uso:

```
GestorCelulares.registrarCelular(datos)
        │
        ▼
FactoryCelular.crear(datos)    ← construye el objeto Celular
        │
        ▼
CelularDAO.insertar(celular)   ← lo guarda en la base de datos
```

#### EstrategiaDescuento (Patrón Strategy — RECOMENDADO)

**¿Qué problema resuelve?** El cálculo del precio final puede variar. El patrón Strategy permite cambiar el algoritmo de cálculo sin modificar `GestorVentas`.

```java
package com.tecnostore.patron;

// Interfaz (contrato que deben cumplir todas las estrategias).
public interface EstrategiaDescuento {
    double aplicar(double totalSinIva);
}

// Implementación 1: sin descuento.
public class SinDescuento implements EstrategiaDescuento {
    public double aplicar(double totalSinIva)  // retorna totalSinIva sin cambios
}

// Implementación 2: descuento por porcentaje.
public class DescuentoPorcentaje implements EstrategiaDescuento {
    private final double porcentaje;           // por ejemplo, 0.10 para 10%
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

**Resumen de patrones:**

| Patrón            | Clase principal       | Estado       | Para qué sirve                                              |
|-------------------|-----------------------|--------------|-------------------------------------------------------------|
| Singleton         | `ConexionDB`          | OBLIGATORIO  | Garantiza una sola conexión a la BD durante toda la ejecución |
| Factory Method    | `FactoryCelular`      | RECOMENDADO  | Centraliza la construcción de objetos `Celular`             |
| Strategy          | `EstrategiaDescuento` | RECOMENDADO  | Permite cambiar el cálculo de descuentos sin tocar `GestorVentas` |

---

## Data Models

### Enumeraciones

```java
// Clasificación del celular por rango de precio y tecnología.
public enum CategoriaGama { ALTA, MEDIA, BAJA }

// Sistema operativo del celular.
public enum SistemaOperativo { ANDROID, IOS }

// Rol del empleado dentro de la tienda.
public enum TipoEmpleado { CAJERO, BODEGUERO, ADMINISTRADOR }
```

Usar enums evita errores por escribir mal el texto (por ejemplo, `"android"` vs `"ANDROID"` vs `"Android"`). El compilador rechaza cualquier valor que no esté en la lista.

---

### Persona (clase base abstracta)

No se instancia directamente. Sirve como base para `Cliente` y `Empleado`.

| Atributo         | Tipo Java | Restricción                                       |
|------------------|-----------|---------------------------------------------------|
| `id`             | `int`     | Asignado por la base de datos (AUTO_INCREMENT)    |
| `nombre`         | `String`  | Entre 1 y 100 caracteres                          |
| `identificacion` | `String`  | Entre 1 y 20 caracteres, único en el sistema      |
| `telefono`       | `String`  | Entre 7 y 15 dígitos                              |
| `correo`         | `String`  | Formato válido: un @, punto después del @         |

```java
public abstract class Persona implements java.io.Serializable {
    // Constructor con todos los atributos
    public Persona(int id, String nombre, String identificacion,
                   String telefono, String correo)
    public Persona(String nombre, String identificacion,
                   String telefono, String correo)
    // Getters y setters para todos los atributos
    public int getId() / public void setId(int id)
    public String getNombre() / public void setNombre(String nombre)
    public String getIdentificacion() / public void setIdentificacion(String id)
    public String getTelefono() / public void setTelefono(String telefono)
    public String getCorreo() / public void setCorreo(String correo)
}
```

---

### Cliente

Extiende `Persona`. Representa una persona que compra en la tienda.

| Atributo adicional | Tipo Java | Restricción     |
|--------------------|-----------|-----------------|
| `rut`              | `String`  | Puede ser nulo  |

```java
public class Cliente extends Persona {
    public Cliente(int id, String nombre, String identificacion,
                   String telefono, String correo, String rut)
    public Cliente(String nombre, String identificacion,
                   String telefono, String correo)
    public String getRut() / public void setRut(String rut)
    public String toString()
}
```

---

### Empleado

Extiende `Persona`. Representa a un trabajador de la tienda.

| Atributo adicional | Tipo Java      | Restricción             |
|--------------------|----------------|-------------------------|
| `tipo`             | `TipoEmpleado` | CAJERO, BODEGUERO o ADMINISTRADOR |

```java
public class Empleado extends Persona {
    public Empleado(int id, String nombre, String identificacion,
                    String telefono, String correo, TipoEmpleado tipo)
    public TipoEmpleado getTipo() / public void setTipo(TipoEmpleado tipo)
    public String toString()
}
```

---

### Marca

Representa la marca fabricante del celular (Samsung, Apple, etc.).

| Atributo | Tipo Java | Restricción              |
|----------|-----------|--------------------------|
| `id`     | `int`     | Asignado por la BD       |
| `nombre` | `String`  | Entre 1 y 100 caracteres |

```java
public class Marca implements java.io.Serializable {
    public Marca(int id, String nombre)
    public Marca(String nombre)
    public int getId() / public void setId(int id)
    public String getNombre() / public void setNombre(String nombre)
    public String toString()
}
```

---

### Precio

Representa el precio de un celular como entidad separada (normalizado).

| Atributo | Tipo Java | Restricción                     |
|----------|-----------|---------------------------------|
| `id`     | `int`     | Asignado por la BD              |
| `nombre` | `String`  | Etiqueta descriptiva, ej: "Precio base Galaxy S24" |
| `valor`  | `double`  | Entre 0.01 y 999,999,999.99     |

```java
public class Precio implements java.io.Serializable {
    public Precio(int id, String nombre, double valor)
    public Precio(String nombre, double valor)
    public int getId() / public void setId(int id)
    public String getNombre() / public void setNombre(String nombre)
    public double getValor() / public void setValor(double valor)
    public String toString()
}
```

**¿Por qué precio es una tabla separada?** La base de datos `TecnoStoreAnt` está normalizada: el precio no se guarda directamente en la tabla `celular`, sino como una referencia a la tabla `precio`. Esto permite llevar historial de cambios de precios en el futuro sin modificar el celular.

---

### Celular

Representa un teléfono en el inventario.

| Atributo           | Tipo Java         | Restricción                                    |
|--------------------|-------------------|------------------------------------------------|
| `id`               | `int`             | Asignado por la base de datos (AUTO_INCREMENT) |
| `modelo`           | `String`          | Entre 1 y 100 caracteres                       |
| `marca`            | `Marca`           | Objeto Marca no nulo                           |
| `sistemaOperativo` | `SistemaOperativo`| ANDROID o IOS                                  |
| `gama`             | `CategoriaGama`   | ALTA, MEDIA o BAJA                             |
| `precio`           | `Precio`          | Objeto Precio no nulo                          |
| `stock`            | `int`             | Entre 0 y 999,999                              |

```java
public class Celular implements java.io.Serializable {
    public Celular(int id, String modelo, Marca marca, SistemaOperativo so,
                   CategoriaGama gama, Precio precio, int stock)
    public Celular(String modelo, Marca marca, SistemaOperativo so,
                   CategoriaGama gama, Precio precio, int stock)
    public int getId() / public void setId(int id)
    public String getModelo() / public void setModelo(String modelo)
    public Marca getMarca() / public void setMarca(Marca marca)
    public SistemaOperativo getSistemaOperativo() / public void setSistemaOperativo(SistemaOperativo so)
    public CategoriaGama getGama() / public void setGama(CategoriaGama gama)
    public Precio getPrecio() / public void setPrecio(Precio precio)
    public int getStock() / public void setStock(int stock)
    public String toString()
}
```

---

### ItemVenta

Representa una línea dentro de una venta: un celular específico con su cantidad y precio al momento de la compra.

| Atributo          | Tipo Java | Restricción                              |
|-------------------|-----------|------------------------------------------|
| `id`              | `int`     | Asignado por la base de datos            |
| `celular`         | `Celular` | No nulo                                  |
| `cantidad`        | `int`     | Entre 1 y 100                            |
| `subtotal`        | `double`  | Calculado: `round(precio.valor * cantidad, 2)` |

> **¿Por qué guardar `subtotal`?** El precio de un celular puede cambiar después de la venta. Al guardar el subtotal calculado con el precio vigente al momento de la compra, el historial es siempre correcto aunque el precio cambie luego.

```java
public class ItemVenta implements java.io.Serializable {
    public ItemVenta(Celular celular, int cantidad)
    public ItemVenta(int id, Celular celular, int cantidad, double subtotal)
    public int getId()
    public Celular getCelular()
    public int getCantidad()
    public double getSubtotal()   // retorna round(celular.getPrecio().getValor() * cantidad, 2)
    public void setCelular(Celular celular)
    public void setCantidad(int cantidad)
    public String toString()
}
```

---

### Venta

Representa una transacción completa de compra.

| Atributo    | Tipo Java         | Restricción                                    |
|-------------|-------------------|------------------------------------------------|
| `id`        | `int`             | Asignado por la base de datos                  |
| `cliente`   | `Cliente`         | No nulo                                        |
| `empleado`  | `Empleado`        | No nulo (quien procesó la venta)               |
| `items`     | `List<ItemVenta>` | Al menos un elemento                           |
| `fecha`     | `LocalDateTime`   | Fecha y hora del sistema al crear la venta     |
| `total`     | `double`          | Suma de todos los subtotales más IVA del 19%   |

> El campo `total` en la tabla `venta` almacena el total con IVA incluido. El desglose (sin IVA, valor IVA) se calcula en `GestorVentas` al momento de mostrar el comprobante.

```java
public class Venta implements java.io.Serializable {
    public Venta(Cliente cliente, Empleado empleado,
                 List<ItemVenta> items, double total)
    public Venta(int id, Cliente cliente, Empleado empleado,
                 List<ItemVenta> items, LocalDateTime fecha, double total)
    public int getId()
    public Cliente getCliente()
    public Empleado getEmpleado()
    public List<ItemVenta> getItems()
    public LocalDateTime getFecha()
    public double getTotal()
    public void agregarItem(ItemVenta item)
    public String toString()
}
```

---

### Flujo principal de ejecución — proceso de una venta

Este flujo muestra exactamente qué pasa en el código cuando el empleado registra una venta, desde que escribe en la consola hasta que los datos quedan en la base de datos.

```
[CONSOLA]               [Main.java]         [GestorVentas]          [DAOs / BD]
    │                       │                     │                      │
    │── "3. Ventas" ───────►│                     │                      │
    │◄── "ID cliente:" ─────│                     │                      │
    │── "12345678" ────────►│                     │                      │
    │                       │── iniciarSesion() ──►│                      │
    │                       │── agregarItem(1,2) ──►│── buscarPorId(1) ──►│
    │                       │                     │◄─ Celular obj. ───────│
    │                       │                     │ stockComprometido[1]+=2│
    │◄── "¿Agregar más? S/N"│                     │                      │
    │── "N" ───────────────►│                     │                      │
    │◄── "¿Confirmar? S/N" ─│                     │                      │
    │── "S" ───────────────►│                     │                      │
    │                       │── confirmarVenta() ──►│                      │
    │                       │                     │── buscarCliente ──────►│
    │                       │                     │◄─ Cliente obj. ────────│
    │                       │                     │── 2ª verif. stock ────►│
    │                       │                     │◄─ stock OK ────────────│
    │                       │                     │── calcular IVA         │
    │                       │                     │── BEGIN TX ───────────►│
    │                       │                     │── INSERT venta ───────►│
    │                       │                     │── INSERT detalle_venta►│
    │                       │                     │── UPDATE stock ───────►│
    │                       │                     │── COMMIT ─────────────►│
    │◄── Comprobante ───────│                     │                      │
```

---

### Script SQL completo — TecnoStoreAnt

El siguiente script crea la base de datos completa con todas las tablas normalizadas y los datos de prueba. Está listo para ejecutarse en MySQL Workbench.

```sql
-- ============================================================
-- TecnoStoreAnt.sql
-- Base de datos normalizada para el sistema de gestión TecnoStore
-- Ejecutar en MySQL Workbench
-- ============================================================

DROP DATABASE IF EXISTS TecnoStoreAnt;

CREATE DATABASE TecnoStoreAnt
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE TecnoStoreAnt;

-- ------------------------------------------------------------
-- Tabla base: persona
-- ------------------------------------------------------------
CREATE TABLE persona (
    id             INT           NOT NULL AUTO_INCREMENT,
    nombre         VARCHAR(100)  NOT NULL,
    identificacion VARCHAR(20)   NOT NULL,
    telefono       VARCHAR(15)   NOT NULL,
    correo         VARCHAR(255)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_persona_identificacion (identificacion)
);

-- ------------------------------------------------------------
-- Tabla: empleado (hereda de persona)
-- ------------------------------------------------------------
CREATE TABLE empleado (
    id   INT NOT NULL,
    tipo ENUM('CAJERO','BODEGUERO','ADMINISTRADOR') NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_empleado_persona
        FOREIGN KEY (id) REFERENCES persona (id)
        ON DELETE CASCADE
);

-- ------------------------------------------------------------
-- Tabla: cliente (hereda de persona)
-- ------------------------------------------------------------
CREATE TABLE cliente (
    id  INT          NOT NULL,
    rut VARCHAR(20)  NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_cliente_persona
        FOREIGN KEY (id) REFERENCES persona (id)
        ON DELETE CASCADE
);

-- ------------------------------------------------------------
-- Tabla: marca
-- ------------------------------------------------------------
CREATE TABLE marca (
    id     INT          NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_marca_nombre (nombre)
);

-- ------------------------------------------------------------
-- Tabla: precio
-- ------------------------------------------------------------
CREATE TABLE precio (
    id     INT           NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100)  NOT NULL,
    valor  DECIMAL(15,2) NOT NULL,
    PRIMARY KEY (id)
);

-- ------------------------------------------------------------
-- Tabla: celular
-- ------------------------------------------------------------
CREATE TABLE celular (
    id                INT          NOT NULL AUTO_INCREMENT,
    modelo            VARCHAR(100) NOT NULL,
    id_marca_fk       INT          NOT NULL,
    sistema_operativo ENUM('ANDROID','IOS')         NOT NULL,
    gama              ENUM('ALTA','MEDIA','BAJA')    NOT NULL,
    id_precio_fk      INT          NOT NULL,
    stock             INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_celular_marca
        FOREIGN KEY (id_marca_fk)  REFERENCES marca  (id),
    CONSTRAINT fk_celular_precio
        FOREIGN KEY (id_precio_fk) REFERENCES precio (id)
);

-- ------------------------------------------------------------
-- Tabla: venta
-- ------------------------------------------------------------
CREATE TABLE venta (
    id              INT           NOT NULL AUTO_INCREMENT,
    id_cliente_fk   INT           NOT NULL,
    fecha           DATETIME      NOT NULL,
    total           DECIMAL(15,2) NOT NULL,
    id_empleado_fk  INT           NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_venta_cliente
        FOREIGN KEY (id_cliente_fk)  REFERENCES cliente  (id),
    CONSTRAINT fk_venta_empleado
        FOREIGN KEY (id_empleado_fk) REFERENCES empleado (id)
);

-- ------------------------------------------------------------
-- Tabla: detalle_venta
-- ------------------------------------------------------------
CREATE TABLE detalle_venta (
    id            INT           NOT NULL AUTO_INCREMENT,
    id_venta_fk   INT           NOT NULL,
    id_celular_fk INT           NOT NULL,
    cantidad      INT           NOT NULL,
    subtotal      DECIMAL(15,2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_detalle_venta
        FOREIGN KEY (id_venta_fk)   REFERENCES venta   (id),
    CONSTRAINT fk_detalle_celular
        FOREIGN KEY (id_celular_fk) REFERENCES celular (id)
);

-- ============================================================
-- DATOS DE PRUEBA
-- ============================================================

-- 3 empleados ─────────────────────────────────────────────────
INSERT INTO persona (nombre, identificacion, telefono, correo) VALUES
    ('Pedro Ramírez',  '11111111', '3001234567', 'pedro.ramirez@tecnostore.com'),
    ('María González', '22222222', '3007654321', 'maria.gonzalez@tecnostore.com'),
    ('Carlos Mendoza', '33333333', '3009876543', 'carlos.mendoza@tecnostore.com');

INSERT INTO empleado (id, tipo) VALUES
    (1, 'CAJERO'),
    (2, 'BODEGUERO'),
    (3, 'ADMINISTRADOR');

-- 5 clientes ──────────────────────────────────────────────────
INSERT INTO persona (nombre, identificacion, telefono, correo) VALUES
    ('Alejandro Torres',  '44444444', '3101112233', 'alejandro.torres@gmail.com'),
    ('Valentina Ruiz',    '55555555', '3204445566', 'valentina.ruiz@hotmail.com'),
    ('Sebastián Castro',  '66666666', '3137778899', 'sebastian.castro@gmail.com'),
    ('Camila Herrera',    '77777777', '3009990011', 'camila.herrera@yahoo.com'),
    ('Andrés Vargas',     '88888888', '3162223344', 'andres.vargas@gmail.com');

INSERT INTO cliente (id, rut) VALUES
    (4,  NULL),
    (5,  NULL),
    (6,  NULL),
    (7,  NULL),
    (8,  NULL);

-- Marcas ──────────────────────────────────────────────────────
INSERT INTO marca (nombre) VALUES
    ('Samsung'),   -- id 1
    ('Apple'),     -- id 2
    ('Xiaomi'),    -- id 3
    ('Motorola'),  -- id 4
    ('Huawei'),    -- id 5
    ('Google'),    -- id 6
    ('Realme');    -- id 7

-- Precios ─────────────────────────────────────────────────────
INSERT INTO precio (nombre, valor) VALUES
    ('Precio Galaxy S24 Ultra',     4199990.00),  -- id 1
    ('Precio Galaxy A54',           1099990.00),  -- id 2
    ('Precio iPhone 15 Pro',        5299990.00),  -- id 3
    ('Precio iPhone 14',            3499990.00),  -- id 4
    ('Precio Redmi Note 13 Pro',     899990.00),  -- id 5
    ('Precio Poco X6 Pro',           999990.00),  -- id 6
    ('Precio Moto G84',              699990.00),  -- id 7
    ('Precio P60 Pro',              2799990.00),  -- id 8
    ('Precio Pixel 8 Pro',          3999990.00),  -- id 9
    ('Precio Realme 11 Pro+',        799990.00);  -- id 10

-- 10 modelos de celular (30 unidades de stock cada uno) ───────
INSERT INTO celular (modelo, id_marca_fk, sistema_operativo, gama, id_precio_fk, stock) VALUES
    ('Galaxy S24 Ultra',  1, 'ANDROID', 'ALTA',  1,  30),
    ('Galaxy A54',        1, 'ANDROID', 'MEDIA', 2,  30),
    ('iPhone 15 Pro',     2, 'IOS',     'ALTA',  3,  30),
    ('iPhone 14',         2, 'IOS',     'ALTA',  4,  30),
    ('Redmi Note 13 Pro', 3, 'ANDROID', 'MEDIA', 5,  30),
    ('Poco X6 Pro',       3, 'ANDROID', 'MEDIA', 6,  30),
    ('Moto G84',          4, 'ANDROID', 'MEDIA', 7,  30),
    ('P60 Pro',           5, 'ANDROID', 'ALTA',  8,  30),
    ('Pixel 8 Pro',       6, 'ANDROID', 'ALTA',  9,  30),
    ('Realme 11 Pro+',    7, 'ANDROID', 'MEDIA', 10, 30);

-- 2 ventas de prueba ──────────────────────────────────────────
-- Venta 1: Alejandro Torres compra 1 Galaxy S24 Ultra y 1 Galaxy A54
INSERT INTO venta (id_cliente_fk, fecha, total, id_empleado_fk) VALUES
    (4, '2025-06-01 10:30:00', 6307188.10, 1);

INSERT INTO detalle_venta (id_venta_fk, id_celular_fk, cantidad, subtotal) VALUES
    (1, 1, 1, 4199990.00),   -- 1 Galaxy S24 Ultra
    (1, 2, 1, 1099990.00);   -- 1 Galaxy A54
-- total sin IVA = 5299980.00 | IVA 19% = 1006996.20 | total = 6306976.20
-- (ajustado con redondeo a 2 decimales en el sistema)

UPDATE celular SET stock = stock - 1 WHERE id IN (1, 2);

-- Venta 2: Valentina Ruiz compra 2 Redmi Note 13 Pro
INSERT INTO venta (id_cliente_fk, fecha, total, id_empleado_fk) VALUES
    (5, '2025-06-02 14:15:00', 2141976.20, 1);

INSERT INTO detalle_venta (id_venta_fk, id_celular_fk, cantidad, subtotal) VALUES
    (2, 5, 2, 1799980.00);   -- 2 Redmi Note 13 Pro (899990 × 2)
-- total sin IVA = 1799980.00 | IVA 19% = 341996.20 | total = 2141976.20

UPDATE celular SET stock = stock - 2 WHERE id = 5;
```

**Diagrama de relaciones entre tablas:**

```
persona          cliente          empleado
───────          ───────          ────────
id ◄────────────── id              id ◄──────────────────────┐
nombre             rut             tipo                       │
identificacion                                               │
telefono                                                     │
correo                                                       │
                                                             │
marca            precio           celular                    │
─────            ──────           ───────                    │
id ◄──────┐      id ◄──────┐      id                        │
nombre    │      nombre    │      modelo                     │
          │      valor     │      id_marca_fk ───────────────┘(*)
          └────────────────┴───── id_precio_fk               │
                                  sistema_operativo           │
                                  gama                        │
                                  stock                       │
                                                             │
venta            detalle_venta                               │
─────            ─────────────                               │
id ◄────────────── id_venta_fk    ◄── id_celular_fk ─────────┘(*)
id_cliente_fk      id_celular_fk
fecha              cantidad
total              subtotal
id_empleado_fk ───────────────────────────────────────────────►empleado.id
```

---

## Correctness Properties

*Una propiedad es una característica o comportamiento que debe ser verdadero en todas las ejecuciones válidas del sistema. Las propiedades sirven como base para las pruebas automáticas basadas en propiedades (property-based testing).*

### Property 1: Validación consistente de precio y stock

*Para cualquier* valor de precio y stock generados aleatoriamente, `Validador.esPrecioValido()` debe retornar `true` si y solo si el precio está en el rango (0.01, 999999999.99], y `Validador.esStockValido()` debe retornar `true` si y solo si el stock está en el rango [0, 999999]. Ningún celular debe poder registrarse con valores fuera de esos rangos.

**Validates: Requirements 2.2, 2.4**

---

### Property 2: Validación consistente de correo electrónico

*Para cualquier* cadena de texto generada aleatoriamente, `Validador.esCorreoValido()` debe retornar `true` si y solo si la cadena contiene exactamente un "@", al menos un carácter no vacío antes del "@", y al menos un "." después del "@" con caracteres en ambos lados.

**Validates: Requirements 3.3**

---

### Property 3: Unicidad de identificación de cliente

*Para cualquier* identificación generada aleatoriamente, si se intenta registrar un segundo cliente con esa misma identificación, el sistema debe rechazar el registro y la base de datos debe seguir conteniendo exactamente un cliente con esa identificación.

**Validates: Requirements 3.4**

---

### Property 4: Cálculo correcto de subtotal e IVA

*Para cualquier* lista de ítems de venta con precios y cantidades válidos, el subtotal de cada ítem debe ser exactamente `round(precio * cantidad, 2)`, y el total con IVA de la venta debe ser exactamente `round(sumaDeTodosLosSubtotales * 1.19, 2)`.

**Validates: Requirements 4.5**

---

### Property 5: Reducción correcta de stock al confirmar venta

*Para cualquier* venta confirmada exitosamente, el stock de cada celular incluido en la venta debe ser exactamente igual al stock antes de la venta menos la cantidad total vendida de ese celular en esa transacción.

**Validates: Requirements 4.6**

---

### Property 6: Atomicidad de la transacción de venta

*Para cualquier* intento de registrar una venta que falle en algún paso intermedio (al insertar un ítem, al actualizar el stock o al fallar la segunda verificación de stock), el estado completo del sistema debe ser idéntico al estado anterior al intento. Ningún cambio parcial debe quedar persistido.

**Validates: Requirements 4.7, 4.11**

---

### Property 7: Consistencia del stock comprometido durante la sesión

*Para cualquier* secuencia de adiciones del mismo celular a una venta en curso, el valor de `stockComprometido[idCelular]` debe ser igual a la suma de todas las cantidades agregadas para ese celular en la sesión actual. Si la suma de cantidades solicitadas supera el stock disponible en cualquier punto de la sesión, el sistema debe rechazar el ítem y mostrar el stock disponible real y el ya comprometido.

**Validates: Requirements 4.3, 4.4, 4.10**

---

### Property 8: Corrección del filtro de stock bajo

*Para cualquier* lista de celulares generada aleatoriamente, el reporte de stock bajo debe contener exactamente los celulares cuyo stock sea estrictamente menor a 5, ordenados de menor a mayor stock, y no debe contener ningún celular con stock mayor o igual a 5.

**Validates: Requirements 5.1**

---

### Property 9: Round-trip de serialización del reporte

*Para cualquier* lista de objetos `Venta` (incluyendo la lista vacía), llamar a `ArchivoUtils.generarReporte(ventas)` seguido de `ArchivoUtils.leerReporte()` debe retornar una lista con los mismos objetos (mismos ids, clientes, ítems y totales) que la lista original.

**Validates: Requirements 6.1, 6.2**

---

## Error Handling

| Excepción                  | Dónde ocurre                               | Quién la captura                                     | Acción al capturar                                                                   |
|----------------------------|--------------------------------------------|------------------------------------------------------|--------------------------------------------------------------------------------------|
| `SQLException`             | Métodos de todos los DAOs                  | `GestorCelulares`, `GestorClientes`, `GestorVentas`  | Muestra "Error al acceder a la base de datos" en consola                             |
| `SQLException` en TX       | `VentaDAO.insertarConTransaccion`          | `GestorVentas`                                       | Ejecuta `rollback()`, muestra "Error al registrar la venta, no se realizaron cambios"|
| `IOException`              | `ArchivoUtils.generarReporte/leerReporte`  | El propio `ArchivoUtils`                             | Muestra "No se pudo acceder al archivo de reporte", imprime traza en `System.err`    |
| `ClassNotFoundException`   | `ArchivoUtils.leerReporte` (deserializar)  | El propio `ArchivoUtils`                             | Muestra "Archivo de reporte incompatible o dañado", imprime traza en `System.err`    |
| `SQLException` al iniciar  | `ConexionDB.getInstancia()`                | `Main.java`                                          | Muestra mensaje con la causa y termina con `System.exit(1)`                          |
| `IllegalArgumentException` | Gestores (validación de negocio)           | `Main.java`                                          | Muestra el mensaje de la excepción y repite la solicitud de datos                    |
| `IllegalStateException`    | `GestorVentas.agregarItem` (stock insuf.)  | `Main.java`                                          | Muestra el mensaje (disponible X, comprometido Y) y permite intentar de nuevo        |
| `NumberFormatException`    | `Main.java` al leer enteros del Scanner    | `Main.java`                                          | Muestra "Debe ingresar un número válido" y repite la lectura                         |

**Regla general:** Las capas bajas (DAO) lanzan excepciones. Las capas altas (servicio, `Main`) las capturan y las convierten en mensajes amigables para el usuario. Nunca se muestra un stack trace al usuario final; solo se muestra en `System.err` para el desarrollador.

---

## Testing Strategy

### Pruebas basadas en propiedades (Property-Based Testing con jqwik)

Para este sistema se usa **jqwik** como biblioteca de property-based testing, compatible con JUnit 5 en Java 21. Cada propiedad de corrección listada arriba se implementa como una prueba automática que genera muchas entradas aleatorias (mínimo **100 iteraciones** por propiedad) y verifica que la propiedad se cumpla en todos los casos.

**Etiqueta de referencia por prueba:**

```
Feature: tecnostore-sistema-gestion, Property {número}: {texto de la propiedad}
```

Ejemplo de implementación para la Propiedad 4 (cálculo de subtotal e IVA):

```java
// Feature: tecnostore-sistema-gestion, Property 4: Cálculo correcto de subtotal e IVA
@Property
void calculoSubtotalEIvaEsCorrecto(
        @ForAll @DoubleRange(min = 0.01, max = 999999.99) double precioValor,
        @ForAll @IntRange(min = 1, max = 100) int cantidad) {

    Precio precio = new Precio("Test", precioValor);
    Marca marca = new Marca("MarcaTest");
    Celular celular = new Celular("ModeloTest", marca, SistemaOperativo.ANDROID,
                                  CategoriaGama.MEDIA, precio, 100);

    ItemVenta item = new ItemVenta(celular, cantidad);
    double subtotalEsperado = Math.round(precioValor * cantidad * 100.0) / 100.0;
    assertThat(item.getSubtotal()).isEqualTo(subtotalEsperado);

    List<ItemVenta> items = List.of(item);
    double totalConIvaEsperado = Math.round(subtotalEsperado * 1.19 * 100.0) / 100.0;
    GestorVentas gestor = new GestorVentas(mockVentaDAO, mockClienteDAO, mockCelularDAO);
    assertThat(gestor.calcularTotalConIva(items)).isEqualTo(totalConIvaEsperado);
}
```

Ejemplo para la Propiedad 9 (round-trip de serialización):

```java
// Feature: tecnostore-sistema-gestion, Property 9: Round-trip de serialización del reporte
@Property
void serializacionEsRoundTrip(@ForAll("listaDeVentas") List<Venta> ventas) {
    ArchivoUtils.generarReporte(ventas);
    List<Venta> leidas = ArchivoUtils.leerReporte();

    assertThat(leidas).hasSameSizeAs(ventas);
    for (int i = 0; i < ventas.size(); i++) {
        assertThat(leidas.get(i).getId()).isEqualTo(ventas.get(i).getId());
        assertThat(leidas.get(i).getTotal()).isEqualTo(ventas.get(i).getTotal());
    }
}
```

### Pruebas unitarias con ejemplos concretos

Complementan las pruebas de propiedades con casos específicos y casos límite:

- **Validador:** correos válidos e inválidos concretos (`"usuario@dominio.com"` válido, `"sinArroba"` inválido, `"doble@@dominio.com"` inválido); precios límite exactos (0.01 válido, 0.00 inválido, 999999999.99 válido, 1000000000.00 inválido)
- **ReporteUtils:** listas con 3, 2, 1 y 0 celulares vendidos para verificar el top N; lista vacía que produce el mensaje "No hay datos disponibles"
- **GestorVentas:** stock insuficiente en primer ítem, stock insuficiente por acumulación de stockComprometido, cliente inexistente, cantidad fuera de rango [1,100]
- **ConexionDB:** dos llamadas a `getInstancia()` deben retornar la misma referencia de objeto (Singleton)
- **ArchivoUtils:** generar reporte con lista vacía y verificar que se escribe igualmente el archivo y aparece el aviso

### Pruebas de integración

Se ejecutan contra una base de datos MySQL de prueba separada (`TecnoStoreAntTest`):

- `CelularDAO`, `ClienteDAO`, `EmpleadoDAO`, `MarcaDAO`, `PrecioDAO` y `VentaDAO` ejecutan correctamente operaciones CRUD contra la BD real
- `VentaDAO.insertarConTransaccion` ejecuta `rollback` correctamente cuando se fuerza un fallo en el segundo INSERT
- `ConexionDB` retorna siempre la misma instancia incluso con múltiples llamadas concurrentes (para verificar el thread-safety del Singleton)
- La herencia `Persona → Cliente` y `Persona → Empleado` persiste y se reconstruye correctamente desde la BD

### Equilibrio entre tipos de prueba

Las pruebas de propiedades cubren el espacio de entradas de forma amplia (100+ combinaciones aleatorias). Las pruebas unitarias cubren los casos límite exactos que son difíciles de generar aleatoriamente. Las pruebas de integración verifican que las piezas encajan correctamente con la base de datos real. Los tres tipos son complementarios y necesarios.
