# Implementation Plan: TecnoStore Sistema de Gestión

## Overview

Este plan convierte el diseño técnico de TecnoStore en una serie de tareas de codificación ordenadas para un desarrollador que está aprendiendo Java. Cada tarea construye sobre las anteriores hasta tener el sistema completo funcionando: desde la estructura del proyecto hasta el menú principal con todas sus funciones.

**Lenguaje:** Java 21  
**IDE:** NetBeans  
**Base de datos:** MySQL 8+ con JDBC (sin frameworks)  
**Testing:** jqwik sobre JUnit 5

---

## Tasks

- [ ] 1. Fase 0 — Configurar el proyecto en NetBeans
  - Crea un proyecto Java Application en NetBeans llamado `TecnoStore`.
  - Crea manualmente la estructura de paquetes vacíos dentro de `src/`:
    - `com.tecnostore.modelo`
    - `com.tecnostore.servicio`
    - `com.tecnostore.persistencia`
    - `com.tecnostore.util`
    - `com.tecnostore.patron`
  - Descarga el JAR del conector MySQL (Connector/J 8.x) y agrégalo como librería del proyecto en NetBeans (clic derecho en Libraries → Add JAR/Folder).
  - Descarga los JARs de JUnit 5 y jqwik y agrégalos también como librerías.
  - Crea el archivo `src/config.properties` con los parámetros de conexión (host, puerto, nombre de base de datos, usuario, contraseña).
  - **Archivos a crear:** estructura de paquetes, `src/config.properties`
  - **Depende de:** —
  - **Criterio de aceptación:** el proyecto compila sin errores con los paquetes vacíos creados y las librerías agregadas.
  - **Estimado:** 1 – 2 h
  - _Requerimientos: 7.1, 10.4, 10.5_

- [ ] 2. Fase 1 — Implementar los enumerados (enums)
  - [ ] 2.1 Crear los tres enumerados del modelo
    - Crea la clase `CategoriaGama.java` en el paquete `com.tecnostore.modelo` con los valores `ALTA`, `MEDIA` y `BAJA`.
    - Crea la clase `SistemaOperativo.java` en el mismo paquete con los valores `ANDROID` e `IOS`.
    - Crea la clase `TipoEmpleado.java` en el mismo paquete con los valores `CAJERO`, `BODEGUERO` y `ADMINISTRADOR`.
    - Los enums no necesitan métodos extra, solo los valores declarados.
    - **Archivos a crear:** `CategoriaGama.java`, `SistemaOperativo.java`, `TipoEmpleado.java`
    - **Depende de:** 1
    - **Criterio de aceptación:** los tres archivos compilan sin errores y sus valores se pueden usar en otras clases.
    - **Estimado:** 30 min
    - _Requerimientos: 2.3_

- [ ] 3. Fase 1 — Implementar las clases del modelo
  - [ ] 3.1 Crear la clase base abstracta `Persona`
    - Crea `Persona.java` en `com.tecnostore.modelo` como clase abstracta que implementa `java.io.Serializable`.
    - Atributos privados: `id` (int), `nombre` (String), `identificacion` (String), `telefono` (String), `correo` (String).
    - Dos constructores: uno con `id` (para cargar desde la BD) y otro sin `id` (para crear nuevo).
    - Genera getters y setters para todos los atributos.
    - **Archivos a crear:** `Persona.java`
    - **Depende de:** 2.1
    - **Criterio de aceptación:** la clase compila; no se puede instanciar directamente con `new Persona(...)`.
    - **Estimado:** 30 min
    - _Requerimientos: 3.2, 6.8_

  - [ ] 3.2 Crear las clases `Cliente` y `Empleado`
    - Crea `Cliente.java` en `com.tecnostore.modelo` que extiende `Persona`.
    - Agrega el atributo privado `rut` (String, puede ser nulo).
    - Dos constructores: con `id` y sin `id`. Ambos llaman a `super(...)`.
    - Agrega getter, setter y `toString()`.
    - Crea `Empleado.java` que extiende `Persona`.
    - Agrega el atributo privado `tipo` de tipo `TipoEmpleado`.
    - Dos constructores con y sin `id`. Agrega getter, setter y `toString()`.
    - **Archivos a crear:** `Cliente.java`, `Empleado.java`
    - **Depende de:** 3.1
    - **Criterio de aceptación:** se puede crear un objeto `new Cliente("Juan", "12345", "3001234567", "j@mail.com")` sin errores.
    - **Estimado:** 30 min
    - _Requerimientos: 3.1, 3.2, 6.8_

  - [ ] 3.3 Crear las clases `Marca` y `Precio`
    - Crea `Marca.java` en `com.tecnostore.modelo` con atributos privados `id` (int) y `nombre` (String). Implementa `Serializable`. Dos constructores (con y sin `id`). Getters, setters y `toString()`.
    - Crea `Precio.java` con atributos privados `id` (int), `nombre` (String) y `valor` (double). Implementa `Serializable`. Dos constructores (con y sin `id`). Getters, setters y `toString()`.
    - **Archivos a crear:** `Marca.java`, `Precio.java`
    - **Depende de:** 3.1
    - **Criterio de aceptación:** se puede crear `new Marca("Samsung")` y `new Precio("Precio base", 999990.00)` sin errores.
    - **Estimado:** 30 min
    - _Requerimientos: 7.2, 6.8_

  - [ ] 3.4 Crear la clase `Celular`
    - Crea `Celular.java` en `com.tecnostore.modelo`. Implementa `Serializable`.
    - Atributos privados: `id` (int), `modelo` (String), `marca` (Marca), `sistemaOperativo` (SistemaOperativo), `gama` (CategoriaGama), `precio` (Precio), `stock` (int).
    - Dos constructores: con `id` (para cargar desde la BD) y sin `id` (para nuevo registro). Getters, setters y `toString()`.
    - **Archivos a crear:** `Celular.java`
    - **Depende de:** 3.3, 2.1
    - **Criterio de aceptación:** se puede crear un objeto `Celular` con todos sus atributos sin errores de compilación.
    - **Estimado:** 30 min
    - _Requerimientos: 2.2, 6.8_

  - [ ] 3.5 Crear las clases `ItemVenta` y `Venta`
    - Crea `ItemVenta.java` en `com.tecnostore.modelo`. Implementa `Serializable`.
    - Atributos: `id` (int), `celular` (Celular), `cantidad` (int), `subtotal` (double).
    - El constructor `ItemVenta(Celular celular, int cantidad)` debe calcular el subtotal automáticamente: `Math.round(celular.getPrecio().getValor() * cantidad * 100.0) / 100.0`.
    - El constructor `ItemVenta(int id, Celular celular, int cantidad, double subtotal)` recibe el subtotal ya calculado (para cargar desde la BD).
    - Getters y `toString()`. El setter de `cantidad` debe recalcular el subtotal.
    - Crea `Venta.java`. Implementa `Serializable`.
    - Atributos: `id` (int), `cliente` (Cliente), `empleado` (Empleado), `items` (List<ItemVenta>), `fecha` (LocalDateTime), `total` (double).
    - Constructor sin `id` que asigna `fecha = LocalDateTime.now()`.
    - Constructor con `id` y `fecha` para cargar desde la BD.
    - Método `agregarItem(ItemVenta item)` que agrega a la lista. Getters y `toString()`.
    - **Archivos a crear:** `ItemVenta.java`, `Venta.java`
    - **Depende de:** 3.4, 3.2
    - **Criterio de aceptación:** dado un `Celular` con precio 100.00, crear `new ItemVenta(celular, 3)` debe dar `subtotal = 300.00`.
    - **Estimado:** 1 h
    - _Requerimientos: 4.5, 6.8_

