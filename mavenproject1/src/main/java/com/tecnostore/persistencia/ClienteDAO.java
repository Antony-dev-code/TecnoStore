package com.tecnostore.persistencia;

import com.tecnostore.modelo.Cliente;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ClienteDAO - acceso a las tablas persona + cliente.
 * Al insertar, primero llama a PersonaDAO para obtener el id,
 * luego inserta ese id en la tabla cliente.
 */
public class ClienteDAO {

    private final Connection conexion;
    private final PersonaDAO personaDAO;

    public ClienteDAO(Connection conexion) {
        this.conexion   = conexion;
        this.personaDAO = new PersonaDAO(conexion);
    }

    /** Inserta en persona y luego en cliente. Asigna el id al objeto. */
    public void insertar(Cliente cliente) throws SQLException {
        int id = personaDAO.insertar(cliente);
        cliente.setId(id);
        String sql = "INSERT INTO cliente (id, rut) VALUES (?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (cliente.getRut() != null) ps.setString(2, cliente.getRut());
            else                          ps.setNull  (2, Types.VARCHAR);
            ps.executeUpdate();
        }
    }

    /** Busca un cliente por identificacion con JOIN persona+cliente. */
    public Cliente buscarPorIdentificacion(String identificacion) throws SQLException {
        String sql = "SELECT p.id, p.nombre, p.identificacion, p.telefono, p.correo, c.rut "
                   + "FROM persona p JOIN cliente c ON p.id = c.id "
                   + "WHERE p.identificacion = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, identificacion);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    /** Lista todos los clientes. */
    public List<Cliente> listarTodos() throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT p.id, p.nombre, p.identificacion, p.telefono, p.correo, c.rut "
                   + "FROM persona p JOIN cliente c ON p.id = c.id "
                   + "ORDER BY p.nombre";
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    /** Verifica si ya existe un cliente con esa identificacion. */
    public boolean existeIdentificacion(String identificacion) throws SQLException {
        String sql = "SELECT 1 FROM persona p JOIN cliente c ON p.id = c.id "
                   + "WHERE p.identificacion = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, identificacion);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        return new Cliente(
            rs.getInt   ("id"),
            rs.getString("nombre"),
            rs.getString("identificacion"),
            rs.getString("telefono"),
            rs.getString("correo"),
            rs.getString("rut")   // puede ser null
        );
    }
}