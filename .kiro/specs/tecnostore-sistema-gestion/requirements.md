# Requirements Document

## Introduction

TecnoStore es una tienda minorista de teléfonos celulares. Este documento describe los requerimientos del sistema de gestión que se va a construir en Java para reemplazar el manejo manual en hojas de cálculo.

El sistema permitirá controlar el inventario de celulares, registrar clientes, procesar ventas y generar reportes, todo desde la consola. Se conectará a una base de datos MySQL usando JDBC.

Este documento está organizado por módulos que coinciden con los temas que has estudiado, para que puedas ver directamente qué conocimiento aplicas en cada parte del sistema.

**Estimado total del proyecto:** 72 – 96 horas de desarrollo.

---

## Glossary

- **Sistema**: La aplicación de consola TecnoStore desarrollada en Java.
- **Celular**: Producto que vende la tienda. Tiene marca, modelo, precio, stock, sistema operativo y gama.
- **Cliente**: Persona que compra en la tienda. Tiene nombre, identificación única, correo y teléfono.
- **Venta**: Transacción que registra qué cliente compró qué celular(es), cuándo y por cuánto.
- **ItemVenta**: Línea de detalle dentro de una venta. Representa un celular y la cantidad comprada.
- **Gama**: Clasificación del celular: Alta, Media o Baja.
- **IVA**: Impuesto al Valor Agregado, fijo en 19% para todas las ventas.
- **Stock**: Cantidad de unidades disponibles de un celular en el inventario.
- **GestorCelulares**: Clase que contiene la lógica de negocio para manejar celulares.
- **GestorClientes**: Clase que contiene la lógica de negocio para manejar clientes.
- **GestorVentas**: Clase que contiene la lógica de negocio para registrar y consultar ventas.
- **DAO**: Clase que se encarga de hablar con la base de datos (Data Access Object).
- **ConexionDB**: Clase que abre y cierra la conexión a MySQL usando JDBC.
- **Validador**: Clase utilitaria que verifica que los datos ingresados sean correctos.
- **ReporteUtils**: Clase utilitaria que genera los reportes usando Stream API.
- **ArchivoUtils**: Clase utilitaria que escribe y lee el archivo binario serializado `.dat` con el resumen de ventas usando ObjectOutputStream y ObjectInputStream.
- **FactoryCelular**: Clase que crea objetos Celular siguiendo el patrón de diseño Factory.
- **Main**: Clase principal que arranca el sistema y muestra el menú de consola.
- **tecnostore_db**: Base de datos MySQL que almacena toda la información del sistema.
- **ConsolaUtils**: Clase utilitaria con métodos estáticos para dar formato estético a la consola (bordes, tablas, títulos).
- **TecnoStoreAnt**: Nombre de la base de datos MySQL normalizada del sistema.
- **Persona**: Tabla base en la base de datos de la que heredan Cliente y Empleado.

---

## Requirements

### Requerimiento 1: Configuración e inicio del sistema

**Temas aplicados:** Módulo 1 (variables, decisiones), Módulo 3 (Singleton), Módulo 5 (JDBC)

**Historia de usuario:** Como desarrollador, quiero que el sistema arranque correctamente y se conecte a la base de datos, para que el usuario pueda usar todas las funciones desde el menú principal.

**Estimado:** 4 – 6 horas

#### Criterios de aceptación

1. CUANDO el usuario ejecuta el programa, EL Sistema DEBE mostrar un menú principal numerado en consola con las opciones: 1. Celulares, 2. Clientes, 3. Ventas, 4. Reportes y 5. Salir.
2. CUANDO el sistema arranca, EL ConexionDB DEBE abrir una única conexión a la base de datos MySQL usando los parámetros leídos desde un archivo de configuración (host, puerto, usuario, contraseña, nombre de base de datos).
3. SI la conexión a la base de datos falla, ENTONCES EL Sistema DEBE mostrar un mensaje de error que indique la causa del fallo y terminar la ejecución sin lanzar excepciones no capturadas.
4. EL ConexionDB DEBE implementarse con el patrón Singleton, de modo que solo exista una instancia de conexión durante toda la ejecución del programa.
5. CUANDO el usuario selecciona la opción "Salir", EL Sistema DEBE cerrar la conexión a la base de datos y terminar la ejecución.
6. SI el usuario ingresa una opción que no existe en el menú, ENTONCES EL Sistema DEBE mostrar el mensaje "Opción no válida, intente de nuevo" y mostrar el menú otra vez.

---

