package com.tecnostore.persistencia;

import com.tecnostore.modelo.Precio;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Guarda y lee precios de la base de datos.
public class PrecioDAO {

    private final Connection conexion;

    public PrecioDAO(Connection conexion) {
        this.conexion = conexion;
    }

    public void insertar(Precio precio) throws SQLException {
        String sql = "INSERT INTO precio (nombre, valor) VALUES (?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, precio.getNombre());
            ps.setDouble(2, precio.getValor());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) precio.setId(rs.getInt(1));
            }
        }
    }

    public List<Precio> listarTodos() throws SQLException {
        List<Precio> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, valor FROM precio ORDER BY id";
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Precio(rs.getInt("id"), rs.getString("nombre"), rs.getDouble("valor")));
            }
        }
        return lista;
    }

    public Precio buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nombre, valor FROM precio WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new Precio(rs.getInt("id"), rs.getString("nombre"), rs.getDouble("valor"));
            }
        }
        return null;
    }
}