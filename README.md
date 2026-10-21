# 📱 TecnoStore - Sistema de Gestión

Aplicación de consola en **Java 21 + MySQL** para administrar el día a día de una
tienda de celulares: inventario, clientes, ventas y reportes.

El proyecto está pensado para ejecutarse en **cualquier PC** que clone el
repositorio, sin depender de las credenciales de MySQL de una máquina en
concreto.

---

> ## 🚀 ¿Vienes a ponerlo en marcha? Empieza aquí
>
> Lo más importante para arrancar el proyecto en tu equipo está en dos secciones:
>
> ### 👉 [🔌 Enlazar la base de datos en cualquier PC](#-enlazar-la-base-de-datos-en-cualquier-pc)
> ### 👉 [▶️ Cómo ejecutar el programa](#️-cómo-ejecutar-el-programa)
>
> Ahí explicamos cómo cada persona conecta su propia base de datos **sin tocar la
> configuración de los demás** y cómo correr la aplicación paso a paso.

---

## 📑 Tabla de contenido

- [🎯 Qué problemas resuelve](#-qué-problemas-resuelve)
- [📋 Requerimientos funcionales](#-requerimientos-funcionales)
- [🏗️ Arquitectura y módulos](#️-arquitectura-y-módulos)
- [🛠️ Tecnologías](#️-tecnologías)
- [✅ Requisitos previos](#-requisitos-previos)
- [🗄️ Crear la base de datos](#️-crear-la-base-de-datos)
- [🔌 Enlazar la base de datos en cualquier PC](#-enlazar-la-base-de-datos-en-cualquier-pc) ⭐
- [▶️ Cómo ejecutar el programa](#️-cómo-ejecutar-el-programa) ⭐
- [📝 Notas finales](#-notas-finales)

---

## 🎯 Qué problemas resuelve

- 📦 **Inventario a mano:** controla los celulares, su stock y sus precios en un
  solo lugar, evitando hojas de cálculo sueltas.
- 🧾 **Ventas sin registro:** cada venta queda guardada con su cliente, el empleado
  que la atendió, los productos y el total con IVA.
- 📊 **Falta de información para decidir:** genera reportes de stock bajo, productos
  más vendidos y ventas por mes.
- 🔒 **Stock que se descuadra:** al cerrar una venta, el descuento de stock y el
  guardado se hacen todo-o-nada, así nunca se vende algo que no existe.

---

## 📋 Requerimientos funcionales

Estos son los requerimientos que definimos al inicio y que el sistema cumple hoy.

### 📱 RF1 - Gestión de celulares (inventario)
- Registrar un celular nuevo (modelo, marca, sistema operativo, gama, precio y stock).
- Listar todos los celulares.
- Buscar un celular por su ID.
- Actualizar los datos de un celular existente.
- Eliminar un celular.
- Validar que modelo, precio y stock estén dentro de rangos permitidos antes de guardar.

### 👤 RF2 - Gestión de clientes
- Registrar un cliente nuevo (nombre, identificación, correo y teléfono).
- Listar todos los clientes.
- Buscar un cliente por su identificación.
- Validar el formato del correo, del teléfono y evitar identificaciones duplicadas.

### 🛒 RF3 - Registro de ventas
- Seleccionar el empleado que procesa la venta.
- Seleccionar el cliente (con reintentos si no se encuentra).
- Agregar varios celulares a la venta, controlando el stock disponible en tiempo real.
- Calcular el total aplicando descuento (si lo hay) y sumando el IVA (19%).
- Confirmar la venta: se guarda y descuenta el stock de forma segura.
- Mostrar un comprobante al finalizar.

### 📊 RF4 - Reportes
- Celulares con stock bajo (menos de 5 unidades).
- Top de celulares más vendidos.
- Ventas totales agrupadas por mes.

### 💾 RF5 - Reportes en archivo
- Exportar las ventas a un archivo `reporte_ventas.dat`.
- Volver a leer ese archivo y mostrar su contenido en pantalla.

### 🌍 RF6 - Portabilidad
- El programa debe correr en cualquier PC que clone el repositorio, con cada
  usuario configurando sus propias credenciales de MySQL sin tocar archivos
  compartidos.

---

## 🏗️ Arquitectura y módulos

El sistema sigue el patrón **MVC (Modelo - Vista - Controlador)**, que separa la
información, la pantalla y la coordinación. Los paquetes viven bajo
`com.tecnostore`.

```
com.tecnostore
├── Main.java            → arranca el programa y conecta las piezas
├── vista/               → lo que ve el usuario (menús, tablas, mensajes)
├── controlador/         → coordina el flujo entre la vista y el modelo
├── servicio/            → reglas del negocio (gestores)
├── persistencia/        → acceso a la base de datos (DAO) + conexión
├── modelo/              → las entidades (datos) del sistema
├── patron/              → patrones de diseño (Factory y Strategy)
└── util/                → utilidades (validación, consola, reportes, archivos)
```

### 🚪 `Main`
Punto de arranque. Abre la conexión, crea los DAO, los gestores, la vista y el
controlador, y le cede el control al menú. No contiene lógica de negocio.

### 🖥️ `vista/` - Presentación
`Vista` se encarga solo de **mostrar en pantalla y leer del teclado**: menús,
tablas de inventario y clientes, comprobantes y mensajes de éxito o error. No
sabe nada de la base de datos.

### 🎛️ `controlador/` - Coordinación
`Controlador` es el intermediario: lee la opción que elige el usuario y llama a
quien corresponda (los gestores). Contiene los bucles de los menús (principal,
celulares, clientes, ventas y reportes).

### ⚙️ `servicio/` - Reglas del negocio
Los **gestores** concentran la lógica:
- `GestorCelulares`: alta, baja, modificación y consulta de celulares.
- `GestorClientes`: registro y búsqueda de clientes, con validaciones.
- `GestorVentas`: arma la venta, controla el stock disponible, calcula el total
  con descuento e IVA y confirma la venta.

### 🗃️ `persistencia/` - Acceso a datos
Usa el patrón **DAO** (un objeto por entidad que sabe leer y escribir en MySQL):
`CelularDAO`, `ClienteDAO`, `VentaDAO`, `MarcaDAO`, `PrecioDAO`, `EmpleadoDAO`,
`PersonaDAO`. Aquí también vive `ConexionDB`, que gestiona la conexión.

`VentaDAO` hace la venta como una **transacción**: revisa el stock, guarda la
venta y sus líneas, y descuenta el inventario. Si algo falla, no se guarda nada.

### 📦 `modelo/` - Entidades
Las clases que representan los datos: `Persona` (base común de `Cliente` y
`Empleado`), `Celular`, `Marca`, `Precio`, `Venta`, `ItemVenta`, y los tipos
`SistemaOperativo`, `CategoriaGama`, `TipoEmpleado`. Son serializables, lo que
permite guardarlas en el archivo `.dat`.

### 🧩 `patron/` - Patrones de diseño
- **Factory** (`FactoryCelular`): crea celulares en un solo lugar, validando los
  datos antes de construir el objeto.
- **Strategy** (`EstrategiaDescuento`, `SinDescuento`, `DescuentoPorcentaje`):
  permite cambiar la forma de calcular el descuento sin tocar el gestor de ventas.

### 🧰 `util/` - Utilidades
- `Validador`: revisa precios, stock, correos, teléfonos, textos y cantidades.
- `ConsolaUtils`: da formato a títulos, tablas, precios y comprobantes.
- `ReporteUtils`: arma los reportes de stock bajo, más vendidos y ventas por mes.
- `ArchivoUtils`: guarda y lee el reporte de ventas en el archivo `.dat`.

---

## 🛠️ Tecnologías

- ☕ **Java 21** (LTS).
- 🐬 **MySQL 8** como base de datos.
- 🔗 **JDBC** (`mysql-connector-j`) para conectar Java con MySQL.
- 📦 **Maven** como gestor del proyecto, con **Maven Wrapper** incluido (no hace
  falta instalar Maven aparte).
- 🧪 **JUnit 5** y **jqwik** para pruebas.
- 💾 **Serialización de objetos** de Java para los reportes en archivo `.dat`.

Patrones aplicados: **MVC**, **Singleton** (una sola conexión a la base de
datos), **DAO**, **Factory** y **Strategy**.

---

## ✅ Requisitos previos

- ☕ **Java 21** (JDK). Verifícalo con `java -version`.
- 🐬 **MySQL Server** en ejecución (local o remoto).
- 📦 **No** necesitas instalar Maven: el proyecto trae el Maven Wrapper
  (`mvnw` / `mvnw.cmd`), que descarga Maven solo la primera vez.

---

## 🗄️ Crear la base de datos

Ejecuta el script SQL **una sola vez**. Crea la base de datos `TecnoStoreAnt` con
sus tablas y datos de prueba.

- 🖱️ **MySQL Workbench:** abre `mavenproject1/sql/TecnoStoreAnt.sql` y ejecútalo.
- ⌨️ **Línea de comandos:**

  ```bash
  mysql -u TU_USUARIO -p < mavenproject1/sql/TecnoStoreAnt.sql
  ```

---

# 🔌 Enlazar la base de datos en cualquier PC

> ⭐ **Esta es la parte clave del proyecto.** Explica cómo lograr que corra en el
> equipo de cualquiera que baje el repositorio.

El problema que resuelve: **cada persona tiene un usuario y una contraseña
distintos en MySQL**, así que no se puede dejar una contraseña fija escrita en el
código.

### 🔎 Cómo decide el programa qué credenciales usar

Al arrancar, `ConexionDB` busca los datos de conexión en este **orden de
prioridad** (gana el primero que encuentre):

1. 🌱 **Variables de entorno** del sistema.
2. 🔐 **`config.local.properties`** → tu archivo personal (ignorado por git).
3. 📄 **`config.properties`** → plantilla compartida con valores por defecto.

Así cada quien pone lo suyo sin pisar la configuración de los demás.

### ✨ Opción A (recomendada): archivo local

1. Ve a la carpeta `mavenproject1/src/main/resources/`.
2. Copia el archivo `config.local.properties.example` y renómbralo a
   **`config.local.properties`** (déjalo en esa misma carpeta).
3. Ábrelo y pon **tus** datos reales de MySQL:

   ```properties
   db.host=localhost
   db.puerto=3306
   db.nombre=TecnoStoreAnt
   db.usuario=root
   db.contrasena=TU_CONTRASENA
   ```

Qué significa cada campo:

| Campo           | Qué poner                                            |
| --------------- | ---------------------------------------------------- |
| `db.host`       | `localhost` si MySQL está en tu mismo PC.            |
| `db.puerto`     | `3306` (el puerto por defecto de MySQL).            |
| `db.nombre`     | `TecnoStoreAnt` (la base que crea el script SQL).   |
| `db.usuario`    | Tu usuario de MySQL (normalmente `root`).           |
| `db.contrasena` | La contraseña que pusiste al instalar MySQL.        |

> 🔒 `config.local.properties` está en `.gitignore`, así que **tu contraseña nunca
> se sube al repositorio** ni afecta a nadie más. Cada PC tiene el suyo.

### 🌱 Opción B: variables de entorno

Útil en servidores, despliegues o integración continua, donde no quieres
archivos con contraseñas. Define estas variables antes de ejecutar:

| Variable        | Ejemplo         |
| --------------- | --------------- |
| `DB_HOST`       | `localhost`     |
| `DB_PUERTO`     | `3306`          |
| `DB_NOMBRE`     | `TecnoStoreAnt` |
| `DB_USUARIO`    | `root`          |
| `DB_CONTRASENA` | `mi_clave`      |

### 🧪 Verificar que conectó

Al ejecutar el programa deberías ver:

```
[OK] Conexion establecida correctamente.
```

- ❌ Si aparece *"Access denied"*, la contraseña o el usuario no coinciden con tu
  MySQL: revisa tu `config.local.properties`.
- ⚠️ Si aparece que no se pudo conectar, confirma que el servicio de MySQL esté
  iniciado y que ya ejecutaste el script SQL de la sección anterior.

---

# ▶️ Cómo ejecutar el programa

> ⭐ Una vez creada la base de datos y configuradas tus credenciales, arrancar es
> así de simple.

Desde la carpeta `mavenproject1`:

🪟 **Windows:**

```powershell
.\mvnw.cmd compile exec:java
```

🐧 **Linux / 🍎 Mac:**

```bash
./mvnw compile exec:java
```

💡 También puedes abrir el proyecto en **NetBeans / IntelliJ / Eclipse** (traen su
propio Maven) y ejecutar la clase `com.tecnostore.Main`.

> ⚠️ Si al ejecutar con `mvnw` sale *"JAVA_HOME not found"*, define la variable
> apuntando a tu JDK 21. En Windows PowerShell:
> `$env:JAVA_HOME='C:\Program Files\Java\jdk-21'`

---

## 📝 Notas finales

- 📄 `config.properties` es solo una **plantilla compartida** con valores por
  defecto. No pongas tu contraseña personal ahí: ese archivo sí se sube a git.
- 🔐 `config.local.properties` es **personal de cada desarrollador** y git lo
  ignora.
- 🧹 Los archivos `.dat` (reportes generados) también están ignorados por git, así
  que cada quien genera los suyos al usar el programa y el repositorio se
  mantiene limpio.
