package com.tecnostore.persistencia;

import com.tecnostore.modelo.Marca;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Acceso a la tabla marca. */
public class MarcaDAO {

    private final Connection conexion;

    public MarcaDAO(Connection conexion) {
        this.conexion = conexion;
    }

    public void insertar(Marca marca) throws SQLException {
        String sql = "INSERT INTO marca (nombre) VALUES (?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, marca.getNombre());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) marca.setId(rs.getInt(1));
            }
        }
    }

    public List<Marca> listarTodas() throws SQLException {
        List<Marca> lista = new ArrayList<>();
        String sql = "SELECT id, nombre FROM marca ORDER BY id";
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Marca(rs.getInt("id"), rs.getString("nombre")));
            }
        }
        return lista;
    }

    public Marca buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nombre FROM marca WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new Marca(rs.getInt("id"), rs.getString("nombre"));
            }
        }
        return null;
    }

    public Marca buscarPorNombre(String nombre) throws SQLException {
        String sql = "SELECT id, nombre FROM marca WHERE nombre = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new Marca(rs.getInt("id"), rs.getString("nombre"));
            }
        }
        return null;
    }
}