### Requerimiento 2: Gestión de celulares

**Temas aplicados:** Módulo 2 (POO, encapsulamiento, enumeraciones, listas), Módulo 3 (SOLID, Factory), Módulo 5 (JDBC - CelularDAO)

**Historia de usuario:** Como empleado de TecnoStore, quiero registrar, actualizar, eliminar y listar los celulares del inventario, para que el inventario esté siempre actualizado y pueda consultarlo rápidamente.

**Estimado:** 10 – 14 horas

#### Criterios de aceptación

1. CUANDO el empleado ingresa los datos de un celular nuevo (marca, modelo, precio, stock, sistema operativo, gama), EL GestorCelulares DEBE guardar el celular en la base de datos usando CelularDAO.
2. EL Celular DEBE tener los atributos id, marca, modelo, precio, stock, sistemaOperativo y gama, todos privados y accesibles solo por métodos getter y setter; la marca y el modelo no deben superar 100 caracteres, el precio debe estar entre 0.01 y 999,999,999.99, y el stock entre 0 y 999,999.
3. LA CategoriaGama DEBE ser una enumeración (enum) con los valores ALTA, MEDIA y BAJA.
4. CUANDO el empleado registra un celular, EL Validador DEBE verificar que el precio sea mayor que cero y que el stock sea mayor o igual a cero; si la validación es exitosa, EL GestorCelulares DEBE guardar el celular.
5. SI el precio es menor o igual a cero o el stock es negativo, ENTONCES EL Sistema DEBE mostrar un mensaje indicando cuál campo es inválido y permitir que el empleado corrija los datos sin salir del flujo de registro.
6. CUANDO el empleado solicita actualizar un celular existente por su id, EL GestorCelulares DEBE actualizar los datos en la base de datos y mostrar en consola un mensaje que confirme que la operación fue exitosa.
7. SI el id del celular que se quiere actualizar o eliminar no existe, ENTONCES EL Sistema DEBE mostrar el mensaje "Celular no encontrado" y regresar al menú de celulares.
8. CUANDO el empleado solicita eliminar un celular por su id, EL GestorCelulares DEBE eliminar el registro de la base de datos y mostrar en consola un mensaje que confirme que la operación fue exitosa.
9. CUANDO el empleado solicita listar todos los celulares, EL GestorCelulares DEBE recuperar la lista completa desde la base de datos y mostrarla en consola con todos sus atributos; SI la lista está vacía, ENTONCES EL Sistema DEBE mostrar el mensaje "No hay celulares registrados".
10. EL FactoryCelular DEBE proveer un método estático que reciba los parámetros marca, modelo, precio, stock, sistemaOperativo y gama, y retorne un objeto Celular correctamente construido, aplicando el patrón Factory.
11. EL GestorCelulares DEBE aplicar el Principio de Responsabilidad Única (SRP de SOLID): solo debe contener lógica de negocio, no código de acceso a la base de datos.
12. SI ocurre una excepción de base de datos al guardar, actualizar o eliminar un celular, ENTONCES EL GestorCelulares DEBE mostrar el mensaje "Error al acceder a la base de datos" y el estado del inventario en memoria no debe cambiar.

---

### Requerimiento 3: Gestión de clientes

**Temas aplicados:** Módulo 2 (POO, encapsulamiento, listas, conjuntos para evitar duplicados), Módulo 3 (SOLID), Módulo 5 (JDBC - ClienteDAO)

**Historia de usuario:** Como empleado de TecnoStore, quiero registrar y consultar clientes, para que cada venta quede asociada a un cliente identificado.

**Estimado:** 8 – 10 horas

#### Criterios de aceptación