- [ ] 4. Fase 2 — Crear el script SQL y la clase de conexión
  - [ ] 4.1 Crear el script `TecnoStoreAnt.sql`
    - Crea el archivo `TecnoStoreAnt.sql` en la raíz del proyecto (fuera de `src/`).
    - El script debe comenzar con `DROP DATABASE IF EXISTS TecnoStoreAnt;` y `CREATE DATABASE TecnoStoreAnt CHARACTER SET utf8mb4;`.
    - Crea las 8 tablas normalizadas en este orden (respeta las llaves foráneas): `persona`, `empleado`, `cliente`, `marca`, `precio`, `celular`, `venta`, `detalle_venta`.
    - Agrega los datos de prueba al final del script:
      - 3 empleados en `persona` + `empleado` (1 CAJERO, 1 BODEGUERO, 1 ADMINISTRADOR).
      - 5 clientes en `persona` + `cliente`.
      - 7 marcas en `marca` (Samsung, Apple, Xiaomi, Motorola, Huawei, Google, Realme).
      - 10 registros en `precio` y 10 en `celular` con 30 unidades de stock cada uno.
      - 2 ventas completas en `venta` + `detalle_venta` + actualización de stock con `UPDATE`.
    - El diseño completo del script (columnas, tipos, constraints) está en la sección "Script SQL completo" del documento de diseño.
    - **Archivos a crear:** `TecnoStoreAnt.sql`
    - **Depende de:** 1
    - **Criterio de aceptación:** ejecutar el script en MySQL Workbench sin errores crea la base de datos con todas las tablas y los datos de prueba visibles.
    - **Estimado:** 2 – 3 h
    - _Requerimientos: 7.2, 7.6, 10.1_

  - [ ] 4.2 Implementar `ConexionDB` (Singleton thread-safe)
    - Crea `ConexionDB.java` en `com.tecnostore.persistencia`.
    - Declara el atributo estático privado `instancia` de tipo `ConexionDB` (inicialmente null).
    - Declara el atributo privado `conexion` de tipo `Connection`.
    - El constructor es **privado** (nadie puede hacer `new ConexionDB()` desde fuera).
    - En el constructor privado, lee `config.properties` del classpath con `Properties` y `getResourceAsStream`, y llama a `DriverManager.getConnection(url, usuario, contrasena)`.
    - El método estático `getInstancia()` debe ser **synchronized** para que sea thread-safe: si `instancia == null`, crea una nueva; si no, devuelve la que ya existe.
    - Agrega los métodos `getConexion()` y `cerrarConexion()`.
    - **Archivos a crear:** `ConexionDB.java`
    - **Depende de:** 4.1
    - **Criterio de aceptación:** dos llamadas consecutivas a `ConexionDB.getInstancia()` retornan exactamente el mismo objeto (misma referencia).
    - **Estimado:** 1 – 2 h
    - _Requerimientos: 1.2, 1.4, 7.1, 8.1, 9.2_

