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

/**
 * GestorVentas - logica de negocio para el registro de ventas.
 *
 * Caracteristicas importantes:
 *
 * 1. stockComprometido: mapa que registra cuanto stock ya se "reservo"
 *    para el celular actual durante la sesion de venta en curso.
 *    Clave = id del celular, Valor = cantidad acumulada en esta venta.
 *    Esto evita que se pidan mas unidades de las disponibles en la misma venta.
 *
 * 2. confirmarVenta() es synchronized: si en el futuro el sistema
 *    se usa con multiples hilos, solo uno puede confirmar una venta a la vez.
 *
 * 3. EstrategiaDescuento: permite cambiar la forma de calcular precios
 *    sin modificar este gestor (patron Strategy).
 */
public class GestorVentas {

    private static final double TASA_IVA = 0.19;

    // Contador de ventas del dia - thread-safe con AtomicInteger
    private static final AtomicInteger contadorVentasDelDia = new AtomicInteger(0);

    private final VentaDAO    ventaDAO;
    private final ClienteDAO  clienteDAO;
    private final CelularDAO  celularDAO;

    // Estrategia de descuento (por defecto: sin descuento)
    private EstrategiaDescuento estrategiaDescuento;

    // Registro temporal de stock comprometido en la sesion actual
    // Clave: id del celular, Valor: cantidad ya acumulada en esta venta
    private Map<Integer, Integer> stockComprometido;

    // Lista de items de la venta en curso
    private List<ItemVenta> itemsEnCurso;

    public GestorVentas(VentaDAO ventaDAO, ClienteDAO clienteDAO, CelularDAO celularDAO) {
        this.ventaDAO           = ventaDAO;
        this.clienteDAO         = clienteDAO;
        this.celularDAO         = celularDAO;
        this.estrategiaDescuento = new SinDescuento();
        this.stockComprometido  = new HashMap<>();
        this.itemsEnCurso       = new ArrayList<>();
    }

    /** Cambia la estrategia de descuento (patron Strategy). */
    public void setEstrategiaDescuento(EstrategiaDescuento estrategia) {
        this.estrategiaDescuento = estrategia;
    }

    /**
     * Inicia una nueva sesion de venta.
     * Limpia el stock comprometido y los items de sesiones anteriores.
     * Debe llamarse antes de empezar a agregar items.
     */
    public void iniciarSesionVenta() {
        stockComprometido = new HashMap<>();
        itemsEnCurso      = new ArrayList<>();
    }

    /**
     * Intenta agregar un celular a la venta en curso.
     *
     * Verifica que:
     *   1. La cantidad sea valida (1-100)
     *   2. El celular exista
     *   3. El stock disponible menos lo ya comprometido en esta sesion sea suficiente
     *
     * @return true si se agrego correctamente, false si hubo un problema
     */
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

            // Calcular stock real disponible descontando lo ya comprometido en esta venta
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

            // Acumular en el mapa de stock comprometido
            stockComprometido.put(idCelular, yaComprometido + cantidad);

            // Agregar a la lista de items en curso
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

    /** Retorna la lista de items en la venta actual (para mostrar en consola). */
    public List<ItemVenta> getItemsEnCurso() {
        return itemsEnCurso;
    }

    /**
     * Calcula el total con IVA de una lista de items.
     * Aplica la estrategia de descuento antes de agregar el IVA.
     *
     * Formula: totalConIva = round((sum(subtotales) con descuento) * 1.19, 2)
     */
    public double calcularTotalConIva(List<ItemVenta> items) {
        double subtotalSinDescuento = items.stream()
                .mapToDouble(ItemVenta::getSubtotal)
                .sum();
        double subtotalConDescuento = estrategiaDescuento.aplicar(subtotalSinDescuento);
        double totalConIva = subtotalConDescuento * (1 + TASA_IVA);
        return Math.round(totalConIva * 100.0) / 100.0;
    }

    /**
     * Confirma y guarda la venta en la base de datos.
     *
     * Metodo synchronized: solo un hilo puede ejecutarlo a la vez.
     * Hace doble verificacion de stock antes del commit (en VentaDAO).
     *
     * @param identificacionCliente identificacion del cliente que compra
     * @param empleado              empleado que procesa la venta (cajero)
     * @return la Venta guardada, o null si hubo un error
     */
    public synchronized Venta confirmarVenta(String identificacionCliente, Empleado empleado) {
        if (itemsEnCurso.isEmpty()) {
            ConsolaUtils.imprimirError("No hay productos en la venta. Agrega al menos uno.");
            return null;
        }

        try {
            // Verificar que el cliente existe
            Cliente cliente = clienteDAO.buscarPorIdentificacion(identificacionCliente);
            if (cliente == null) {
                ConsolaUtils.imprimirError("Cliente no encontrado con identificacion: " + identificacionCliente);
                return null;
            }

            // Calcular total con IVA y descuento
            double totalConIva = calcularTotalConIva(itemsEnCurso);

            // Construir el objeto Venta
            Venta venta = new Venta(cliente, empleado, totalConIva);
            for (ItemVenta item : itemsEnCurso) {
                venta.agregarItem(item);
            }

            // Guardar en BD con transaccion (VentaDAO hace doble check de stock)
            ventaDAO.insertarConTransaccion(venta);

            // Incrementar contador del dia
            contadorVentasDelDia.incrementAndGet();

            // Limpiar sesion
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

    /**
     * Retorna todas las ventas registradas en la BD.
     */
    public List<Venta> listarTodas() {
        try {
            return ventaDAO.listarTodas();
        } catch (SQLException e) {
            ConsolaUtils.imprimirError("Error al acceder a la base de datos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /** Retorna el numero de ventas confirmadas en la sesion actual del programa. */
    public int getContadorVentasDelDia() {
        return contadorVentasDelDia.get();
    }
}