1. CUANDO el empleado registra un cliente nuevo, EL GestorClientes DEBE guardar los datos en la base de datos usando ClienteDAO; el nombre debe tener entre 1 y 100 caracteres, la identificación entre 1 y 20 caracteres, y el teléfono entre 7 y 15 dígitos.
2. EL Cliente DEBE tener todos sus atributos privados y solo accesibles mediante getters y setters.
3. CUANDO el empleado ingresa el correo de un cliente, EL Validador DEBE verificar que el correo contenga exactamente un "@", al menos un carácter antes del "@" y al menos un "." después del "@" con caracteres en ambos lados; SI el formato es incorrecto, EL Sistema DEBE mostrar un mensaje de error y pedir el correo nuevamente, hasta un máximo de 3 intentos.
4. CUANDO el empleado intenta registrar un cliente con un número de identificación que ya existe en la base de datos, EL GestorClientes DEBE mostrar el mensaje "Ya existe un cliente con esa identificación" y no guardar el registro duplicado.
5. CUANDO el empleado solicita listar todos los clientes, EL GestorClientes DEBE recuperar la lista desde la base de datos y mostrarla en consola; SI no hay clientes registrados, EL Sistema DEBE mostrar el mensaje "No hay clientes registrados".
6. CUANDO el empleado busca un cliente por su número de identificación, EL GestorClientes DEBE retornar los datos del cliente encontrado o mostrar el mensaje "Cliente no encontrado".
7. EL GestorClientes DEBE aplicar el Principio de Responsabilidad Única: solo debe contener lógica de negocio, no código de acceso a la base de datos.
8. SI algún campo obligatorio (nombre, identificación, correo, teléfono) está vacío o es nulo al registrar un cliente, ENTONCES EL Sistema DEBE mostrar un mensaje indicando cuál campo falta y no guardar el registro.

---

### Requerimiento 4: Gestión de ventas

**Temas aplicados:** Módulo 2 (POO, relaciones entre clases, listas), Módulo 3 (SOLID, Strategy), Módulo 4 (conceptos de transacción atómica), Módulo 5 (JDBC - VentaDAO)

**Historia de usuario:** Como empleado de TecnoStore, quiero registrar ventas seleccionando un cliente y uno o más celulares, para que cada transacción quede guardada y el inventario se actualice automáticamente.

**Estimado:** 12 – 16 horas

#### Criterios de aceptación

1. CUANDO el empleado inicia una venta, EL GestorVentas DEBE pedir el número de identificación del cliente y validar que el cliente exista antes de continuar.
2. SI el número de identificación ingresado no corresponde a ningún cliente registrado, ENTONCES EL GestorVentas DEBE mostrar el mensaje "Cliente no encontrado" y permitir ingresar otro número sin cancelar la operación.
3. CUANDO el empleado agrega un celular a la venta indicando su id y la cantidad deseada, EL GestorVentas DEBE verificar que la cantidad sea un número entero entre 1 y 100, y que haya suficiente stock disponible.
4. SI el stock disponible es menor que la cantidad solicitada, ENTONCES EL Sistema DEBE mostrar el mensaje "Stock insuficiente" indicando la cantidad disponible, y permitir elegir otro celular o una cantidad menor.
5. CUANDO el empleado confirma la venta, EL GestorVentas DEBE calcular el subtotal de cada ItemVenta multiplicando precio por cantidad, calcular el total sumando todos los subtotales, y aplicar el IVA del 19% al total; todos los valores monetarios deben redondearse a exactamente 2 decimales.
6. CUANDO la venta es confirmada, EL GestorVentas DEBE guardar el registro de la venta en "ventas", cada ítem en "detalle_ventas" y reducir el stock de cada celular vendido, todo dentro de una sola transacción de base de datos usando VentaDAO.
7. SI ocurre un error durante el guardado de la venta, ENTONCES EL GestorVentas DEBE revertir todos los cambios (rollback) y mostrar el mensaje "Error al registrar la venta, no se realizaron cambios".
8. CUANDO la venta finaliza con éxito, EL Sistema DEBE mostrar un comprobante en consola con: nombre del cliente, lista de celulares comprados con cantidad y subtotal, total sin IVA, valor del IVA y total con IVA.
9. EL GestorVentas DEBE delegar el acceso a base de datos a VentaDAO y no debe contener instrucciones directas de acceso a consola dentro de la lógica de cálculo.
10. MIENTRAS el empleado agrega ítems a una misma venta, EL GestorVentas DEBE llevar un registro temporal del stock comprometido por cada celular en la sesión actual; SI el empleado intenta agregar un segundo pedido del mismo celular en la misma transacción y la suma total supera el stock disponible, ENTONCES EL Sistema DEBE mostrar el mensaje "No hay suficiente stock para este celular en esta venta (disponible: X, ya solicitado: Y)" y no agregar el ítem.
11. CUANDO la venta se confirma, EL GestorVentas DEBE verificar nuevamente que el stock de cada celular en la base de datos sea suficiente para cubrir la cantidad total acumulada en la transacción; SI algún celular ya no tiene stock suficiente al momento del commit, ENTONCES EL GestorVentas DEBE ejecutar rollback y mostrar el mensaje "No se pudo completar la venta: stock insuficiente al confirmar".

---

### Requerimiento 5: Reportes y análisis

**Temas aplicados:** Módulo 6 (Stream API, expresiones lambda), Módulo 2 (colecciones: listas y mapas)

