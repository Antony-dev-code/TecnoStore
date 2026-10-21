package com.tecnostore.servicio;

import com.tecnostore.persistencia.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Reporte de ventas filtrado por marca
public class ReporteVentas {

    // datos de una fila del reporte
    private String cliente;
    private String fecha;
    private String modelo;
    private double precio;
    private String empleado;
    private int cantidad;

    public String getCliente() { return cliente; }
    public String getFecha() { return fecha; }
    public String getModelo() { return modelo; }
    public double getPrecio() { return precio; }
    public String getEmpleado() { return empleado; }
    public int getCantidad() { return cantidad; }

    // muestra el reporte de la marca que le pasan
    public static void generar(String marca) {
        try {
            List<ReporteVentas> lista = buscarPorMarca(marca);

            // excepcion si no hay ventas de esa marca
            if (lista.isEmpty()) {
                throw new Exception("No se encuentran ventas de la marca o marca no registrada");
            }

            int totalUnidades = 0;
            double totalVentas = 0;

            // muestro los datos separados por /
            System.out.println("Cliente/Fecha/Modelo/Precio/Empleado");
            for (ReporteVentas r : lista) {
                System.out.println(r.getCliente() + "/" + r.getFecha() + "/" + r.getModelo() + "/" + r.getPrecio() + "/" + r.getEmpleado());
                totalUnidades = totalUnidades + r.getCantidad();
                totalVentas = totalVentas + (r.getPrecio() * r.getCantidad());
            }

            System.out.println("Total unidades: " + totalUnidades);
            System.out.println("Total ventas: " + totalVentas);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // consulta JDBC filtrada por marca
    private static List<ReporteVentas> buscarPorMarca(String marca) {
        List<ReporteVentas> lista = new ArrayList<>();

        String sql = "SELECT cl.nombre AS cliente, v.fecha, c.modelo, p.valor, "
                + "em.nombre AS empleado, dv.cantidad "
                + "FROM detalle_venta dv "
                + "JOIN venta v ON dv.id_venta_fk = v.id "
                + "JOIN celular c ON dv.id_celular_fk = c.id "
                + "JOIN marca m ON c.id_marca_fk = m.id "
                + "JOIN precio p ON c.id_precio_fk = p.id "
                + "JOIN persona cl ON v.id_cliente_fk = cl.id "
                + "JOIN persona em ON v.id_empleado_fk = em.id "
                + "WHERE m.nombre = ?";

        Connection con = ConexionDB.getInstancia().getConexion();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = con.prepareStatement(sql);
            ps.setString(1, marca);
            rs = ps.executeQuery();

            while (rs.next()) {
                ReporteVentas r = new ReporteVentas();
                r.cliente = rs.getString("cliente");
                r.fecha = rs.getString("fecha");
                r.modelo = rs.getString("modelo");
                r.precio = rs.getDouble("valor");
                r.empleado = rs.getString("empleado");
                r.cantidad = rs.getInt("cantidad");
                lista.add(r);
            }
        } catch (SQLException e) {
            System.out.println("Error al consultar: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                System.out.println("Error al cerrar");
            }
        }

        return lista;
    }
}
