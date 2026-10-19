package com.tecnostore;

import com.tecnostore.modelo.*;
import com.tecnostore.patron.DescuentoPorcentaje;
import com.tecnostore.patron.SinDescuento;
import com.tecnostore.persistencia.*;
import com.tecnostore.servicio.*;
import com.tecnostore.util.*;
import java.sql.Connection;
import java.util.List;
import java.util.Scanner;

/**
 * Main - punto de entrada del sistema TecnoStore.
 *
 * Aqui se inicializan todos los componentes y se muestra el menu principal.
 * Cada opcion del menu llama a los gestores correspondientes.
 *
 * Estructura:
 *   main()              -> conecta BD, crea gestores, muestra menu principal
 *   menuCelulares()     -> subMenu de gestion de celulares
 *   menuClientes()      -> subMenu de gestion de clientes
 *   menuVentas()        -> subMenu de ventas
 *   menuReportes()      -> subMenu de reportes y archivos
 */
public class Main {

    // Scanner compartido para leer entradas del usuario
    private static final Scanner sc = new Scanner(System.in);

    // Gestores (se inicializan en main())
    private static GestorCelulares gestorCelulares;
    private static GestorClientes  gestorClientes;
    private static GestorVentas    gestorVentas;

    // DAOs adicionales que necesita el menu de ventas
    private static MarcaDAO    marcaDAO;
    private static PrecioDAO   precioDAO;
    private static EmpleadoDAO empleadoDAO;

    public static void main(String[] args) {

        ConsolaUtils.imprimirTitulo("TECNOSTORE - SISTEMA DE GESTION");
        System.out.println("  Conectando a la base de datos...");

        // Inicializar conexion Singleton
        ConexionDB db;
        try {
            db = ConexionDB.getInstancia();
            if (!db.estaConectado()) {
                ConsolaUtils.imprimirError("No se pudo establecer la conexion a la base de datos.");
                return;
            }
            ConsolaUtils.imprimirExito("Conexion establecida correctamente.");
        } catch (RuntimeException e) {
            ConsolaUtils.imprimirError(e.getMessage());
            return;
        }

        // Crear DAOs con la conexion compartida
        Connection conn = db.getConexion();
        marcaDAO    = new MarcaDAO(conn);
        precioDAO   = new PrecioDAO(conn);
        empleadoDAO = new EmpleadoDAO(conn);

        CelularDAO celularDAO = new CelularDAO(conn);
        ClienteDAO clienteDAO = new ClienteDAO(conn);
        VentaDAO   ventaDAO   = new VentaDAO(conn);

        // Crear gestores inyectando sus DAOs
        gestorCelulares = new GestorCelulares(celularDAO);
        gestorClientes  = new GestorClientes(clienteDAO);
        gestorVentas    = new GestorVentas(ventaDAO, clienteDAO, celularDAO);

        // Bucle principal del menu
        int opcion = 0;
        do {
            mostrarMenuPrincipal();
            opcion = leerEntero("Seleccione una opcion: ");
            switch (opcion) {
                case 1 -> menuCelulares();
                case 2 -> menuClientes();
                case 3 -> menuVentas();
                case 4 -> menuReportes();
                case 5 -> {
                    db.cerrarConexion();
                    ConsolaUtils.imprimirTitulo("Gracias por usar TecnoStore. Hasta pronto!");
                }
                default -> ConsolaUtils.imprimirAviso("Opcion no valida. Intente de nuevo.");
            }
        } while (opcion != 5);

        sc.close();
    }

    // =========================================================================
    // MENU PRINCIPAL
    // =========================================================================
    private static void mostrarMenuPrincipal() {
        ConsolaUtils.imprimirTitulo("MENU PRINCIPAL - TECNOSTORE");
        System.out.println("  1. Gestion de Celulares");
        System.out.println("  2. Gestion de Clientes");
        System.out.println("  3. Registrar Venta");
        System.out.println("  4. Reportes y Archivos");
        System.out.println("  5. Salir");
        ConsolaUtils.imprimirLinea();
    }