**Historia de usuario:** Como dueño de TecnoStore, quiero ver reportes del negocio desde el menú, para tomar decisiones sobre el inventario y las ventas.

**Estimado:** 8 – 10 horas

#### Criterios de aceptación

1. CUANDO el usuario selecciona el reporte de stock bajo, EL ReporteUtils DEBE usar Stream API para filtrar y mostrar todos los celulares con stock estrictamente menor a 5 unidades, ordenados por cantidad de stock de menor a mayor.
2. CUANDO el usuario solicita el top de celulares más vendidos, EL ReporteUtils DEBE usar Stream API para mostrar los N celulares con mayor cantidad total de unidades vendidas en todos los períodos, donde N es el mínimo entre 3 y el número de modelos con al menos una venta, mostrando nombre del modelo y cantidad total; en caso de empate en unidades vendidas, se ordenan alfabéticamente por nombre del modelo.
3. CUANDO el usuario solicita el reporte de ventas por mes, EL ReporteUtils DEBE usar Stream API para agrupar las ventas por período (formato MM/AAAA), mostrar el total recaudado en cada período calculado como la suma del producto precio_unitario × cantidad de cada ítem, ordenado cronológicamente de más antiguo a más reciente.
4. EL ReporteUtils DEBE implementar todos los reportes usando expresiones lambda y métodos de Stream API (filter, map, sorted, collect, groupingBy), sin usar bucles for o while tradicionales.
5. SI la colección de datos requerida para generar un reporte está vacía o no contiene registros que satisfagan los criterios del reporte, ENTONCES EL ReporteUtils DEBE mostrar el mensaje "No hay datos disponibles para este reporte" sin lanzar excepción.

---

### Requerimiento 6: Persistencia en archivos

**Temas aplicados:** Módulo 5 (serialización de objetos, try-with-resources), Módulo 2 (listas)

**Historia de usuario:** Como dueño de TecnoStore, quiero que el sistema genere automáticamente un archivo serializado con el resumen de ventas y permita leerlo desde el menú, para tener un respaldo estructurado del reporte.

**Estimado:** 4 – 6 horas

#### Criterios de aceptación

1. CUANDO el usuario selecciona la opción "Generar reporte de ventas" en el menú, EL ArchivoUtils DEBE crear o sobreescribir el archivo "reporte_ventas.dat" en el directorio de trabajo actual usando serialización de objetos Java (ObjectOutputStream).
2. EL archivo "reporte_ventas.dat" DEBE contener una lista serializada de objetos Venta con todos sus datos, incluyendo la fecha en formato DD/MM/YYYY HH:MM:SS al momento de la generación.
3. EL ArchivoUtils DEBE usar try-with-resources con ObjectOutputStream y FileOutputStream para garantizar el cierre correcto del archivo aunque ocurra un error.
4. EL ArchivoUtils DEBE proveer también un método `leerReporte()` que use ObjectInputStream para deserializar el archivo "reporte_ventas.dat" y retornar la lista de Venta leída; CUANDO el usuario seleccione "Ver reporte" en el menú, EL Sistema DEBE llamar este método y mostrar el contenido en consola con formato estético (tabla con bordes).
5. SI no hay ventas registradas al momento de generar el reporte, ENTONCES EL ArchivoUtils DEBE serializar una lista vacía, escribir el archivo igualmente y mostrar el mensaje "Reporte generado: no hay ventas registradas aún".
6. SI ocurre un error al escribir o leer el archivo, ENTONCES EL ArchivoUtils DEBE mostrar el mensaje "No se pudo acceder al archivo de reporte", imprimir la traza del error en la consola de errores y continuar sin terminar el programa.
7. CUANDO el archivo se genera con éxito, EL Sistema DEBE mostrar el mensaje "Archivo reporte_ventas.dat generado correctamente" seguido de la ruta absoluta del archivo.
8. LA clase Venta, la clase Cliente, la clase Celular y la clase ItemVenta DEBEN implementar la interfaz java.io.Serializable para poder ser serializadas.

---

### Requerimiento 7: Persistencia con base de datos MySQL (JDBC)

**Temas aplicados:** Módulo 5 (JDBC), Módulo 3 (SOLID - principio de inversión de dependencias)

**Historia de usuario:** Como desarrollador, quiero que todos los datos se almacenen en MySQL usando JDBC, para que la información persista entre ejecuciones del programa.

**Estimado:** 10 – 12 horas

#### Criterios de aceptación