- [ ] 5. Fase 3 — Implementar los DAOs
  - [ ] 5.1 Crear `PersonaDAO`
    - Crea `PersonaDAO.java` en `com.tecnostore.persistencia`.
    - El constructor recibe un `Connection`.
    - Implementa tres métodos con `PreparedStatement`:
      - `insertar(Persona persona)`: hace `INSERT INTO persona` y retorna el `id` generado por `AUTO_INCREMENT` (usa `Statement.RETURN_GENERATED_KEYS`).
      - `buscarPorId(int id)`: hace `SELECT * FROM persona WHERE id = ?` y retorna un objeto o `null`.
      - `buscarPorIdentificacion(String identificacion)`: hace `SELECT` por identificación.
    - Todos los métodos declaran `throws SQLException`.
    - **Archivos a crear:** `PersonaDAO.java`
    - **Depende de:** 4.2, 3.2
    - **Criterio de aceptación:** `insertar` guarda un registro en la tabla `persona` y retorna un `id` mayor a 0.
    - **Estimado:** 1 h
    - _Requerimientos: 7.3, 7.4_

  - [ ] 5.2 Crear `MarcaDAO` y `PrecioDAO`
    - Crea `MarcaDAO.java` con los métodos: `insertar(Marca)`, `listarTodas()`, `buscarPorId(int)`, `buscarPorNombre(String)`. Todos usan `PreparedStatement`.
    - Crea `PrecioDAO.java` con los métodos: `insertar(Precio)`, `listarTodos()`, `buscarPorId(int)`.
    - Ambas clases reciben `Connection` en el constructor y declaran `throws SQLException`.
    - **Archivos a crear:** `MarcaDAO.java`, `PrecioDAO.java`
    - **Depende de:** 4.2, 3.3
    - **Criterio de aceptación:** `MarcaDAO.listarTodas()` retorna las 7 marcas del script de prueba.
    - **Estimado:** 1 h
    - _Requerimientos: 7.3_

  - [ ] 5.3 Crear `CelularDAO`
    - Crea `CelularDAO.java` en `com.tecnostore.persistencia`. Recibe `Connection` en el constructor.
    - Implementa con `PreparedStatement`:
      - `insertar(Celular)`: `INSERT INTO celular` usando `id_marca_fk` e `id_precio_fk`.
      - `buscarPorId(int)`: `SELECT` con `JOIN` a `marca` y `precio`.
      - `listarTodos()`: `SELECT` con `JOIN`, retorna lista.
      - `actualizar(Celular)`: `UPDATE celular SET ... WHERE id = ?`.
      - `eliminar(int id)`: `DELETE FROM celular WHERE id = ?`.
      - `actualizarStock(int idCelular, int cantidad)`: `UPDATE celular SET stock = stock - cantidad WHERE id = ? AND stock >= cantidad`. Si no actualiza ninguna fila (stock insuficiente), lanza `SQLException`.
    - **Archivos a crear:** `CelularDAO.java`
    - **Depende de:** 5.2, 3.4
    - **Criterio de aceptación:** `listarTodos()` retorna los 10 celulares del script; `actualizarStock` lanza excepción si se intenta descontar más stock del disponible.
    - **Estimado:** 2 h
    - _Requerimientos: 2.1, 2.6, 2.8, 7.3_

  - [ ] 5.4 Crear `ClienteDAO` y `EmpleadoDAO`
    - Crea `ClienteDAO.java`. Recibe `Connection` en el constructor y crea internamente un `PersonaDAO`.
    - Método `insertar(Cliente)`: llama a `personaDAO.insertar(cliente)` primero (para obtener el id), luego hace `INSERT INTO cliente` con ese id.
    - Método `buscarPorIdentificacion(String)`: hace `SELECT` con `JOIN` entre `persona` y `cliente`.
    - Método `listarTodos()`: `SELECT` con `JOIN`, retorna lista de `Cliente`.
    - Método `existeIdentificacion(String)`: retorna `boolean`, más eficiente que cargar el objeto completo.
    - Crea `EmpleadoDAO.java` con la misma lógica pero para la tabla `empleado`: `insertar(Empleado)`, `buscarPorId(int)`, `listarTodos()`.
    - **Archivos a crear:** `ClienteDAO.java`, `EmpleadoDAO.java`
    - **Depende de:** 5.1, 3.2
    - **Criterio de aceptación:** `ClienteDAO.listarTodos()` retorna los 5 clientes del script; `existeIdentificacion("44444444")` retorna `true`.
    - **Estimado:** 2 h
    - _Requerimientos: 3.1, 3.4, 3.6, 7.4_

  - [ ] 5.5 Crear `VentaDAO`
    - Crea `VentaDAO.java` en `com.tecnostore.persistencia`. Recibe `Connection` en el constructor.
    - Método `insertarConTransaccion(Venta venta)`:
      1. Llama a `conexion.setAutoCommit(false)` para iniciar la transacción.
      2. Hace doble verificación del stock de cada celular con `SELECT stock FROM celular WHERE id = ? FOR UPDATE`. Si alguno tiene stock insuficiente, lanza `SQLException` inmediatamente.
      3. `INSERT INTO venta` y obtiene el id generado.
      4. Para cada `ItemVenta`: `INSERT INTO detalle_venta` y llama a `CelularDAO.actualizarStock`.
      5. Llama a `conexion.commit()` si todo fue bien.
      6. En el bloque `catch`: llama a `conexion.rollback()` y re-lanza la excepción.
      7. En el bloque `finally`: restaura `conexion.setAutoCommit(true)`.
    - Método `listarTodas()`: `SELECT` con `JOIN` a `cliente`, `empleado`, y `detalle_venta`. Reconstruye el objeto `Venta` con su lista de `ItemVenta`.
    - **Archivos a crear:** `VentaDAO.java`
    - **Depende de:** 5.4, 5.3, 3.5
    - **Criterio de aceptación:** al forzar un fallo en el segundo INSERT, `listarTodas()` retorna el mismo número de ventas que antes del intento; el stock no cambia.
    - **Estimado:** 3 h
    - _Requerimientos: 4.6, 4.7, 7.3_

