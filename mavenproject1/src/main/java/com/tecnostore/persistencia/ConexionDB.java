package com.tecnostore.persistencia;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

// Abre una sola conexion a MySQL y la reutiliza en todo el programa.
//
// Cada persona pone su usuario y contrasena en su propio archivo
// config.local.properties (que no se sube a git). Si no lo crea, se usan
// los valores de config.properties o las variables de entorno.
// Orden: variables de entorno, luego config.local.properties, luego config.properties.
public class ConexionDB {

    private static ConexionDB instancia = null;
    private Connection conexion;

    private ConexionDB() {
        try {
            Properties props = cargarConfiguracion();

            String host       = valor(props, "db.host",       "DB_HOST",       "localhost");
            String puerto     = valor(props, "db.puerto",     "DB_PUERTO",     "3306");
            String nombre     = valor(props, "db.nombre",     "DB_NOMBRE",     "TecnoStoreAnt");
            String usuario    = valor(props, "db.usuario",    "DB_USUARIO",    "root");
            String contrasena = valor(props, "db.contrasena", "DB_CONTRASENA", "");

            if (usuario == null || usuario.isBlank()) {
                throw new RuntimeException(
                    "Falta el usuario de MySQL. Define db.usuario en " +
                    "config.local.properties o la variable de entorno DB_USUARIO."
                );
            }

            String url = "jdbc:mysql://" + host + ":" + puerto + "/" + nombre
                    + "?useSSL=false"
                    + "&allowPublicKeyRetrieval=true"
                    + "&serverTimezone=America/Bogota"
                    + "&characterEncoding=UTF-8";

            this.conexion = DriverManager.getConnection(url, usuario, contrasena);

        } catch (IOException e) {
            throw new RuntimeException(
                "Error al leer la configuracion de la base de datos: " + e.getMessage(), e
            );
        } catch (SQLException e) {
            throw new RuntimeException(
                "No se pudo conectar a la base de datos.\n" +
                "  - Verifica que MySQL este iniciado.\n" +
                "  - Crea el archivo src/main/resources/config.local.properties con TU usuario\n" +
                "    y TU contrasena de MySQL (copia config.properties como plantilla), o\n" +
                "    define las variables de entorno DB_USUARIO y DB_CONTRASENA.\n" +
                "  - Ejecuta el script sql/TecnoStoreAnt.sql para crear la base de datos.\n" +
                "Causa tecnica: " + e.getMessage(), e
            );
        }
    }

    // Junta los valores: primero la plantilla, luego el archivo local (que manda).
    private Properties cargarConfiguracion() throws IOException {
        Properties props = new Properties();

        try (InputStream base = getClass()
                .getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (base != null) {
                props.load(base);
            }
        }

        try (InputStream local = getClass()
                .getClassLoader()
                .getResourceAsStream("config.local.properties")) {
            if (local != null) {
                props.load(local);
            }
        }

        for (String ruta : new String[]{
                "config.local.properties",
                "src/main/resources/config.local.properties"}) {
            Path p = Path.of(ruta);
            if (Files.exists(p)) {
                try (InputStream fs = Files.newInputStream(p)) {
                    props.load(fs);
                }
            }
        }

        return props;
    }

    // Toma el valor de la variable de entorno; si no, del archivo; si no, el de reserva.
    private String valor(Properties props, String claveArchivo,
                         String claveEnv, String porDefecto) {
        String env = System.getenv(claveEnv);
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        String prop = props.getProperty(claveArchivo);
        if (prop != null && !prop.isBlank()) {
            return prop.trim();
        }
        return porDefecto;
    }

    // Devuelve la conexion, creandola la primera vez que se pide.
    public static synchronized ConexionDB getInstancia() {
        if (instancia == null) {
            instancia = new ConexionDB();
        }
        return instancia;
    }

    public Connection getConexion() {
        return conexion;
    }

    // Se llama al salir del programa
    public void cerrarConexion() {
        if (conexion != null) {
            try {
                conexion.close();
                instancia = null;
                System.out.println("Conexion a la base de datos cerrada correctamente.");
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexion: " + e.getMessage());
            }
        }
    }

    // Indica si la conexion sigue abierta
    public boolean estaConectado() {
        try {
            return conexion != null && !conexion.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
