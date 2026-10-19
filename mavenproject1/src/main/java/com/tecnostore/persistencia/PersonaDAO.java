package com.tecnostore.persistencia;

import com.tecnostore.modelo.Persona;
import java.sql.*;

// Guarda los datos comunes de clientes y empleados.
public class PersonaDAO {

    private final Connection conexion;

    public PersonaDAO(Connection conexion) {
        this.conexion = conexion;
    }

    // Guarda la persona y devuelve el id que le quedo asignado.
    public int insertar(Persona persona) throws SQLException {
        String sql = "INSERT INTO persona (nombre, identificacion, telefono, correo) "
                   + "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, persona.getNombre());
            ps.setString(2, persona.getIdentificacion());
            ps.setString(3, persona.getTelefono());
            ps.setString(4, persona.getCorreo());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("No se obtuvo el id generado para persona.");
    }

    // Busca una persona por su id.
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

    // Busca una persona por su identificacion.
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

    private Persona mapear(ResultSet rs) throws SQLException {
        final int    id             = rs.getInt("id");
        final String nombre         = rs.getString("nombre");
        final String identificacion = rs.getString("identificacion");
        final String telefono       = rs.getString("telefono");
        final String correo         = rs.getString("correo");

        return new Persona(id, nombre, identificacion, telefono, correo) {};
    }
}