- [ ] 6. Fase 4 — Implementar las utilidades
  - [ ] 6.1 Crear `Validador`
    - Crea `Validador.java` en `com.tecnostore.util`. Todos los métodos son **estáticos** (no se instancia la clase).
    - Implementa exactamente estos seis métodos:
      - `esPrecioValido(double precio)`: retorna `true` si `precio >= 0.01 && precio <= 999_999_999.99`.
      - `esStockValido(int stock)`: retorna `true` si `stock >= 0 && stock <= 999_999`.
      - `esCorreoValido(String correo)`: usa `String.indexOf('@')` para verificar que haya exactamente un `@`, al menos un carácter antes, y al menos un `.` después con caracteres en ambos lados. No usar regex.
      - `esTelefonoValido(String telefono)`: verifica que tenga entre 7 y 15 caracteres y que todos sean dígitos.
      - `esTextoValido(String texto, int max)`: retorna `true` si no es nulo, no está vacío (después de `trim()`), y tiene longitud `<= max`.
      - `esCantidadValida(int cantidad)`: retorna `true` si `cantidad >= 1 && cantidad <= 100`.
    - **Archivos a crear:** `Validador.java`
    - **Depende de:** 1
    - **Criterio de aceptación:** ver criterios de las pruebas de propiedad 1 y 2 en el documento de diseño.
    - **Estimado:** 1 h
    - _Requerimientos: 2.4, 2.5, 3.3, 3.8_

  - [ ]* 6.2 Escribir prueba de propiedad para `Validador` (Propiedad 1)
    - Crea la clase de prueba `ValidadorPropiedadesTest.java` en el paquete de pruebas.
    - **Propiedad 1: Validación consistente de precio y stock**
    - Usa la anotación `@Property` de jqwik con `@ForAll @DoubleRange` y `@ForAll @IntRange` para generar precios y stocks aleatorios.
    - Verifica que `esPrecioValido` retorne `true` exactamente cuando el precio está en `(0.01, 999999999.99]` y `false` en caso contrario.
    - Verifica que `esStockValido` retorne `true` exactamente cuando el stock está en `[0, 999999]`.
    - **Valida: Requerimientos 2.2, 2.4**
    - **Archivos a crear:** `ValidadorPropiedadesTest.java`
    - **Depende de:** 6.1
    - **Estimado:** 45 min
    - _Requerimientos: 2.2, 2.4_

  - [ ]* 6.3 Escribir prueba de propiedad para `Validador` (Propiedad 2)
    - En la misma clase `ValidadorPropiedadesTest.java` (o en una nueva si prefieres), agrega el método de prueba para la Propiedad 2.
    - **Propiedad 2: Validación consistente de correo electrónico**
    - Genera cadenas aleatorias con jqwik (`@ForAll String correo`) y verifica que `esCorreoValido` retorne `true` si y solo si la cadena cumple: exactamente un `@`, al menos un carácter antes, y al menos un `.` después del `@` con caracteres en ambos lados.
    - **Valida: Requerimiento 3.3**
    - **Archivos a modificar:** `ValidadorPropiedadesTest.java`
    - **Depende de:** 6.2
    - **Estimado:** 30 min
    - _Requerimientos: 3.3_

  - [ ] 6.4 Crear `ConsolaUtils`
    - Crea `ConsolaUtils.java` en `com.tecnostore.util`. Todos los métodos son **estáticos**.
    - Implementa exactamente estos cinco métodos:
      - `imprimirTitulo(String titulo)`: imprime el título centrado entre dos líneas de `═` de 60 caracteres. Usa `String.format` para centrar el texto.
      - `imprimirLinea()`: imprime 60 caracteres `─` seguidos de un salto de línea.
      - `imprimirTablaEncabezado(String... columnas)`: imprime una fila de encabezado con `|` como separador y las columnas en mayúsculas.
      - `imprimirFila(String... valores)`: imprime una fila de datos con `|` como separador, alineando con `String.format`.
      - `imprimirComprobante(Venta venta)`: imprime el comprobante completo con bordes, sección del cliente, lista de ítems (celular, cantidad, subtotal), total sin IVA, valor del IVA (19%) y total con IVA, todos alineados a la derecha.
    - **Archivos a crear:** `ConsolaUtils.java`
    - **Depende de:** 3.5
    - **Criterio de aceptación:** llamar a `imprimirTitulo("MENÚ PRINCIPAL")` imprime el texto enmarcado en consola sin errores.
    - **Estimado:** 2 h
    - _Requerimientos: 11.1, 11.2, 11.3, 11.4, 11.5_

  - [ ] 6.5 Crear `ReporteUtils`
    - Crea `ReporteUtils.java` en `com.tecnostore.util`. Todos los métodos son **estáticos** y reciben listas de datos ya cargadas (no hacen SQL).
    - Implementa los tres métodos usando **Stream API** (sin bucles `for` ni `while`):
      - `mostrarStockBajo(List<Celular> celulares)`: filtra `stock < 5`, ordena por stock ascendente, imprime con `ConsolaUtils.imprimirFila`. Si no hay resultados, imprime `"No hay datos disponibles para este reporte"`.
      - `mostrarTopMasVendidos(List<Venta> ventas)`: calcula unidades vendidas por modelo sumando cantidades de todos los `ItemVenta`, toma el mínimo entre 3 y el número de modelos con al menos 1 venta, ordena por total descendente (empate: alfabético por modelo). Si no hay ventas, imprime el mensaje de sin datos.
      - `mostrarVentasPorMes(List<Venta> ventas)`: agrupa por `MM/AAAA` usando `Collectors.groupingBy`, suma `precio_unitario × cantidad` por período, ordena cronológicamente. Si no hay ventas, imprime el mensaje de sin datos.
    - **Archivos a crear:** `ReporteUtils.java`
    - **Depende de:** 6.4, 3.5
    - **Criterio de aceptación:** con los datos de prueba del script SQL, `mostrarTopMasVendidos` muestra correctamente el ranking.
    - **Estimado:** 3 h
    - _Requerimientos: 5.1, 5.2, 5.3, 5.4, 5.5_

  - [ ] 6.6 Crear `ArchivoUtils`
    - Crea `ArchivoUtils.java` en `com.tecnostore.util`.
    - Declara la constante `NOMBRE_ARCHIVO = "reporte_ventas.dat"`.
    - Método `generarReporte(List<Venta> ventas)`:
      - Usa `try-with-resources` con `ObjectOutputStream` sobre `FileOutputStream` para serializar la lista completa.
      - Si la lista está vacía, serializa igual y muestra `"Reporte generado: no hay ventas registradas aún"`.
      - Al terminar exitosamente, muestra `"Archivo reporte_ventas.dat generado correctamente"` seguido de la ruta absoluta (usa `new File(NOMBRE_ARCHIVO).getAbsolutePath()`).
      - Si ocurre `IOException`, muestra `"No se pudo acceder al archivo de reporte"` e imprime la traza con `e.printStackTrace()` en `System.err`.
    - Método `leerReporte()`:
      - Usa `try-with-resources` con `ObjectInputStream` sobre `FileInputStream`.
      - Hace cast a `List<Venta>` con supresión del warning de unchecked cast.
      - Si el archivo no existe o hay `IOException` o `ClassNotFoundException`, muestra el mensaje de error e imprime traza en `System.err`. Retorna lista vacía en ese caso.
    - **Archivos a crear:** `ArchivoUtils.java`
    - **Depende de:** 3.5
    - **Criterio de aceptación:** generar el reporte y luego leerlo retorna la misma lista de ventas.
    - **Estimado:** 1 – 2 h
    - _Requerimientos: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 6.8_

  - [ ]* 6.7 Escribir prueba de propiedad para `ArchivoUtils` (Propiedad 9)
    - Crea `ArchivoUtilsPropiedadesTest.java` en el paquete de pruebas.
    - **Propiedad 9: Round-trip de serialización del reporte**
    - Genera listas de objetos `Venta` de forma aleatoria (incluyendo la lista vacía).
    - Verifica que `generarReporte(ventas)` seguido de `leerReporte()` retorne una lista con los mismos ids y totales.
    - **Valida: Requerimientos 6.1, 6.2**
    - **Archivos a crear:** `ArchivoUtilsPropiedadesTest.java`
    - **Depende de:** 6.6
    - **Estimado:** 1 h
    - _Requerimientos: 6.1, 6.2_

