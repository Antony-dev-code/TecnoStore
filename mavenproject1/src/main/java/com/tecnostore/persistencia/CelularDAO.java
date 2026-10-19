package com.tecnostore.persistencia;

import com.tecnostore.modelo.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CelularDAO - acceso a la tabla celular.
 * Todos los SELECT hacen JOIN con marca y precio para obtener objetos completos.
 */
public class CelularDAO {

    private final Connection conexion;

    public CelularDAO(Connection conexion) {
        this.conexion = conexion;
    }

    /** Inserta un celular nuevo. Asigna el id generado al objeto. */
    public void insertar(Celular celular) throws SQLException {
        String sql = "INSERT INTO celular (modelo, id_marca_fk, sistema_operativo, gama, id_precio_fk, stock) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, celular.getModelo());
            ps.setInt   (2, celular.getMarca().getId());
            ps.setString(3, celular.getSistemaOperativo().name());
            ps.setString(4, celular.getGama().name());
            ps.setInt   (5, celular.getPrecio().getId());
            ps.setInt   (6, celular.getStock());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) celular.setId(rs.getInt(1));
            }
        }
    }

    /** Busca un celular por id con JOIN a marca y precio. Retorna null si no existe. */
    public Celular buscarPorId(int id) throws SQLException {
        String sql = "SELECT c.id, c.modelo, c.sistema_operativo, c.gama, c.stock, "
                   + "       m.id AS m_id, m.nombre AS m_nombre, "
                   + "       p.id AS p_id, p.nombre AS p_nombre, p.valor AS p_valor "
                   + "FROM celular c "
                   + "JOIN marca  m ON c.id_marca_fk  = m.id "
                   + "JOIN precio p ON c.id_precio_fk = p.id "
                   + "WHERE c.id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    /** Lista todos los celulares con sus datos de marca y precio. */
    public List<Celular> listarTodos() throws SQLException {
        List<Celular> lista = new ArrayList<>();
        String sql = "SELECT c.id, c.modelo, c.sistema_operativo, c.gama, c.stock, "
                   + "       m.id AS m_id, m.nombre AS m_nombre, "
                   + "       p.id AS p_id, p.nombre AS p_nombre, p.valor AS p_valor "
                   + "FROM celular c "
                   + "JOIN marca  m ON c.id_marca_fk  = m.id "
                   + "JOIN precio p ON c.id_precio_fk = p.id "
                   + "ORDER BY c.id";
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    /** Actualiza todos los campos de un celular existente. */
    public void actualizar(Celular celular) throws SQLException {
        String sql = "UPDATE celular SET modelo = ?, id_marca_fk = ?, sistema_operativo = ?, "
                   + "gama = ?, id_precio_fk = ?, stock = ? WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, celular.getModelo());
            ps.setInt   (2, celular.getMarca().getId());
            ps.setString(3, celular.getSistemaOperativo().name());
            ps.setString(4, celular.getGama().name());
            ps.setInt   (5, celular.getPrecio().getId());
            ps.setInt   (6, celular.getStock());
            ps.setInt   (7, celular.getId());
            ps.executeUpdate();
        }
    }

    /** Elimina un celular por su id. */
    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM celular WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Reduce el stock de un celular en la cantidad indicada.
     * Usa "stock >= cantidad" en el WHERE para que MySQL no permita stock negativo.
     * Si no se actualiza ninguna fila, lanza SQLException (stock insuficiente).
     */
    public void actualizarStock(int idCelular, int cantidad) throws SQLException {
        String sql = "UPDATE celular SET stock = stock - ? WHERE id = ? AND stock >= ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setInt(2, idCelular);
            ps.setInt(3, cantidad);
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new SQLException("Stock insuficiente para el celular con id " + idCelular);
            }
        }
    }

    // Convierte una fila del ResultSet en un objeto Celular completo
    private Celular mapear(ResultSet rs) throws SQLException {
        Marca  marca  = new Marca (rs.getInt("m_id"), rs.getString("m_nombre"));
        Precio precio = new Precio(rs.getInt("p_id"), rs.getString("p_nombre"), rs.getDouble("p_valor"));
        return new Celular(
            rs.getInt   ("id"),
            rs.getString("modelo"),
            marca,
            SistemaOperativo.valueOf(rs.getString("sistema_operativo")),
            CategoriaGama   .valueOf(rs.getString("gama")),
            precio,
            rs.getInt   ("stock")
        );
    }
}