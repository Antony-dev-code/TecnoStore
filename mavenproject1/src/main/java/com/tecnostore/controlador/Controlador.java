package com.tecnostore.controlador;

import com.tecnostore.modelo.*;
import com.tecnostore.persistencia.*;
import com.tecnostore.servicio.*;
import com.tecnostore.util.ArchivoUtils;
import com.tecnostore.util.ReporteUtils;
import com.tecnostore.vista.Vista;
import java.util.List;

// Conecta los menus con el resto del programa: lee la opcion del usuario
// y llama a quien corresponde.
public class Controlador {

    private final Vista           vista;
    private final GestorCelulares gestorCelulares;
    private final GestorClientes  gestorClientes;
    private final GestorVentas    gestorVentas;
    private final MarcaDAO        marcaDAO;
    private final PrecioDAO       precioDAO;
    private final EmpleadoDAO     empleadoDAO;
    private final ConexionDB      conexionDB;

    public Controlador(Vista vista,
                       GestorCelulares gestorCelulares,
                       GestorClientes gestorClientes,
                       GestorVentas gestorVentas,
                       MarcaDAO marcaDAO,
                       PrecioDAO precioDAO,
                       EmpleadoDAO empleadoDAO,
                       ConexionDB conexionDB) {
        this.vista           = vista;
        this.gestorCelulares = gestorCelulares;
        this.gestorClientes  = gestorClientes;
        this.gestorVentas    = gestorVentas;
        this.marcaDAO        = marcaDAO;
        this.precioDAO       = precioDAO;
        this.empleadoDAO     = empleadoDAO;
        this.conexionDB      = conexionDB;
    }

    // Menu principal: se repite hasta que el usuario elige salir
    public void iniciar() {
        int opcion;
        do {
            vista.mostrarMenuPrincipal();
            opcion = vista.leerEntero("Seleccione una opcion: ");
            switch (opcion) {
                case 1 -> menuCelulares();
                case 2 -> menuClientes();
                case 3 -> registrarVenta();
                case 4 -> menuReportes();
                case 5 -> {
                    conexionDB.cerrarConexion();
                    vista.mostrarDespedida();
                }
                default -> vista.mostrarAviso("Opcion no valida. Intente de nuevo.");
            }
        } while (opcion != 5);
        vista.cerrar();
    }

    // Celulares
    private void menuCelulares() {
        int opcion;
        do {
            vista.mostrarMenuCelulares();
            opcion = vista.leerEntero("Seleccione una opcion: ");
            switch (opcion) {
                case 1 -> registrarCelular();
                case 2 -> vista.mostrarTablaCelulares(gestorCelulares.listarTodos());
                case 3 -> buscarCelular();
                case 4 -> actualizarCelular();
                case 5 -> eliminarCelular();
                case 6 -> {}
                default -> vista.mostrarAviso("Opcion no valida.");
            }
        } while (opcion != 6);
    }

    private void registrarCelular() {
        vista.mostrarTitulo("REGISTRAR CELULAR NUEVO");
        String modelo = vista.leerTexto("Modelo (ej: Galaxy S24 Ultra): ");

        List<Marca> marcas = marcaDAO == null ? List.of() : obtenerMarcas();
        if (marcas.isEmpty()) return;
        vista.mostrarMarcas(marcas);

        int idMarca = vista.leerEntero("ID de la marca: ");
        Marca marca = marcas.stream().filter(m -> m.getId() == idMarca).findFirst().orElse(null);
        if (marca == null) {
            vista.mostrarError("Marca no encontrada.");
            return;
        }

        SistemaOperativo so = vista.pedirSistemaOperativo();
        CategoriaGama   gama = vista.pedirGama();
        double valorPrecio   = vista.leerDecimal("Precio (ej: 1299990.00): ");

        Precio precio = new Precio("Precio " + marca.getNombre() + " " + modelo, valorPrecio);
        try {
            precioDAO.insertar(precio);
        } catch (Exception e) {
            vista.mostrarError("Error al guardar el precio: " + e.getMessage());
            return;
        }

        int stock = vista.leerEntero("Stock inicial: ");
        gestorCelulares.registrarCelular(modelo, marca, so, gama, precio, stock);
    }

    private void buscarCelular() {
        int id = vista.leerEntero("ID del celular: ");
        Celular c = gestorCelulares.buscarPorId(id);
        if (c == null) {
            vista.mostrarError("Celular no encontrado.");
            return;
        }
        vista.mostrarDetalleCelular(c);
    }

    private void actualizarCelular() {
        vista.mostrarTablaCelulares(gestorCelulares.listarTodos());
        int id = vista.leerEntero("ID del celular a actualizar: ");
        Celular existente = gestorCelulares.buscarPorId(id);
        if (existente == null) {
            vista.mostrarError("Celular no encontrado.");
            return;
        }
        vista.mostrarTitulo("ACTUALIZAR CELULAR #" + id);
        vista.mostrarTexto("(Presiona Enter para conservar el valor actual)");

        String modelo = vista.leerTextoOpcional("Modelo [" + existente.getModelo() + "]: ", existente.getModelo());
        int stock     = vista.leerEnteroOpcional("Stock [" + existente.getStock() + "]: ", existente.getStock());
        double valor  = vista.leerDecimalOpcional(
            "Precio [" + existente.getPrecio().getValor() + "]: ", existente.getPrecio().getValor());

        Precio precio = existente.getPrecio();
        precio.setValor(valor);

        gestorCelulares.actualizarCelular(
            id, modelo, existente.getMarca(),
            existente.getSistemaOperativo(), existente.getGama(), precio, stock);
    }