- [ ] 7. Fase 5 — Implementar los patrones de diseño
  - [ ] 7.1 Crear `FactoryCelular` *(recomendado)*
    - Crea `FactoryCelular.java` en `com.tecnostore.patron`.
    - Método estático `crear(String modelo, Marca marca, double precio, int stock, SistemaOperativo so, CategoriaGama gama)`:
      - Verifica que los datos sean válidos usando `Validador`. Si no son válidos, lanza `IllegalArgumentException` con un mensaje descriptivo.
      - Crea un objeto `Precio` con el valor recibido (nombre generado automáticamente, por ejemplo `"Precio " + marca.getNombre() + " " + modelo`).
      - Construye y retorna `new Celular(modelo, marca, so, gama, precio, stock)`.
    - **Archivos a crear:** `FactoryCelular.java`
    - **Depende de:** 3.4, 6.1
    - **Criterio de aceptación:** `FactoryCelular.crear(...)` con datos inválidos lanza `IllegalArgumentException`; con datos válidos retorna un `Celular` construido correctamente.
    - **Estimado:** 1 h
    - _Requerimientos: 2.10, 8.2_

  - [ ] 7.2 Crear `EstrategiaDescuento` y sus implementaciones *(recomendado)*
    - Crea la interfaz `EstrategiaDescuento.java` en `com.tecnostore.patron` con el método `double aplicar(double totalSinIva)`.
    - Crea `SinDescuento.java` que implementa la interfaz: el método `aplicar` retorna `totalSinIva` sin cambios.
    - Crea `DescuentoPorcentaje.java` que implementa la interfaz: tiene un constructor que recibe `double porcentaje`; el método `aplicar` retorna `Math.round(totalSinIva * (1 - porcentaje) * 100.0) / 100.0`.
    - **Archivos a crear:** `EstrategiaDescuento.java`, `SinDescuento.java`, `DescuentoPorcentaje.java`
    - **Depende de:** 1
    - **Criterio de aceptación:** `new DescuentoPorcentaje(0.10).aplicar(1000.00)` retorna `900.00`.
    - **Estimado:** 30 min
    - _Requerimientos: 8.3_