    // =========================================================================
    // MENU CELULARES
    // =========================================================================
    private static void menuCelulares() {
        int opcion = 0;
        do {
            ConsolaUtils.imprimirTitulo("GESTION DE CELULARES");
            System.out.println("  1. Registrar celular nuevo");
            System.out.println("  2. Listar todos los celulares");
            System.out.println("  3. Buscar celular por ID");
            System.out.println("  4. Actualizar celular");
            System.out.println("  5. Eliminar celular");
            System.out.println("  6. Volver al menu principal");
            ConsolaUtils.imprimirLinea();

            opcion = leerEntero("Seleccione una opcion: ");
            switch (opcion) {
                case 1 -> registrarCelular();
                case 2 -> listarCelulares();
                case 3 -> buscarCelularPorId();
                case 4 -> actualizarCelular();
                case 5 -> eliminarCelular();
                case 6 -> {}
                default -> ConsolaUtils.imprimirAviso("Opcion no valida.");
            }
        } while (opcion != 6);
    }

    private static void registrarCelular() {
        ConsolaUtils.imprimirTitulo("REGISTRAR CELULAR NUEVO");

        String modelo = leerTexto("Modelo (ej: Galaxy S24 Ultra): ");

        // Mostrar marcas disponibles
        List<Marca> marcas = listarMarcasDisponibles();
        if (marcas.isEmpty()) return;
        int idMarca = leerEntero("ID de la marca: ");
        Marca marca = marcas.stream()
                .filter(m -> m.getId() == idMarca)
                .findFirst().orElse(null);
        if (marca == null) {
            ConsolaUtils.imprimirError("Marca no encontrada.");
            return;
        }

        // Sistema operativo
        System.out.println("  Sistema operativo:");
        System.out.println("    1. ANDROID");
        System.out.println("    2. IOS");
        int opSO = leerEntero("Seleccione: ");
        SistemaOperativo so = (opSO == 2) ? SistemaOperativo.IOS : SistemaOperativo.ANDROID;

        // Gama
        System.out.println("  Gama:");
        System.out.println("    1. ALTA");
        System.out.println("    2. MEDIA");
        System.out.println("    3. BAJA");
        int opGama = leerEntero("Seleccione: ");
        CategoriaGama gama = switch (opGama) {
            case 2  -> CategoriaGama.MEDIA;
            case 3  -> CategoriaGama.BAJA;
            default -> CategoriaGama.ALTA;
        };

        double valorPrecio = leerDecimal("Precio (ej: 1299990.00): ");
        Precio precio = new Precio("Precio " + marca.getNombre() + " " + modelo, valorPrecio);
        // Insertar precio primero
        try {
            precioDAO.insertar(precio);
        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al guardar el precio: " + e.getMessage());
            return;
        }

        int stock = leerEntero("Stock inicial: ");
        gestorCelulares.registrarCelular(modelo, marca, so, gama, precio, stock);
    }

    private static void listarCelulares() {
        List<Celular> lista = gestorCelulares.listarTodos();
        ConsolaUtils.imprimirTitulo("INVENTARIO DE CELULARES");
        if (lista.isEmpty()) {
            ConsolaUtils.imprimirAviso("No hay celulares registrados.");
            return;
        }
        int[] anchosCel = {4, 12, 20, 9, 7, 16, 6};
        ConsolaUtils.imprimirEncabezado(anchosCel, "ID", "MARCA", "MODELO", "SO", "GAMA", "PRECIO", "STOCK");
        lista.forEach(c -> ConsolaUtils.imprimirFila(anchosCel,
            String.valueOf(c.getId()),
            c.getMarca().getNombre(),
            c.getModelo(),
            c.getSistemaOperativo().name(),
            c.getGama().name(),
            ConsolaUtils.formatearPrecio(c.getPrecio().getValor()),
            String.valueOf(c.getStock())
        ));
        ConsolaUtils.imprimirLinea();
    }

    private static void buscarCelularPorId() {
        int id = leerEntero("ID del celular: ");
        Celular c = gestorCelulares.buscarPorId(id);
        if (c == null) {
            ConsolaUtils.imprimirError("Celular no encontrado.");
            return;
        }
        ConsolaUtils.imprimirTitulo("DETALLE DEL CELULAR");
        System.out.println("  ID     : " + c.getId());
        System.out.println("  Marca  : " + c.getMarca().getNombre());
        System.out.println("  Modelo : " + c.getModelo());
        System.out.println("  S.O.   : " + c.getSistemaOperativo().name());
        System.out.println("  Gama   : " + c.getGama().name());
        System.out.println("  Precio : " + ConsolaUtils.formatearPrecio(c.getPrecio().getValor()));
        System.out.println("  Stock  : " + c.getStock());
        ConsolaUtils.imprimirLinea();
    }

