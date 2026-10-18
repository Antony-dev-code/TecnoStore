package com.tecnostore.persistencia;

import com.tecnostore.modelo.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * VentaDAO - acceso a las tablas venta y detalle_venta.
 *
 * El metodo insertarConTransaccion() es el mas importante del sistema:
 * guarda la venta, sus items y actualiza el stock todo en UNA sola
 * transaccion. Si algo falla, hace rollback y no queda nada a medias.
 */
public class VentaDAO {

    private final Connection conexion;
    private final CelularDAO celularDAO;

    public VentaDAO(Connection conexion) {
        this.conexion   = conexion;
        this.celularDAO = new CelularDAO(conexion);
    }

    /**
     * Guarda una venta completa en una transaccion atomica.
     *
     * Pasos dentro de la transaccion:
     * 1. Doble verificacion de stock (SELECT FOR UPDATE)
     * 2. INSERT en venta
     * 3. INSERT en detalle_venta por cada item
     * 4. UPDATE stock por cada celular vendido
     * 5. COMMIT si todo salio bien, ROLLBACK si algo fallo
     */
    public void insertarConTransaccion(Venta venta) throws SQLException {
        conexion.setAutoCommit(false);
        try {
            // --- Paso 1: Doble verificacion de stock antes de confirmar ---
            for (ItemVenta item : venta.getItems()) {
                String sqlStock = "SELECT stock FROM celular WHERE id = ? FOR UPDATE";
                try (PreparedStatement ps = conexion.prepareStatement(sqlStock)) {
                    ps.setInt(1, item.getCelular().getId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException(
                            "Celular no encontrado: id " + item.getCelular().getId());
                        int stockActual = rs.getInt("stock");
                        if (stockActual < item.getCantidad()) throw new SQLException(
                            "Stock insuficiente al confirmar venta. Celular: "
                            + item.getCelular().getModelo()
                            + " | Disponible: " + stockActual
                            + " | Solicitado: " + item.getCantidad());
                    }
                }
            }

            // --- Paso 2: INSERT en venta ---
            String sqlVenta = "INSERT INTO venta (id_cliente_fk, fecha, total, id_empleado_fk) "
                            + "VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conexion.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt      (1, venta.getCliente().getId());
                ps.setTimestamp(2, Timestamp.valueOf(venta.getFecha()));
                ps.setDouble   (3, venta.getTotal());
                ps.setInt      (4, venta.getEmpleado().getId());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) venta.setId(rs.getInt(1));
                }
            }

            // --- Paso 3 y 4: INSERT detalle + UPDATE stock por cada item ---
            String sqlDetalle = "INSERT INTO detalle_venta (id_venta_fk, id_celular_fk, cantidad, subtotal) "
                              + "VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conexion.prepareStatement(sqlDetalle)) {
                for (ItemVenta item : venta.getItems()) {
                    ps.setInt   (1, venta.getId());
                    ps.setInt   (2, item.getCelular().getId());
                    ps.setInt   (3, item.getCantidad());
                    ps.setDouble(4, item.getSubtotal());
                    ps.executeUpdate();

                    // Actualizar stock (CelularDAO ya verifica que no quede negativo)
                    celularDAO.actualizarStock(item.getCelular().getId(), item.getCantidad());
                }
            }

            // --- Paso 5: Confirmar todo ---
            conexion.commit();

        } catch (SQLException e) {
            // Si algo fallo, deshacer todos los cambios
            conexion.rollback();
            throw e; // relanzar para que GestorVentas muestre el mensaje de error
        } finally {
            // Siempre restaurar el modo autocommit
            conexion.setAutoCommit(true);
        }
    }

    /** Lista todas las ventas con sus items, cliente y empleado. */
    public List<Venta> listarTodas() throws SQLException {
        List<Venta> ventas = new ArrayList<>();

        String sqlVentas = "SELECT v.id, v.fecha, v.total, "
                         + "       p_c.id AS c_id, p_c.nombre AS c_nombre, "
                         + "       p_c.identificacion AS c_ident, "
                         + "       p_c.telefono AS c_tel, p_c.correo AS c_correo, "
                         + "       cl.rut, "
                         + "       p_e.id AS e_id, p_e.nombre AS e_nombre, "
                         + "       p_e.identificacion AS e_ident, "
                         + "       p_e.telefono AS e_tel, p_e.correo AS e_correo, "
                         + "       em.tipo "
                         + "FROM venta v "
                         + "JOIN cliente  cl  ON v.id_cliente_fk  = cl.id "
                         + "JOIN persona  p_c ON cl.id            = p_c.id "
                         + "JOIN empleado em  ON v.id_empleado_fk = em.id "
                         + "JOIN persona  p_e ON em.id            = p_e.id "
                         + "ORDER BY v.fecha DESC";

        try (PreparedStatement ps = conexion.prepareStatement(sqlVentas);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Cliente  cliente  = new Cliente(
                    rs.getInt("c_id"), rs.getString("c_nombre"),
                    rs.getString("c_ident"), rs.getString("c_tel"),
                    rs.getString("c_correo"), rs.getString("rut"));
                Empleado empleado = new Empleado(
                    rs.getInt("e_id"), rs.getString("e_nombre"),
                    rs.getString("e_ident"), rs.getString("e_tel"),
                    rs.getString("e_correo"),
                    TipoEmpleado.valueOf(rs.getString("tipo")));

                Venta venta = new Venta(
                    rs.getInt("id"), cliente, empleado,
                    new ArrayList<>(),
                    rs.getTimestamp("fecha").toLocalDateTime(),
                    rs.getDouble("total"));

                // Cargar los items de esta venta
                cargarItems(venta);
                ventas.add(venta);
            }
        }
        return ventas;
    }

    /** Carga los items de detalle para una venta ya creada. */
    private void cargarItems(Venta venta) throws SQLException {
        String sql = "SELECT dv.id, dv.cantidad, dv.subtotal, "
                   + "       c.id AS cel_id, c.modelo, c.sistema_operativo, c.gama, c.stock, "
                   + "       m.id AS m_id, m.nombre AS m_nombre, "
                   + "       p.id AS p_id, p.nombre AS p_nombre, p.valor AS p_valor "
                   + "FROM detalle_venta dv "
                   + "JOIN celular c ON dv.id_celular_fk = c.id "
                   + "JOIN marca   m ON c.id_marca_fk    = m.id "
                   + "JOIN precio  p ON c.id_precio_fk   = p.id "
                   + "WHERE dv.id_venta_fk = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, venta.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Marca   marca   = new Marca (rs.getInt("m_id"), rs.getString("m_nombre"));
                    Precio  precio  = new Precio(rs.getInt("p_id"), rs.getString("p_nombre"), rs.getDouble("p_valor"));
                    Celular celular = new Celular(
                        rs.getInt("cel_id"), rs.getString("modelo"), marca,
                        SistemaOperativo.valueOf(rs.getString("sistema_operativo")),
                        CategoriaGama   .valueOf(rs.getString("gama")),
                        precio, rs.getInt("stock"));
                    venta.agregarItem(new ItemVenta(
                        rs.getInt("id"), celular,
                        rs.getInt("cantidad"), rs.getDouble("subtotal")));
                }
            }
        }
    }
}