- [ ] 8. Fase 6 — Implementar los servicios (gestores)
  - [ ] 8.1 Crear `GestorCelulares`
    - Crea `GestorCelulares.java` en `com.tecnostore.servicio`.
    - Recibe `CelularDAO` en el constructor (inyección de dependencias).
    - Implementa:
      - `registrarCelular(...)`: llama a `Validador` para verificar precio y stock; usa `FactoryCelular.crear(...)` para construir el objeto; llama a `celularDAO.insertar(celular)`. Captura `SQLException` y muestra `"Error al acceder a la base de datos"`.
      - `buscarPorId(int id)`: delega a `celularDAO.buscarPorId`. Si no existe, retorna `null`.
      - `listarTodos()`: delega a `celularDAO.listarTodos()`. Si hay error de BD, retorna lista vacía.
      - `actualizarCelular(int id, ...)`: verifica que el id exista primero. Lanza `IllegalArgumentException` si no existe o los datos son inválidos. Delega a `celularDAO.actualizar`.
      - `eliminarCelular(int id)`: verifica que el id exista primero. Lanza `IllegalArgumentException` si no existe. Delega a `celularDAO.eliminar`.
    - Este gestor NO debe contener ninguna línea de SQL ni llamadas a `System.out.println` dentro de los métodos de lógica.
    - **Archivos a crear:** `GestorCelulares.java`
    - **Depende de:** 5.3, 7.1, 6.1
    - **Criterio de aceptación:** intentar registrar un celular con precio -1 lanza `IllegalArgumentException`; intentar eliminar un id inexistente también lanza `IllegalArgumentException`.
    - **Estimado:** 2 h
    - _Requerimientos: 2.1, 2.4, 2.5, 2.6, 2.7, 2.8, 2.11, 2.12, 8.4_

  - [ ] 8.2 Crear `GestorClientes`
    - Crea `GestorClientes.java` en `com.tecnostore.servicio`. Recibe `ClienteDAO` en el constructor.
    - Implementa:
      - `registrarCliente(String nombre, String identificacion, String correo, String telefono)`: valida todos los campos con `Validador` (nombre max 100, identificacion max 20, correo, teléfono). Si alguno es inválido, lanza `IllegalArgumentException` indicando cuál campo. Verifica duplicado con `clienteDAO.existeIdentificacion`. Si ya existe, lanza `IllegalArgumentException` con `"Ya existe un cliente con esa identificación"`. Si todo está bien, crea `new Cliente(...)` y llama a `clienteDAO.insertar`.
      - `buscarPorIdentificacion(String)`: delega a DAO; retorna `null` si no existe.
      - `listarTodos()`: delega a DAO.
    - **Archivos a crear:** `GestorClientes.java`
    - **Depende de:** 5.4, 6.1
    - **Criterio de aceptación:** registrar dos clientes con la misma identificación lanza excepción en el segundo intento; registrar con correo sin `@` lanza excepción.
    - **Estimado:** 2 h
    - _Requerimientos: 3.1, 3.3, 3.4, 3.7, 3.8, 8.4_

  - [ ] 8.3 Crear `GestorVentas`
    - Crea `GestorVentas.java` en `com.tecnostore.servicio`. Recibe `VentaDAO`, `ClienteDAO` y `CelularDAO` en el constructor.
    - Declara los atributos:
      - `private static final double TASA_IVA = 0.19`
      - `private static final AtomicInteger contadorVentasDelDia = new AtomicInteger(0)` (para concurrencia básica)
      - `private Map<Integer, Integer> stockComprometido` (clave = id celular, valor = cantidad acumulada en esta venta)
      - `private EstrategiaDescuento estrategiaDescuento` (inicializada con `new SinDescuento()`)
    - Implementa los métodos:
      - `iniciarSesionVenta()`: limpia el mapa `stockComprometido = new HashMap<>()`.
      - `setEstrategiaDescuento(EstrategiaDescuento e)`: asigna la estrategia.
      - `agregarItem(int idCelular, int cantidad)`: valida la cantidad con `Validador.esCantidadValida`. Carga el celular desde `celularDAO`. Calcula el stock disponible real: `celular.getStock() - stockComprometido.getOrDefault(idCelular, 0)`. Si `cantidad > stockDisponible`, lanza `IllegalStateException` con el mensaje que incluye cuánto hay disponible y cuánto ya está comprometido. Si hay suficiente, acumula en `stockComprometido`.
      - `calcularTotalConIva(List<ItemVenta> items)`: suma todos los subtotales, aplica la estrategia de descuento, luego multiplica por 1.19 y redondea a 2 decimales.
      - `confirmarVenta(String identificacionCliente, List<int[]> itemsSolicitados, Empleado empleado)` — marcado como `synchronized`:
        1. Busca el cliente en la BD. Si no existe, lanza `IllegalArgumentException`.
        2. Construye la lista de `ItemVenta` con cantidades validadas.
        3. Calcula el total con IVA usando `calcularTotalConIva`.
        4. Crea el objeto `Venta` y llama a `ventaDAO.insertarConTransaccion(venta)`.
        5. Si `VentaDAO` lanza `SQLException`, captura y lanza un `IllegalStateException` con `"Error al registrar la venta, no se realizaron cambios"`.
        6. Si todo fue bien, incrementa `contadorVentasDelDia`.
      - `listarTodas()`: delega a `ventaDAO.listarTodas()`.
      - `getContadorVentasDelDia()`: retorna el valor actual del `AtomicInteger`.
    - **Archivos a crear:** `GestorVentas.java`
    - **Depende de:** 5.5, 8.1, 8.2, 7.2
    - **Criterio de aceptación:** agregar un celular con stock 5 dos veces con cantidad 3 debe fallar en el segundo intento; confirmar una venta válida incrementa el contador del día.
    - **Estimado:** 4 h
    - _Requerimientos: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8, 4.9, 4.10, 4.11, 8.3, 9.1, 9.3_

  - [ ]* 8.4 Escribir pruebas de propiedad para los cálculos de ventas (Propiedades 4 y 7)
    - Crea `GestorVentasPropiedadesTest.java` en el paquete de pruebas. Usa mocks manuales para `VentaDAO`, `ClienteDAO` y `CelularDAO` (no necesitas Mockito, crea clases internas simples).
    - **Propiedad 4: Cálculo correcto de subtotal e IVA**
      - Genera precios (`@DoubleRange(min=0.01, max=999999.99)`) y cantidades (`@IntRange(min=1, max=100)`) aleatorios.
      - Verifica que `item.getSubtotal() == round(precio * cantidad, 2)` y que el total con IVA sea `round(subtotal * 1.19, 2)`.
      - **Valida: Requerimiento 4.5**
    - **Propiedad 7: Consistencia del stock comprometido**
      - Genera secuencias de cantidades para el mismo celular.
      - Verifica que la suma en `stockComprometido` sea exactamente la suma de cantidades agregadas exitosamente, y que se rechace correctamente cuando supera el stock disponible.
      - **Valida: Requerimientos 4.3, 4.4, 4.10**
    - **Archivos a crear:** `GestorVentasPropiedadesTest.java`
    - **Depende de:** 8.3
    - **Estimado:** 2 h
    - _Requerimientos: 4.3, 4.4, 4.5, 4.10_

- [ ] 9. Punto de control — Verificar que todo compila y las pruebas de propiedades pasan
  - Compila el proyecto completo en NetBeans (Build → Clean and Build). Corrige cualquier error antes de continuar.
  - Ejecuta las clases de prueba de jqwik disponibles hasta este punto.
  - Verifica que las siguientes clases están todas completas y sin errores: todas las del modelo, todos los DAOs, todos los gestores, todas las utilidades y los patrones.
  - Asegúrate de que todas las pruebas pasan. Pregunta si tienes dudas antes de continuar.