    private static void actualizarCelular() {
        listarCelulares();
        int id = leerEntero("ID del celular a actualizar: ");
        Celular existente = gestorCelulares.buscarPorId(id);
        if (existente == null) {
            ConsolaUtils.imprimirError("Celular no encontrado.");
            return;
        }
        ConsolaUtils.imprimirTitulo("ACTUALIZAR CELULAR #" + id);
        System.out.println("  (Presiona Enter para conservar el valor actual)");
        ConsolaUtils.imprimirEspacio();

        String modelo = leerTextoOpcional("Modelo [" + existente.getModelo() + "]: ", existente.getModelo());
        int stock = leerEnteroOpcional("Stock [" + existente.getStock() + "]: ", existente.getStock());
        double valor = leerDecimalOpcional(
            "Precio [" + existente.getPrecio().getValor() + "]: ",
            existente.getPrecio().getValor());

        Precio precio = existente.getPrecio();
        precio.setValor(valor);

        gestorCelulares.actualizarCelular(
            id, modelo, existente.getMarca(),
            existente.getSistemaOperativo(), existente.getGama(),
            precio, stock);
    }

    private static void eliminarCelular() {
        listarCelulares();
        int id = leerEntero("ID del celular a eliminar: ");
        System.out.print("  Confirma eliminar el celular #" + id + "? (s/n): ");
        String conf = sc.nextLine().trim().toLowerCase();
        if (conf.equals("s")) {
            gestorCelulares.eliminarCelular(id);
        } else {
            ConsolaUtils.imprimirAviso("Operacion cancelada.");
        }
    }

    // =========================================================================
    // MENU CLIENTES
    // =========================================================================
    private static void menuClientes() {
        int opcion = 0;
        do {
            ConsolaUtils.imprimirTitulo("GESTION DE CLIENTES");
            System.out.println("  1. Registrar cliente nuevo");
            System.out.println("  2. Listar todos los clientes");
            System.out.println("  3. Buscar cliente por identificacion");
            System.out.println("  4. Volver al menu principal");
            ConsolaUtils.imprimirLinea();

            opcion = leerEntero("Seleccione una opcion: ");
            switch (opcion) {
                case 1 -> registrarCliente();
                case 2 -> listarClientes();
                case 3 -> buscarCliente();
                case 4 -> {}
                default -> ConsolaUtils.imprimirAviso("Opcion no valida.");
            }
        } while (opcion != 4);
    }

    private static void registrarCliente() {
        ConsolaUtils.imprimirTitulo("REGISTRAR CLIENTE NUEVO");
        String nombre = leerTexto("Nombre completo: ");
        String ident  = leerTexto("Numero de identificacion: ");
        String correo = "";
        int intentos  = 0;
        while (intentos < 3) {
            correo = leerTexto("Correo electronico: ");
            if (Validador.esCorreoValido(correo)) break;
            intentos++;
            ConsolaUtils.imprimirError("Correo invalido. Intento " + intentos + "/3.");
        }
        if (!Validador.esCorreoValido(correo)) {
            ConsolaUtils.imprimirError("Demasiados intentos fallidos. Operacion cancelada.");
            return;
        }
        String tel = leerTexto("Telefono (solo digitos): ");
        gestorClientes.registrarCliente(nombre, ident, correo, tel);
    }

    private static void listarClientes() {
        List<Cliente> lista = gestorClientes.listarTodos();
        ConsolaUtils.imprimirTitulo("LISTADO DE CLIENTES");
        if (lista.isEmpty()) {
            ConsolaUtils.imprimirAviso("No hay clientes registrados.");
            return;
        }
        int[] anchosCli = {4, 22, 16, 13, 28};
        ConsolaUtils.imprimirEncabezado(anchosCli, "ID", "NOMBRE", "IDENTIFICACION", "TELEFONO", "CORREO");
        lista.forEach(c -> ConsolaUtils.imprimirFila(anchosCli,
            String.valueOf(c.getId()), c.getNombre(),
            c.getIdentificacion(), c.getTelefono(), c.getCorreo()
        ));
        ConsolaUtils.imprimirLinea();
    }