    private void eliminarCelular() {
        vista.mostrarTablaCelulares(gestorCelulares.listarTodos());
        int id = vista.leerEntero("ID del celular a eliminar: ");
        if (vista.confirmar("Confirma eliminar el celular #" + id + "?")) {
            gestorCelulares.eliminarCelular(id);
        } else {
            vista.mostrarAviso("Operacion cancelada.");
        }
    }

    // Clientes
    private void menuClientes() {
        int opcion;
        do {
            vista.mostrarMenuClientes();
            opcion = vista.leerEntero("Seleccione una opcion: ");
            switch (opcion) {
                case 1 -> registrarCliente();
                case 2 -> vista.mostrarTablaClientes(gestorClientes.listarTodos());
                case 3 -> buscarCliente();
                case 4 -> {}
                default -> vista.mostrarAviso("Opcion no valida.");
            }
        } while (opcion != 4);
    }

    private void registrarCliente() {
        vista.mostrarTitulo("REGISTRAR CLIENTE NUEVO");
        String nombre = vista.leerTexto("Nombre completo: ");
        String ident  = vista.leerTexto("Numero de identificacion: ");
        String correo = vista.leerTexto("Correo electronico: ");
        String tel    = vista.leerTexto("Telefono (solo digitos): ");
        gestorClientes.registrarCliente(nombre, ident, correo, tel);
    }

    private void buscarCliente() {
        String ident = vista.leerTexto("Numero de identificacion: ");
        Cliente c = gestorClientes.buscarPorIdentificacion(ident);
        if (c == null) {
            vista.mostrarError("Cliente no encontrado.");
            return;
        }
        vista.mostrarDetalleCliente(c);
    }

    // Ventas
    private void registrarVenta() {
        vista.mostrarTitulo("REGISTRAR VENTA");

        Empleado cajero = seleccionarEmpleado();
        if (cajero == null) return;

        vista.mostrarTablaClientes(gestorClientes.listarTodos());

        String identCliente = "";
        Cliente cliente = null;
        int intentos = 0;
        while (cliente == null && intentos < 3) {
            identCliente = vista.leerTexto("Identificacion del cliente (ver columna IDENTIFICACION): ");
            cliente = gestorClientes.buscarPorIdentificacion(identCliente);
            if (cliente == null) {
                intentos++;
                vista.mostrarError("Cliente no encontrado. Intento " + intentos + "/3.");
            }
        }
        if (cliente == null) {
            vista.mostrarError("Cliente no encontrado. Regresando al menu principal.");
            return;
        }
        vista.mostrarTexto("Cliente: " + cliente.getNombre());

        gestorVentas.iniciarSesionVenta();
        vista.mostrarTablaCelulares(gestorCelulares.listarTodos());

        boolean agregando = true;
        while (agregando) {
            int idCelular = vista.leerEntero("ID del celular a agregar (0 para terminar): ");
            if (idCelular == 0) {
                agregando = false;
            } else {
                int cantidad = vista.leerEntero("Cantidad: ");
                gestorVentas.agregarItem(idCelular, cantidad);
            }
            List<ItemVenta> items = gestorVentas.getItemsEnCurso();
            if (!items.isEmpty()) {
                double totalEstimado = gestorVentas.calcularTotalConIva(items);
                vista.mostrarItemsEnCurso(items, totalEstimado);
            }
        }

        if (gestorVentas.getItemsEnCurso().isEmpty()) {
            vista.mostrarAviso("Venta cancelada: no se agregaron productos.");
            return;
        }

        if (!vista.confirmar("Confirmar venta?")) {
            vista.mostrarAviso("Venta cancelada.");
            return;
        }

        Venta venta = gestorVentas.confirmarVenta(identCliente, cajero);
        if (venta != null) {
            vista.mostrarComprobante(venta);
        }
    }

    // Reportes
    private void menuReportes() {
        int opcion;
        do {
            vista.mostrarMenuReportes();
            opcion = vista.leerEntero("Seleccione una opcion: ");
            switch (opcion) {
                case 1 -> ReporteUtils.mostrarStockBajo(gestorCelulares.listarTodos());
                case 2 -> ReporteUtils.mostrarTopMasVendidos(gestorVentas.listarTodas());
                case 3 -> ReporteUtils.mostrarVentasPorMes(gestorVentas.listarTodas());
                case 4 -> ArchivoUtils.generarReporte(gestorVentas.listarTodas());
                case 5 -> ArchivoUtils.mostrarReporte();
                case 6 -> {}
                default -> vista.mostrarAviso("Opcion no valida.");
            }
        } while (opcion != 6);
    }

    // Apoyos internos
    private List<Marca> obtenerMarcas() {
        try {
            return marcaDAO.listarTodas();
        } catch (Exception e) {
            vista.mostrarError("Error al cargar marcas: " + e.getMessage());
            return List.of();
        }
    }

    private Empleado seleccionarEmpleado() {
        List<Empleado> empleados;
        try {
            empleados = empleadoDAO.listarTodos();
        } catch (Exception e) {
            vista.mostrarError("Error al cargar empleados: " + e.getMessage());
            return null;
        }
        if (empleados.isEmpty()) {
            vista.mostrarError("No hay empleados registrados.");
            return null;
        }
        vista.mostrarEmpleados(empleados);
        int idEmp = vista.leerEntero("ID del empleado que procesa la venta: ");
        return empleados.stream().filter(e -> e.getId() == idEmp).findFirst().orElse(null);
    }
}