- [ ] 10. Fase 7 — Implementar `Main.java` y el menú de consola
  - [ ] 10.1 Crear la estructura base de `Main.java`
    - Crea `Main.java` en el paquete `com.tecnostore`.
    - En el método `main(String[] args)`:
      1. Obtiene la conexión con `ConexionDB.getInstancia().getConexion()`. Envuelve esto en `try-catch` para capturar `Exception`: si falla, imprime la causa y llama a `System.exit(1)`.
      2. Crea una instancia de cada DAO pasando la conexión: `CelularDAO`, `ClienteDAO`, `EmpleadoDAO`, `MarcaDAO`, `PrecioDAO`, `VentaDAO`.
      3. Crea una instancia de cada gestor pasando los DAOs correspondientes: `GestorCelulares`, `GestorClientes`, `GestorVentas`.
      4. Crea un `Scanner scanner = new Scanner(System.in)`.
      5. Llama al método `mostrarMenuPrincipal(scanner, ...)` en un bucle `while(true)`.
    - Implementa el método `mostrarMenuPrincipal(...)` que usa `ConsolaUtils.imprimirTitulo("TECNOSTORE")` y muestra las 5 opciones numeradas. Lee la opción con `scanner.nextLine()`. Usa `switch` para despachar a cada submenú. Si la opción no existe, muestra `"Opción no válida, intente de nuevo"`. Si la opción es `5`, cierra la conexión y sale.
    - **Archivos a crear:** `Main.java`
    - **Depende de:** 9
    - **Criterio de aceptación:** ejecutar el programa muestra el menú principal; escribir `5` cierra la aplicación limpiamente.
    - **Estimado:** 2 h
    - _Requerimientos: 1.1, 1.3, 1.5, 1.6_

  - [ ] 10.2 Implementar el submenú de Celulares en `Main.java`
    - Agrega el método `mostrarMenuCelulares(Scanner, GestorCelulares, MarcaDAO, PrecioDAO)` a `Main.java`.
    - Muestra opciones: 1. Registrar celular, 2. Listar todos, 3. Buscar por id, 4. Actualizar, 5. Eliminar, 6. Volver.
    - Para "Registrar celular": pide marca (lista las marcas disponibles primero), modelo, precio, stock, sistema operativo y gama. Llama a `gestorCelulares.registrarCelular(...)`. Envuelve en `try-catch` para `IllegalArgumentException` y muestra el mensaje de error sin salir del flujo.
    - Para "Listar todos": llama a `gestorCelulares.listarTodos()` y muestra con `ConsolaUtils.imprimirTablaEncabezado` + `imprimirFila`. Si la lista está vacía, muestra `"No hay celulares registrados"`.
    - Para "Buscar por id": pide el id, llama a `gestorCelulares.buscarPorId`. Si retorna `null`, muestra `"Celular no encontrado"`.
    - Para "Actualizar" y "Eliminar": pide el id, llama al método correspondiente, envuelve en `try-catch`.
    - **Archivos a modificar:** `Main.java`
    - **Depende de:** 10.1, 8.1
    - **Criterio de aceptación:** registrar un celular desde el menú lo muestra en la lista; eliminar un id inexistente muestra el mensaje de error y el menú vuelve a aparecer.
    - **Estimado:** 2 h
    - _Requerimientos: 2.1, 2.5, 2.6, 2.7, 2.8, 2.9_

  - [ ] 10.3 Implementar el submenú de Clientes en `Main.java`
    - Agrega el método `mostrarMenuClientes(Scanner, GestorClientes)` a `Main.java`.
    - Muestra opciones: 1. Registrar cliente, 2. Listar todos, 3. Buscar por identificación, 4. Volver.
    - Para "Registrar cliente": pide los 4 campos. El correo se pide en un bucle con máximo **3 intentos**: si `Validador.esCorreoValido` falla, muestra el error y pide nuevamente; al tercer intento fallido, cancela el registro y regresa al menú.
    - Para "Listar todos" y "Buscar por identificación": usa `ConsolaUtils` para mostrar los datos. Si la lista está vacía, muestra `"No hay clientes registrados"`. Si no se encuentra, muestra `"Cliente no encontrado"`.
    - **Archivos a modificar:** `Main.java`
    - **Depende de:** 10.1, 8.2
    - **Criterio de aceptación:** registrar un correo inválido 3 veces cancela el registro; registrar un cliente duplicado muestra el mensaje de error.
    - **Estimado:** 2 h
    - _Requerimientos: 3.1, 3.3, 3.4, 3.5, 3.6_

  - [ ] 10.4 Implementar el submenú de Ventas en `Main.java`
    - Agrega el método `mostrarMenuVentas(Scanner, GestorVentas, EmpleadoDAO)` a `Main.java`.
    - Flujo de "Registrar venta":
      1. Llama a `gestorVentas.iniciarSesionVenta()`.
      2. Pide la identificación del cliente. Si `gestorVentas` no encuentra el cliente, muestra `"Cliente no encontrado"` y pide de nuevo (sin cancelar).
      3. Entra en un bucle donde el empleado puede agregar celulares: pide id del celular y cantidad. Llama a `gestorVentas.agregarItem(...)`. Si lanza `IllegalStateException`, muestra el mensaje y permite intentar con otro celular. Si lanza `NumberFormatException`, muestra `"Debe ingresar un número válido"`.
      4. Después de cada ítem, pregunta `"¿Agregar otro celular? (S/N)"`.
      5. Al confirmar con `"S"`, pregunta `"¿Confirmar la venta? (S/N)"`.
      6. Si confirma, determina el empleado (puede ser hardcoded como el primer empleado del sistema para este proyecto educativo) y llama a `gestorVentas.confirmarVenta(...)`. Si ocurre error, muestra el mensaje.
      7. Si la venta se confirma con éxito, llama a `ConsolaUtils.imprimirComprobante(venta)`.
    - Agrega la opción "Ver historial de ventas" que llama a `gestorVentas.listarTodas()` y muestra la lista.
    - **Archivos a modificar:** `Main.java`
    - **Depende de:** 10.1, 8.3
    - **Criterio de aceptación:** completar una venta con 2 celulares muestra el comprobante con cliente, ítems, subtotales, IVA y total; el stock de esos celulares disminuye en la base de datos.
    - **Estimado:** 3 h
    - _Requerimientos: 4.1, 4.2, 4.3, 4.4, 4.8_

  - [ ] 10.5 Implementar el submenú de Reportes en `Main.java`
    - Agrega el método `mostrarMenuReportes(Scanner, GestorCelulares, GestorVentas)` a `Main.java`.
    - Muestra opciones: 1. Stock bajo (< 5 unidades), 2. Top celulares más vendidos, 3. Ventas por mes, 4. Generar archivo reporte_ventas.dat, 5. Ver reporte desde archivo, 6. Volver.
    - Para las opciones 1, 2 y 3: carga los datos necesarios con los gestores y delega a `ReporteUtils`.
    - Para la opción 4: llama a `ArchivoUtils.generarReporte(gestorVentas.listarTodas())`.
    - Para la opción 5: llama a `ArchivoUtils.leerReporte()` y muestra los resultados con `ConsolaUtils`.
    - **Archivos a modificar:** `Main.java`
    - **Depende de:** 10.1, 6.5, 6.6
    - **Criterio de aceptación:** la opción de stock bajo lista solo los celulares con menos de 5 unidades; generar y luego leer el archivo muestra la misma información.
    - **Estimado:** 2 h
    - _Requerimientos: 5.1, 5.2, 5.3, 5.4, 5.5, 6.1, 6.4, 6.7_