    private static void buscarCliente() {
        String ident = leerTexto("Numero de identificacion: ");
        Cliente c = gestorClientes.buscarPorIdentificacion(ident);
        if (c == null) {
            ConsolaUtils.imprimirError("Cliente no encontrado.");
            return;
        }
        ConsolaUtils.imprimirTitulo("DATOS DEL CLIENTE");
        System.out.println("  ID             : " + c.getId());
        System.out.println("  Nombre         : " + c.getNombre());
        System.out.println("  Identificacion : " + c.getIdentificacion());
        System.out.println("  Telefono       : " + c.getTelefono());
        System.out.println("  Correo         : " + c.getCorreo());
        System.out.println("  RUT            : " + (c.getRut() != null ? c.getRut() : "N/A"));
        ConsolaUtils.imprimirLinea();
    }

    // =========================================================================
    // MENU VENTAS
    // =========================================================================
    private static void menuVentas() {
        ConsolaUtils.imprimirTitulo("REGISTRAR VENTA");

        // Seleccionar empleado cajero
        Empleado cajero = seleccionarEmpleado();
        if (cajero == null) return;

        // Listar los clientes disponibles (practico para pruebas)
        listarClientes();

        // Identificar al cliente
        String identCliente = "";
        Cliente cliente = null;
        int intentos = 0;
        while (cliente == null && intentos < 3) {
            identCliente = leerTexto("Identificacion del cliente (ver columna IDENTIFICACION): ");
            cliente = gestorClientes.buscarPorIdentificacion(identCliente);
            if (cliente == null) {
                intentos++;
                ConsolaUtils.imprimirError("Cliente no encontrado. Intento " + intentos + "/3.");
            }
        }
        if (cliente == null) {
            ConsolaUtils.imprimirError("Cliente no encontrado. Regresando al menu principal.");
            return;
        }
        System.out.println("  Cliente: " + cliente.getNombre());

        // Iniciar sesion de venta
        gestorVentas.iniciarSesionVenta();
        listarCelulares();

        // Agregar items
        boolean agregando = true;
        while (agregando) {
            int idCelular = leerEntero("ID del celular a agregar (0 para terminar): ");
            if (idCelular == 0) {
                agregando = false;
            } else {
                int cantidad = leerEntero("Cantidad: ");
                gestorVentas.agregarItem(idCelular, cantidad);
            }

            // Mostrar resumen de items en curso
            List<ItemVenta> items = gestorVentas.getItemsEnCurso();
            if (!items.isEmpty()) {
                ConsolaUtils.imprimirLinea();
                System.out.println("  Items en la venta actual:");
                items.forEach(item -> System.out.printf(
                    "    - %s x%d  ->  %s%n",
                    item.getCelular().getMarca().getNombre() + " " + item.getCelular().getModelo(),
                    item.getCantidad(),
                    ConsolaUtils.formatearPrecio(item.getSubtotal())
                ));
                double totalEstimado = gestorVentas.calcularTotalConIva(items);
                System.out.println("  Total estimado con IVA: " + ConsolaUtils.formatearPrecio(totalEstimado));
                ConsolaUtils.imprimirLinea();
            }
        }

        if (gestorVentas.getItemsEnCurso().isEmpty()) {
            ConsolaUtils.imprimirAviso("Venta cancelada: no se agregaron productos.");
            return;
        }

        // Confirmar o cancelar
        System.out.print("  Confirmar venta? (s/n): ");
        String conf = sc.nextLine().trim().toLowerCase();
        if (!conf.equals("s")) {
            ConsolaUtils.imprimirAviso("Venta cancelada.");
            return;
        }

        Venta venta = gestorVentas.confirmarVenta(identCliente, cajero);
        if (venta != null) {
            ConsolaUtils.imprimirComprobante(venta);
        }
    }

