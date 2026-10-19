# TecnoStore - Sistema de Gestión

Aplicación de consola en Java 21 + MySQL para gestión de ventas, inventario y
clientes.

El proyecto está pensado para ejecutarse en **cualquier PC** que clone el
repositorio, sin depender de las credenciales de MySQL de una máquina en
concreto.

---

## Requisitos

- **Java 21** (JDK). Verifícalo con: `java -version`
- **MySQL Server** en ejecución (local o remoto).
- No necesitas instalar Maven: el proyecto incluye el **Maven Wrapper**
  (`mvnw` / `mvnw.cmd`), que descarga Maven automáticamente la primera vez.

---

## 1. Crear la base de datos

Ejecuta el script SQL una sola vez en tu servidor MySQL. Crea la base de datos
`TecnoStoreAnt` con todas sus tablas y datos iniciales.

**MySQL Workbench:** abre `mavenproject1/sql/TecnoStoreAnt.sql` y ejecútalo.

**Línea de comandos:**

```bash
mysql -u TU_USUARIO -p < mavenproject1/sql/TecnoStoreAnt.sql
```

---

## 2. Configurar TUS credenciales de MySQL

Cada persona tiene su propio usuario y contraseña de MySQL. Para que el programa
use los tuyos **sin modificar archivos compartidos**, tienes dos opciones:

### Opción A (recomendada): archivo local

1. Ve a `mavenproject1/src/main/resources/`
2. Copia `config.local.properties.example` y renómbralo a
   **`config.local.properties`**
3. Pon ahí tu usuario y contraseña reales:

   ```properties
   db.host=localhost
   db.puerto=3306
   db.nombre=TecnoStoreAnt
   db.usuario=root
   db.contrasena=TU_CONTRASENA
   ```

Este archivo está en `.gitignore`, así que **tus credenciales nunca se suben al
repositorio** ni afectan a los demás.

### Opción B: variables de entorno

Define estas variables antes de ejecutar (útil en servidores o CI):

| Variable        | Ejemplo         |
| --------------- | --------------- |
| `DB_HOST`       | `localhost`     |
| `DB_PUERTO`     | `3306`          |
| `DB_NOMBRE`     | `TecnoStoreAnt` |
| `DB_USUARIO`    | `root`          |
| `DB_CONTRASENA` | `mi_clave`      |

**Prioridad de configuración:** variables de entorno → `config.local.properties`
→ `config.properties` (plantilla con valores por defecto).

---

## 3. Ejecutar el programa

Desde la carpeta `mavenproject1`:

**Windows:**

```powershell
.\mvnw.cmd compile exec:java
```

**Linux / Mac:**

```bash
./mvnw compile exec:java
```

> Si `mvnw` falla con *"JAVA_HOME not found"*, define la variable apuntando a tu
> JDK 21. Ejemplo en Windows PowerShell:
> `$env:JAVA_HOME='C:\Program Files\Java\jdk-21'`

También puedes abrir el proyecto directamente en **NetBeans / IntelliJ /
Eclipse**, que traen su propio Maven, y ejecutar la clase `com.tecnostore.Main`.

---

## Notas

- `config.properties` es solo una **plantilla versionada** con valores por
  defecto. No pongas tu contraseña personal ahí.
- `config.local.properties` es **personal de cada desarrollador** y git lo
  ignora.
