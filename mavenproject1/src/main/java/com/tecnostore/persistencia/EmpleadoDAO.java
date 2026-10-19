package com.tecnostore.persistencia;

import com.tecnostore.modelo.Empleado;
import com.tecnostore.modelo.TipoEmpleado;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Guarda y lee empleados de la base de datos.
public class EmpleadoDAO {

    private final Connection conexion;
    private final PersonaDAO personaDAO;

    public EmpleadoDAO(Connection conexion) {
        this.conexion   = conexion;
        this.personaDAO = new PersonaDAO(conexion);
    }

    public void insertar(Empleado empleado) throws SQLException {
        int id = personaDAO.insertar(empleado);
        empleado.setId(id);
        String sql = "INSERT INTO empleado (id, tipo) VALUES (?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt   (1, id);
            ps.setString(2, empleado.getTipo().name());
            ps.executeUpdate();
        }
    }

    // Busca un empleado por su id.
    public Empleado buscarPorId(int id) throws SQLException {
        String sql = "SELECT p.id, p.nombre, p.identificacion, p.telefono, p.correo, e.tipo "
                   + "FROM persona p JOIN empleado e ON p.id = e.id "
                   + "WHERE p.id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    public List<Empleado> listarTodos() throws SQLException {
        List<Empleado> lista = new ArrayList<>();
        String sql = "SELECT p.id, p.nombre, p.identificacion, p.telefono, p.correo, e.tipo "
                   + "FROM persona p JOIN empleado e ON p.id = e.id "
                   + "ORDER BY p.id";
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    private Empleado mapear(ResultSet rs) throws SQLException {
        return new Empleado(
            rs.getInt   ("id"),
            rs.getString("nombre"),
            rs.getString("identificacion"),
            rs.getString("telefono"),
            rs.getString("correo"),
            TipoEmpleado.valueOf(rs.getString("tipo"))
        );
    }
}