package com.tecnostore.persistencia;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * ConexionDB - Patron Singleton thread-safe
 *
 * Garantiza que solo exista UNA conexion a MySQL durante toda la ejecucion.
 *
 * Por que Singleton?
 * Abrir una conexion JDBC es costoso. Con Singleton la abrimos una sola vez
 * y la reutilizamos en toda la aplicacion.
 *
 * Por que synchronized?
 * Evita que dos hilos creen dos instancias al mismo tiempo.
 */
public class ConexionDB {

    // La unica instancia (empieza en null)
    private static ConexionDB instancia = null;

    // La conexion JDBC activa
    private Connection conexion;

    // -------------------------------------------------------------------------
    // Constructor PRIVADO: nadie puede hacer "new ConexionDB()" desde afuera
    // -------------------------------------------------------------------------
    private ConexionDB() {
        try {
            Properties props = new Properties();
            InputStream input = getClass()
                    .getClassLoader()
                    .getResourceAsStream("config.properties");

            if (input == null) {
                throw new RuntimeException(
                    "No se encontro config.properties en el classpath. " +
                    "Verifica que exista en src/main/resources/"
                );
            }

            props.load(input);
            input.close();

            String host       = props.getProperty("db.host");
            String puerto     = props.getProperty("db.puerto");
            String nombre     = props.getProperty("db.nombre");
            String usuario    = props.getProperty("db.usuario");
            String contrasena = props.getProperty("db.contrasena");

            String url = "jdbc:mysql://" + host + ":" + puerto + "/" + nombre
                    + "?useSSL=false"
                    + "&allowPublicKeyRetrieval=true"
                    + "&serverTimezone=America/Bogota"
                    + "&characterEncoding=UTF-8";

            this.conexion = DriverManager.getConnection(url, usuario, contrasena);

        } catch (IOException e) {
            throw new RuntimeException(
                "Error al leer config.properties: " + e.getMessage(), e
            );
        } catch (SQLException e) {
            throw new RuntimeException(
                "No se pudo conectar a la base de datos. " +
                "Verifica que MySQL este iniciado y que config.properties sea correcto. " +
                "Causa: " + e.getMessage(), e
            );
        }
    }

    // -------------------------------------------------------------------------
    // getInstancia() - unico punto de acceso; synchronized = thread-safe
    // -------------------------------------------------------------------------
    public static synchronized ConexionDB getInstancia() {
        if (instancia == null) {
            instancia = new ConexionDB();
        }
        return instancia;
    }

    // -------------------------------------------------------------------------
    // getConexion() - retorna el Connection para usar en los DAOs
    // -------------------------------------------------------------------------
    public Connection getConexion() {
        return conexion;
    }

    // -------------------------------------------------------------------------
    // cerrarConexion() - llamar desde Main cuando el usuario elija Salir
    // -------------------------------------------------------------------------
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

    // -------------------------------------------------------------------------
    // estaConectado() - verifica el estado antes de operar
    // -------------------------------------------------------------------------
    public boolean estaConectado() {
        try {
            return conexion != null && !conexion.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}