1. EL ConexionDB DEBE conectarse a la base de datos **TecnoStoreAnt** en MySQL usando JDBC con los parámetros leídos desde un archivo de configuración (host, puerto, usuario, contraseña).
2. EL script "TecnoStoreAnt.sql" DEBE crear la base de datos con las siguientes tablas normalizadas y sus respectivas llaves primarias y foráneas:
   - **persona** (id INT AUTO_INCREMENT PK, nombre VARCHAR(100), identificacion VARCHAR(20) UNIQUE, telefono VARCHAR(15), correo VARCHAR(255))
   - **empleado** (id INT PK, FK → persona.id, tipo ENUM('CAJERO','BODEGUERO','ADMINISTRADOR'))
   - **cliente** (id INT PK, FK → persona.id, rut VARCHAR(20) NULL)
   - **precio** (id INT AUTO_INCREMENT PK, nombre VARCHAR(100), valor DECIMAL(15,2))
   - **marca** (id INT AUTO_INCREMENT PK, nombre VARCHAR(100) UNIQUE)
   - **celular** (id INT AUTO_INCREMENT PK, modelo VARCHAR(100), id_marca_fk INT FK → marca.id, sistema_operativo ENUM('ANDROID','IOS'), gama ENUM('ALTA','MEDIA','BAJA'), id_precio_fk INT FK → precio.id, stock INT DEFAULT 0)
   - **venta** (id INT AUTO_INCREMENT PK, id_cliente_fk INT FK → cliente.id, fecha DATETIME, total DECIMAL(15,2), id_empleado_fk INT FK → empleado.id)
   - **detalle_venta** (id INT AUTO_INCREMENT PK, id_venta_fk INT FK → venta.id, id_celular_fk INT FK → celular.id, cantidad INT, subtotal DECIMAL(15,2))
3. CADA clase DAO DEBE implementar las operaciones básicas usando PreparedStatement para evitar inyección SQL.
4. LA herencia de persona en empleado y cliente DEBE implementarse en Java cargando primero los datos de persona y luego los datos de la subclase en una sola operación coordinada por el DAO correspondiente.
5. CADA clase DAO DEBE manejar las excepciones de tipo SQLException con bloques try-catch y mostrar mensajes comprensibles.
6. CUANDO se ejecute el script TecnoStoreAnt.sql en MySQL Workbench, DEBE crear la base de datos completa con datos de prueba: 5 clientes, 3 empleados (1 cajero, 1 bodeguero, 1 administrador), 10 modelos de celular con 30 unidades de stock cada uno, al menos 2 ventas de prueba con sus detalles.

---

### Requerimiento 8: Aplicación de patrones y principios SOLID

**Temas aplicados:** Módulo 3 (SOLID, patrones creacionales, estructurales y de comportamiento)

**Historia de usuario:** Como desarrollador estudiante, quiero que el código aplique los principios SOLID y al menos un patrón de diseño recomendado, para consolidar lo aprendido sin sobre-ingeniería.

**Estimado:** 6 – 8 horas (distribuido en toda la implementación)

#### Criterios de aceptación

1. EL Sistema DEBE implementar obligatoriamente el patrón Singleton en ConexionDB: solo debe existir una instancia de conexión durante toda la ejecución del programa.
2. SE RECOMIENDA implementar el patrón Factory en FactoryCelular para centralizar la construcción de objetos Celular; si se implementa, DEBE proveer un método estático que reciba los datos y retorne un Celular construido correctamente.
3. SE RECOMIENDA implementar el patrón Strategy para el cálculo de descuentos o precios especiales; si se implementa, DEBE usar una interfaz EstrategiaDescuento con implementaciones intercambiables (SinDescuento, DescuentoPorcentaje), de modo que GestorVentas no necesite modificarse para cambiar la forma de calcular.
4. EL Sistema DEBE aplicar el Principio de Responsabilidad Única (SRP): los Gestores manejan lógica de negocio, los DAOs manejan base de datos, los Utils manejan archivos y reportes.
5. EL Sistema DEBE aplicar el Principio Abierto/Cerrado (OCP): las clases de reportes deben poder extenderse para nuevos tipos sin modificar el código existente.
6. EL Sistema DEBE aplicar encapsulamiento en todas las clases del modelo: atributos privados, accesibles solo mediante getters y setters.

---

### Requerimiento 9: Concurrencia básica

**Temas aplicados:** Módulo 4 (introducción a concurrencia, sincronización)

