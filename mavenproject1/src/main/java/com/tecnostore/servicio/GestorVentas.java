package com.tecnostore.servicio;

import com.tecnostore.modelo.*;
import com.tecnostore.patron.EstrategiaDescuento;
import com.tecnostore.patron.SinDescuento;
import com.tecnostore.persistencia.CelularDAO;
import com.tecnostore.persistencia.ClienteDAO;
import com.tecnostore.persistencia.VentaDAO;
import com.tecnostore.util.ConsolaUtils;
import com.tecnostore.util.Validador;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

// Maneja el armado y el cierre de una venta.
public class GestorVentas {

    private static final double TASA_IVA = 0.19;

    // Cuenta las ventas hechas desde que arranco el programa.
    private static final AtomicInteger contadorVentasDelDia = new AtomicInteger(0);

    private final VentaDAO    ventaDAO;
    private final ClienteDAO  clienteDAO;
    private final CelularDAO  celularDAO;

    private EstrategiaDescuento estrategiaDescuento;

    // Cuanto stock de cada celular ya se aparto en la venta que se esta armando.
    private Map<Integer, Integer> stockComprometido;

    // Los celulares que llevamos agregados a la venta actual.
    private List<ItemVenta> itemsEnCurso;

    public GestorVentas(VentaDAO ventaDAO, ClienteDAO clienteDAO, CelularDAO celularDAO) {
        this.ventaDAO           = ventaDAO;
        this.clienteDAO         = clienteDAO;
        this.celularDAO         = celularDAO;
        this.estrategiaDescuento = new SinDescuento();
        this.stockComprometido  = new HashMap<>();
        this.itemsEnCurso       = new ArrayList<>();
    }

    // Cambia como se calcula el descuento.
    public void setEstrategiaDescuento(EstrategiaDescuento estrategia) {
        this.estrategiaDescuento = estrategia;
    }

    // Empieza una venta nueva y limpia lo de la anterior.
    public void iniciarSesionVenta() {
        stockComprometido = new HashMap<>();
        itemsEnCurso      = new ArrayList<>();
    }

    // Agrega un celular a la venta si la cantidad es valida y hay stock.
    public boolean agregarItem(int idCelular, int cantidad) {
        if (!Validador.esCantidadValida(cantidad)) {
            ConsolaUtils.imprimirError("La cantidad debe ser un numero entre 1 y 100.");
            return false;
        }

        try {
            Celular celular = celularDAO.buscarPorId(idCelular);
            if (celular == null) {
                ConsolaUtils.imprimirError("Celular no encontrado con id: " + idCelular);
                return false;
            }

            // Al stock le restamos lo que ya pusimos en esta misma venta.
            int yaComprometido    = stockComprometido.getOrDefault(idCelular, 0);
            int stockDisponible   = celular.getStock() - yaComprometido;

            if (cantidad > stockDisponible) {
                ConsolaUtils.imprimirError(
                    "No hay suficiente stock para este celular en esta venta.\n"
                    + "  Disponible: " + celular.getStock()
                    + " | Ya solicitado en esta venta: " + yaComprometido
                    + " | Stock real disponible: " + stockDisponible
                );
                return false;
            }

            stockComprometido.put(idCelular, yaComprometido + cantidad);
            itemsEnCurso.add(new ItemVenta(celular, cantidad));

            ConsolaUtils.imprimirExito(
                "Agregado: " + celular.getMarca().getNombre() + " " + celular.getModelo()
                + " x" + cantidad
                + " - Subtotal: " + ConsolaUtils.formatearPrecio(
                    celular.getPrecio().getValor() * cantidad)
            );
            return true;

        } catch (SQLException e) {
            ConsolaUtils.imprimirError("Error al acceder a la base de datos: " + e.getMessage());
            return false;
        }
    }

    // Devuelve los celulares de la venta actual.
    public List<ItemVenta> getItemsEnCurso() {
        return itemsEnCurso;
    }

    // Suma los subtotales, aplica el descuento y le agrega el IVA.
    public double calcularTotalConIva(List<ItemVenta> items) {
        double subtotalSinDescuento = items.stream()
                .mapToDouble(ItemVenta::getSubtotal)
                .sum();
        double subtotalConDescuento = estrategiaDescuento.aplicar(subtotalSinDescuento);
        double totalConIva = subtotalConDescuento * (1 + TASA_IVA);
        return Math.round(totalConIva * 100.0) / 100.0;
    }

    // Cierra la venta y la guarda. Devuelve la venta o null si algo salio mal.
    public synchronized Venta confirmarVenta(String identificacionCliente, Empleado empleado) {
        if (itemsEnCurso.isEmpty()) {
            ConsolaUtils.imprimirError("No hay productos en la venta. Agrega al menos uno.");
            return null;
        }

        try {
            Cliente cliente = clienteDAO.buscarPorIdentificacion(identificacionCliente);
            if (cliente == null) {
                ConsolaUtils.imprimirError("Cliente no encontrado con identificacion: " + identificacionCliente);
                return null;
            }

            double totalConIva = calcularTotalConIva(itemsEnCurso);

            Venta venta = new Venta(cliente, empleado, totalConIva);
            for (ItemVenta item : itemsEnCurso) {
                venta.agregarItem(item);
            }

            ventaDAO.insertarConTransaccion(venta);
            contadorVentasDelDia.incrementAndGet();
            iniciarSesionVenta();

            return venta;

        } catch (SQLException e) {
            ConsolaUtils.imprimirError(
                "No se pudo completar la venta: stock insuficiente al confirmar o error de BD.\n"
                + "  Detalle: " + e.getMessage()
            );
            return null;
        }
    }

    // Devuelve todas las ventas guardadas.
    public List<Venta> listarTodas() {
        try {
            return ventaDAO.listarTodas();
        } catch (SQLException e) {
            ConsolaUtils.imprimirError("Error al acceder a la base de datos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Cuantas ventas se han cerrado desde que arranco el programa.
    public int getContadorVentasDelDia() {
        return contadorVentasDelDia.get();
    }
}