- [ ] 11. Punto de control final — Prueba completa del sistema
  - Ejecuta el script `TecnoStoreAnt.sql` en MySQL Workbench para dejar la base de datos en estado limpio.
  - Arranca el programa y navega por todos los menús verificando que:
    - El menú principal aparece correctamente con formato.
    - Se puede registrar, listar, actualizar y eliminar celulares.
    - Se puede registrar y buscar clientes.
    - Se puede registrar una venta completa con comprobante.
    - Los reportes muestran datos correctos.
    - El archivo `.dat` se genera y se puede leer.
    - Los mensajes de error aparecen cuando se ingresan datos inválidos.
  - Asegúrate de que todas las pruebas de jqwik disponibles pasan.
  - Pregunta si tienes dudas antes de continuar.

- [ ] 12. Fase 8 — Crear los entregables finales
  - [ ] 12.1 Escribir pruebas de propiedad restantes (Propiedades 3, 5, 6 y 8)
    - Crea o completa los archivos de prueba para las propiedades que aún no tienen test:
    - **Propiedad 3: Unicidad de identificación de cliente** — en `GestorClientesPropiedadesTest.java`. Usa un `ClienteDAO` mock que simule la BD. Genera identificaciones aleatorias y verifica que registrar dos veces la misma lanza excepción y no duplica. **Valida: Requerimiento 3.4**
    - **Propiedad 5: Reducción correcta de stock** — en `VentaDAOPropiedadesTest.java`. Genera cantidades vendidas aleatorias y verifica que `stock_después = stock_antes - cantidad`. **Valida: Requerimiento 4.6**
    - **Propiedad 6: Atomicidad de transacción de venta** — en `VentaDAOPropiedadesTest.java`. Fuerza un fallo en el DAO y verifica que el estado es idéntico al estado anterior. **Valida: Requerimientos 4.7, 4.11**
    - **Propiedad 8: Corrección del filtro de stock bajo** — en `ReporteUtilsPropiedadesTest.java`. Genera listas aleatorias de celulares con stocks variados y verifica que el resultado contiene exactamente los de stock < 5, en orden correcto. **Valida: Requerimiento 5.1**
    - **Archivos a crear:** `GestorClientesPropiedadesTest.java`, `VentaDAOPropiedadesTest.java`, `ReporteUtilsPropiedadesTest.java`
    - **Depende de:** 11
    - **Estimado:** 3 h
    - _Requerimientos: 3.4, 4.6, 4.7, 4.11, 5.1_

  - [ ] 12.2 Crear el archivo `README.md`
    - Crea `README.md` en la raíz del proyecto con las siguientes secciones:
      1. **Descripción del proyecto**: qué es TecnoStore y qué hace el sistema.
      2. **Estructura de carpetas**: muestra el árbol de paquetes y archivos principales.
      3. **Cómo importar la base de datos**: pasos para ejecutar `TecnoStoreAnt.sql` en MySQL Workbench.
      4. **Cómo configurar la conexión**: cómo editar `config.properties` con los datos locales.
      5. **Cómo ejecutar el proyecto en NetBeans**: pasos desde File → Open Project hasta Run.
      6. **Ejemplo de uso del menú**: captura de texto mostrando el flujo de registrar una venta.
    - **Archivos a crear:** `README.md`
    - **Depende de:** 11
    - **Criterio de aceptación:** alguien que no conoce el proyecto puede seguir el README y ejecutarlo desde cero.
    - **Estimado:** 1 – 2 h
    - _Requerimientos: 10.2_

---

## Notes

- Las tareas marcadas con `*` son opcionales. Se pueden saltar si el objetivo es llegar a un MVP funcional primero y agregar las pruebas después.
- Cada tarea referencia los requerimientos específicos que satisface para facilitar el seguimiento.
- Los patrones **Factory** y **Strategy** están marcados como recomendados en el diseño — su implementación (tareas 7.1 y 7.2) es obligatoria en este plan porque el resto del código los usa; lo que es opcional es agregarles funcionalidad extra.
- El patrón **Singleton** (tarea 4.2) es **obligatorio**.
- Los checkpoints (tareas 9 y 11) son buenos momentos para hacer commit en git si usas control de versiones.
- Si encuentras que un DAO no devuelve los datos esperados, revisa primero el script SQL y verifica con MySQL Workbench que los datos de prueba existen.

---

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1"] },
    { "id": 1, "tasks": ["2.1", "4.1"] },
    { "id": 2, "tasks": ["3.1", "4.2"] },
    { "id": 3, "tasks": ["3.2", "3.3", "6.1", "7.2"] },
    { "id": 4, "tasks": ["3.4", "5.1", "6.4"] },
    { "id": 5, "tasks": ["3.5", "5.2", "6.2", "6.3"] },
    { "id": 6, "tasks": ["5.3", "5.4", "6.5", "6.6", "7.1"] },
    { "id": 7, "tasks": ["5.5", "6.7", "8.1", "8.2"] },
    { "id": 8, "tasks": ["8.3"] },
    { "id": 9, "tasks": ["8.4", "10.1"] },
    { "id": 10, "tasks": ["10.2", "10.3", "10.4", "10.5"] },
    { "id": 11, "tasks": ["12.1", "12.2"] }
  ]
}
```