**Historia de usuario:** Como desarrollador, quiero que las operaciones críticas sobre el stock sean seguras si en el futuro el sistema se usa con múltiples hilos, para evitar errores de inventario.

**Estimado:** 3 – 4 horas

#### Criterios de aceptación

1. EL GestorVentas DEBE marcar el método de actualización de stock como sincronizado (synchronized) para garantizar que dos operaciones no modifiquen el stock al mismo tiempo.
2. EL ConexionDB DEBE implementarse de forma thread-safe en su patrón Singleton, de modo que si dos hilos intentan obtener la instancia al mismo tiempo, solo se cree una sola conexión.
3. EL Sistema DEBE usar tipos atómicos (como AtomicInteger) para contadores internos que puedan ser accedidos por múltiples hilos, como el contador de ventas del día.

---

### Requerimiento 10: Entregables del proyecto

**Temas aplicados:** Todos los módulos (integración final)

**Historia de usuario:** Como desarrollador estudiante, quiero tener todos los entregables organizados al finalizar el proyecto, para presentar un trabajo completo y documentado.

**Estimado:** 4 – 6 horas

#### Criterios de aceptación

1. EL Sistema DEBE incluir el archivo "tecnostore_db.sql" con el script completo para crear la base de datos, las tablas y datos de ejemplo (al menos 5 celulares, 3 clientes y 2 ventas de prueba).
2. EL Sistema DEBE incluir un archivo "README.md" que explique en lenguaje simple: descripción del proyecto, estructura de carpetas, cómo importar la base de datos en MySQL Workbench, cómo ejecutar el proyecto en NetBeans y un ejemplo de uso del menú principal.
3. EL Sistema DEBE generar el archivo "reporte_ventas.txt" automáticamente cuando se solicite desde el menú de reportes.
4. EL Sistema DEBE compilar y ejecutarse sin errores en Java 21 con NetBeans IDE.
5. EL código fuente DEBE estar organizado en paquetes que reflejen la arquitectura del sistema: modelo, servicio, persistencia, util y patron.

---

### Requerimiento 11: Presentación estética en consola

**Temas aplicados:** Módulo 1 (cadenas y conversiones), Módulo 4 (utilidades de texto)

**Historia de usuario:** Como usuario del sistema, quiero que los menús, tablas y mensajes en consola tengan un formato visual claro y ordenado, para que el uso del sistema sea cómodo y fácil de leer.

**Estimado:** 3 – 4 horas

#### Criterios de aceptación

1. EL Sistema DEBE mostrar cada menú enmarcado con bordes ASCII (líneas de guiones o ═) con el título centrado, los ítems numerados y separadores entre secciones.
2. CUANDO el Sistema muestra una lista de celulares, clientes o ventas, DEBE presentarlos en formato de tabla con columnas alineadas usando espaciado fijo o String.format(), con una línea de encabezado y una línea separadora de guiones.
3. CUANDO el Sistema muestra el comprobante de una venta, DEBE incluir bordes, separadores entre secciones (cliente, ítems, totales) y alinear los valores monetarios a la derecha.
4. EL Sistema DEBE usar saltos de línea entre secciones para evitar que los bloques de información queden pegados visualmente.
5. EL Sistema DEBE centralizar toda la lógica de formato en una clase utilitaria `ConsolaUtils` con métodos estáticos como `imprimirTitulo(String titulo)`, `imprimirLinea()`, `imprimirTablaEncabezado(String... columnas)` y `imprimirFila(String... valores)`.

---

## Resumen de estimación por módulo

| Requerimiento | Temas principales | Horas estimadas |
|---|---|---|
| 1. Inicio y conexión | Módulo 1, 3, 5 | 4 – 6 h |
| 2. Gestión de celulares | Módulo 2, 3, 5 | 10 – 14 h |
| 3. Gestión de clientes | Módulo 2, 3, 5 | 8 – 10 h |
| 4. Gestión de ventas | Módulo 2, 3, 4, 5 | 12 – 16 h |
| 5. Reportes y análisis | Módulo 6 | 8 – 10 h |
| 6. Archivos de texto | Módulo 5 | 4 – 6 h |
| 7. Base de datos JDBC | Módulo 5, 3 | 10 – 12 h |
| 8. Patrones y SOLID | Módulo 3 | 6 – 8 h |
| 9. Concurrencia básica | Módulo 4 | 3 – 4 h |
| 10. Entregables | Todos | 4 – 6 h |
| 11. Consola estética | Módulo 1 | 3 – 4 h |
| **Total** | | **72 – 96 horas** |