    // =========================================================================
    // MENU REPORTES
    // =========================================================================
    private static void menuReportes() {
        int opcion = 0;
        do {
            ConsolaUtils.imprimirTitulo("REPORTES Y ARCHIVOS");
            System.out.println("  1. Celulares con stock bajo (menos de 5 unidades)");
            System.out.println("  2. Top 3 celulares mas vendidos");
            System.out.println("  3. Ventas totales por mes");
            System.out.println("  4. Generar archivo reporte_ventas.dat");
            System.out.println("  5. Ver reporte desde archivo .dat");
            System.out.println("  6. Volver al menu principal");
            ConsolaUtils.imprimirLinea();

            opcion = leerEntero("Seleccione una opcion: ");
            switch (opcion) {
                case 1 -> ReporteUtils.mostrarStockBajo(gestorCelulares.listarTodos());
                case 2 -> ReporteUtils.mostrarTopMasVendidos(gestorVentas.listarTodas());
                case 3 -> ReporteUtils.mostrarVentasPorMes(gestorVentas.listarTodas());
                case 4 -> ArchivoUtils.generarReporte(gestorVentas.listarTodas());
                case 5 -> ArchivoUtils.mostrarReporte();
                case 6 -> {}
                default -> ConsolaUtils.imprimirAviso("Opcion no valida.");
            }
        } while (opcion != 6);
    }

    // =========================================================================
    // METODOS AUXILIARES
    // =========================================================================

    /** Muestra las marcas disponibles y retorna la lista. */
    private static List<Marca> listarMarcasDisponibles() {
        List<Marca> marcas;
        try {
            marcas = marcaDAO.listarTodas();
        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al cargar marcas: " + e.getMessage());
            return List.of();
        }
        System.out.println("  Marcas disponibles:");
        marcas.forEach(m -> System.out.println("    " + m.getId() + ". " + m.getNombre()));
        return marcas;
    }

    /** Muestra los empleados y permite seleccionar uno. */
    private static Empleado seleccionarEmpleado() {
        List<Empleado> empleados;
        try {
            empleados = empleadoDAO.listarTodos();
        } catch (Exception e) {
            ConsolaUtils.imprimirError("Error al cargar empleados: " + e.getMessage());
            return null;
        }
        if (empleados.isEmpty()) {
            ConsolaUtils.imprimirError("No hay empleados registrados.");
            return null;
        }
        System.out.println("  Empleados disponibles:");
        empleados.forEach(e -> System.out.println(
            "    " + e.getId() + ". " + e.getNombre() + " (" + e.getTipo().name() + ")"));
        int idEmp = leerEntero("ID del empleado que procesa la venta: ");
        return empleados.stream()
                .filter(e -> e.getId() == idEmp)
                .findFirst().orElse(null);
    }

    /** Lee un entero desde la consola. Si hay error retorna 0. */
    private static int leerEntero(String prompt) {
        System.out.print("  " + prompt);
        try {
            String linea = sc.nextLine().trim();
            return Integer.parseInt(linea);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** Lee un decimal desde la consola. Si hay error retorna 0.0. */
    private static double leerDecimal(String prompt) {
        System.out.print("  " + prompt);
        try {
            return Double.parseDouble(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /** Lee un texto no vacio desde la consola. */
    private static String leerTexto(String prompt) {
        System.out.print("  " + prompt);
        return sc.nextLine().trim();
    }

    /** Lee texto con valor por defecto si el usuario presiona Enter. */
    private static String leerTextoOpcional(String prompt, String valorDefault) {
        System.out.print("  " + prompt);
        String val = sc.nextLine().trim();
        return val.isEmpty() ? valorDefault : val;
    }

    /** Lee entero con valor por defecto si el usuario presiona Enter. */
    private static int leerEnteroOpcional(String prompt, int valorDefault) {
        System.out.print("  " + prompt);
        String val = sc.nextLine().trim();
        if (val.isEmpty()) return valorDefault;
        try { return Integer.parseInt(val); } catch (NumberFormatException e) { return valorDefault; }
    }

    /** Lee decimal con valor por defecto si el usuario presiona Enter. */
    private static double leerDecimalOpcional(String prompt, double valorDefault) {
        System.out.print("  " + prompt);
        String val = sc.nextLine().trim();
        if (val.isEmpty()) return valorDefault;
        try { return Double.parseDouble(val); } catch (NumberFormatException e) { return valorDefault; }
    }
}