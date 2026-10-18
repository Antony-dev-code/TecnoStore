package com.tecnostore.persistencia;

import com.tecnostore.modelo.Persona;
import java.sql.*;

/**
 * PersonaDAO - acceso a la tabla persona
 *
 * Los clientes y empleados comparten datos en la tabla persona.
 * Este DAO los inserta ahi primero y retorna el id generado,
 * para que ClienteDAO y EmpleadoDAO lo usen en sus tablas.
 */
public class PersonaDAO {

    private final Connection conexion;

    public PersonaDAO(Connection conexion) {
        this.conexion = conexion;
    }

    /**
     * Inserta en la tabla persona y retorna el id AUTO_INCREMENT generado.
     * Ese id luego se usa en cliente o empleado.
     */
    public int insertar(Persona persona) throws SQLException {
        String sql = "INSERT INTO persona (nombre, identificacion, telefono, correo) "
                   + "VALUES (?, ?, ?, ?)";

        // RETURN_GENERATED_KEYS le dice a JDBC que queremos el id generado
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, persona.getNombre());
            ps.setString(2, persona.getIdentificacion());
            ps.setString(3, persona.getTelefono());
            ps.setString(4, persona.getCorreo());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1); // retorna el id asignado por MySQL
                }
            }
        }
        throw new SQLException("No se obtuvo el id generado para persona.");
    }

    /** Busca una persona por su id. Retorna null si no existe. */
    public Persona buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nombre, identificacion, telefono, correo "
                   + "FROM persona WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    /** Busca una persona por su numero de identificacion. Retorna null si no existe. */
    public Persona buscarPorIdentificacion(String identificacion) throws SQLException {
        String sql = "SELECT id, nombre, identificacion, telefono, correo "
                   + "FROM persona WHERE identificacion = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, identificacion);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    // Convierte una fila del ResultSet en un objeto Persona anonimo (solo para busquedas internas)
    private Persona mapear(ResultSet rs) throws SQLException {
        // Persona es abstracta, usamos una clase anonima para el mapeo interno
        final int    id             = rs.getInt("id");
        final String nombre         = rs.getString("nombre");
        final String identificacion = rs.getString("identificacion");
        final String telefono       = rs.getString("telefono");
        final String correo         = rs.getString("correo");

        return new Persona(id, nombre, identificacion, telefono, correo) {};
    }
}