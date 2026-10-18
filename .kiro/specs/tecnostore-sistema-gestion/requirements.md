# Requirements Document

## Introduction

TecnoStore es una tienda minorista de teléfonos celulares. Este documento describe los requerimientos del sistema de gestión que se va a construir en Java para reemplazar el manejo manual en hojas de cálculo.

El sistema permitirá controlar el inventario de celulares, registrar clientes, procesar ventas y generar reportes, todo desde la consola. Se conectará a una base de datos MySQL usando JDBC.

Este documento está organizado por módulos que coinciden con los temas que has estudiado, para que puedas ver directamente qué conocimiento aplicas en cada parte del sistema.

**Estimado total del proyecto:** 70 – 90 horas de desarrollo.

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
- **ArchivoUtils**: Clase utilitaria que escribe el archivo de texto con el resumen de ventas.
- **FactoryCelular**: Clase que crea objetos Celular siguiendo el patrón de diseño Factory.
- **Main**: Clase principal que arranca el sistema y muestra el menú de consola.
- **tecnostore_db**: Base de datos MySQL que almacena toda la información del sistema.

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

### Requerimiento 6: Persistencia en archivos de texto

**Temas aplicados:** Módulo 5 (manejo de archivos, try-with-resources), Módulo 2 (listas)

**Historia de usuario:** Como dueño de TecnoStore, quiero que el sistema genere automáticamente un archivo de texto con el resumen de ventas, para tener un respaldo físico del reporte.

**Estimado:** 4 – 6 horas

#### Criterios de aceptación

1. CUANDO el usuario selecciona la opción "Generar reporte de ventas" en el menú, EL ArchivoUtils DEBE crear o sobreescribir el archivo "reporte_ventas.txt" en el directorio de trabajo actual del proceso Java con el resumen de todas las ventas registradas en orden de registro.
2. EL archivo "reporte_ventas.txt" DEBE contener: la fecha de generación en formato DD/MM/YYYY HH:MM:SS, el listado de ventas con la fecha de cada venta en formato DD/MM/YYYY, el nombre del cliente y el total con IVA expresado con exactamente 2 decimales, y el gran total recaudado con exactamente 2 decimales al final.
3. EL ArchivoUtils DEBE usar try-with-resources al manejar el archivo para garantizar que el recurso se cierre correctamente aunque ocurra un error.
4. SI no hay ventas registradas al momento de generar el reporte, ENTONCES EL ArchivoUtils DEBE crear el archivo indicando que no hay ventas registradas y mostrar el mensaje "Archivo reporte_ventas.txt generado correctamente" con la ruta absoluta del archivo.
5. SI ocurre un error al escribir el archivo, ENTONCES EL ArchivoUtils DEBE mostrar el mensaje "No se pudo generar el archivo de reporte", imprimir la traza del error en la consola de errores y continuar la ejecución del programa sin terminar.
6. CUANDO el archivo se genera con éxito, EL Sistema DEBE mostrar el mensaje "Archivo reporte_ventas.txt generado correctamente" seguido de la ruta absoluta del archivo generado.

---

### Requerimiento 7: Persistencia con base de datos MySQL (JDBC)

**Temas aplicados:** Módulo 5 (JDBC), Módulo 3 (SOLID - principio de inversión de dependencias)

**Historia de usuario:** Como desarrollador, quiero que todos los datos se almacenen en MySQL usando JDBC, para que la información persista entre ejecuciones del programa.

**Estimado:** 10 – 12 horas

#### Criterios de aceptación

1. EL ConexionDB DEBE conectarse a la base de datos tecnostore_db en MySQL usando JDBC con los parámetros: host, puerto, usuario y contraseña.
2. CADA clase DAO (CelularDAO, ClienteDAO, VentaDAO) DEBE implementar las operaciones básicas: insertar, buscar por id, listar todos, actualizar y eliminar, usando PreparedStatement para evitar inyección SQL.
3. EL ConexionDB DEBE implementar el patrón Singleton para garantizar que solo haya una conexión activa durante toda la ejecución.
4. CUANDO se ejecuta el script tecnostore_db.sql, EL Sistema DEBE crear la base de datos tecnostore_db con las tablas: celulares, clientes, ventas y detalle_ventas con sus respectivas llaves primarias y foráneas.
5. CADA clase DAO DEBE manejar las excepciones de tipo SQLException usando bloques try-catch y mostrar mensajes de error comprensibles para el desarrollador.
6. EL CelularDAO, EL ClienteDAO y EL VentaDAO DEBEN depender de la interfaz de ConexionDB y no de su implementación concreta, aplicando el Principio de Inversión de Dependencias (DIP de SOLID).

---

### Requerimiento 8: Aplicación de patrones y principios SOLID

**Temas aplicados:** Módulo 3 (SOLID, patrones creacionales, estructurales y de comportamiento)

**Historia de usuario:** Como desarrollador estudiante, quiero que el código aplique los principios y patrones que he estudiado, para consolidar lo aprendido en el proyecto real.

**Estimado:** 6 – 8 horas (distribuido en toda la implementación)

#### Criterios de aceptación

1. EL FactoryCelular DEBE implementar el patrón Factory Method con un método estático que reciba los datos del celular y retorne un objeto Celular correctamente construido.
2. EL Sistema DEBE aplicar el Principio de Responsabilidad Única (SRP): cada clase debe tener una sola razón para cambiar (los Gestores manejan lógica, los DAOs manejan base de datos, los Utils generan reportes/archivos).
3. EL Sistema DEBE aplicar el Principio Abierto/Cerrado (OCP): las clases de reportes deben poder extenderse para nuevos tipos de reporte sin modificar el código existente.
4. EL Sistema DEBE aplicar el Principio de Sustitución de Liskov (LSP): si se usan clases que heredan de una clase base, los objetos hijos deben poder reemplazar a los padres sin romper el funcionamiento.
5. DONDE se requiera calcular descuentos o precios especiales, EL Sistema DEBE implementar el patrón Strategy para que el algoritmo de cálculo sea intercambiable sin modificar el GestorVentas.
6. EL Sistema DEBE aplicar encapsulamiento en todas las clases del modelo: todos los atributos deben ser privados y accesibles solo mediante getters y setters con validación básica.

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
| **Total** | | **69 – 92 horas** |
