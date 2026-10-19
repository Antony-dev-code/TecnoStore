package com.tecnostore.vista;

import com.tecnostore.modelo.*;
import com.tecnostore.util.ConsolaUtils;
import com.tecnostore.util.Validador;
import java.util.List;
import java.util.Scanner;

// Todo lo que se muestra en pantalla y se lee del teclado.
public class Vista {

    private final Scanner sc = new Scanner(System.in);

    // Menus
    public void mostrarBienvenida() {
        ConsolaUtils.imprimirTitulo("TECNOSTORE - SISTEMA DE GESTION");
        System.out.println("  Conectando a la base de datos...");
    }

    public void mostrarMenuPrincipal() {
        ConsolaUtils.imprimirTitulo("MENU PRINCIPAL - TECNOSTORE");
        System.out.println("  1. Gestion de Celulares");
        System.out.println("  2. Gestion de Clientes");
        System.out.println("  3. Registrar Venta");
        System.out.println("  4. Reportes y Archivos");
        System.out.println("  5. Salir");
        ConsolaUtils.imprimirLinea();
    }

    public void mostrarMenuCelulares() {
        ConsolaUtils.imprimirTitulo("GESTION DE CELULARES");
        System.out.println("  1. Registrar celular nuevo");
        System.out.println("  2. Listar todos los celulares");
        System.out.println("  3. Buscar celular por ID");
        System.out.println("  4. Actualizar celular");
        System.out.println("  5. Eliminar celular");
        System.out.println("  6. Volver al menu principal");
        ConsolaUtils.imprimirLinea();
    }

    public void mostrarMenuClientes() {
        ConsolaUtils.imprimirTitulo("GESTION DE CLIENTES");
        System.out.println("  1. Registrar cliente nuevo");
        System.out.println("  2. Listar todos los clientes");
        System.out.println("  3. Buscar cliente por identificacion");
        System.out.println("  4. Volver al menu principal");
        ConsolaUtils.imprimirLinea();
    }

    public void mostrarMenuReportes() {
        ConsolaUtils.imprimirTitulo("REPORTES Y ARCHIVOS");
        System.out.println("  1. Celulares con stock bajo (menos de 5 unidades)");
        System.out.println("  2. Top 3 celulares mas vendidos");
        System.out.println("  3. Ventas totales por mes");
        System.out.println("  4. Generar archivo reporte_ventas.dat");
        System.out.println("  5. Ver reporte desde archivo .dat");
        System.out.println("  6. Volver al menu principal");
        ConsolaUtils.imprimirLinea();
    }

    // Lectura de teclado
    public int leerEntero(String prompt) {
        System.out.print("  " + prompt);
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public double leerDecimal(String prompt) {
        System.out.print("  " + prompt);
        try {
            return Double.parseDouble(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public String leerTexto(String prompt) {
        System.out.print("  " + prompt);
        return sc.nextLine().trim();
    }

    public String leerTextoOpcional(String prompt, String valorDefault) {
        System.out.print("  " + prompt);
        String val = sc.nextLine().trim();
        return val.isEmpty() ? valorDefault : val;
    }

    public int leerEnteroOpcional(String prompt, int valorDefault) {
        System.out.print("  " + prompt);
        String val = sc.nextLine().trim();
        if (val.isEmpty()) return valorDefault;
        try { return Integer.parseInt(val); } catch (NumberFormatException e) { return valorDefault; }
    }

    public double leerDecimalOpcional(String prompt, double valorDefault) {
        System.out.print("  " + prompt);
        String val = sc.nextLine().trim();
        if (val.isEmpty()) return valorDefault;
        try { return Double.parseDouble(val); } catch (NumberFormatException e) { return valorDefault; }
    }

    public boolean confirmar(String prompt) {
        System.out.print("  " + prompt + " (s/n): ");
        return sc.nextLine().trim().equalsIgnoreCase("s");
    }

    // Mensajes
    public void mostrarExito(String mensaje)  { ConsolaUtils.imprimirExito(mensaje); }
    public void mostrarError(String mensaje)  { ConsolaUtils.imprimirError(mensaje); }
    public void mostrarAviso(String mensaje)  { ConsolaUtils.imprimirAviso(mensaje); }
    public void mostrarTitulo(String titulo)  { ConsolaUtils.imprimirTitulo(titulo); }
    public void mostrarLinea()                { ConsolaUtils.imprimirLinea(); }
    public void mostrarTexto(String texto)    { System.out.println("  " + texto); }
    public void mostrarDespedida() {
        ConsolaUtils.imprimirTitulo("Gracias por usar TecnoStore. Hasta pronto!");
    }

    // Formularios: piden datos al usuario
    public SistemaOperativo pedirSistemaOperativo() {
        System.out.println("  Sistema operativo:");
        System.out.println("    1. ANDROID");
        System.out.println("    2. IOS");
        int op = leerEntero("Seleccione: ");
        return (op == 2) ? SistemaOperativo.IOS : SistemaOperativo.ANDROID;
    }

    public CategoriaGama pedirGama() {
        System.out.println("  Gama:");
        System.out.println("    1. ALTA");
        System.out.println("    2. MEDIA");
        System.out.println("    3. BAJA");
        int op = leerEntero("Seleccione: ");
        return switch (op) {
            case 2  -> CategoriaGama.MEDIA;
            case 3  -> CategoriaGama.BAJA;
            default -> CategoriaGama.ALTA;
        };
    }

    // Listados y tablas
    public void mostrarMarcas(List<Marca> marcas) {
        System.out.println("  Marcas disponibles:");
        marcas.forEach(m -> System.out.println("    " + m.getId() + ". " + m.getNombre()));
    }

    public void mostrarEmpleados(List<Empleado> empleados) {
        System.out.println("  Empleados disponibles:");
        empleados.forEach(e -> System.out.println(
            "    " + e.getId() + ". " + e.getNombre() + " (" + e.getTipo().name() + ")"));
    }

    public void mostrarTablaCelulares(List<Celular> lista) {
        ConsolaUtils.imprimirTitulo("INVENTARIO DE CELULARES");
        if (lista.isEmpty()) {
            ConsolaUtils.imprimirAviso("No hay celulares registrados.");
            return;
        }
        int[] anchos = {4, 12, 20, 9, 7, 16, 6};
        ConsolaUtils.imprimirEncabezado(anchos, "ID", "MARCA", "MODELO", "SO", "GAMA", "PRECIO", "STOCK");
        lista.forEach(c -> ConsolaUtils.imprimirFila(anchos,
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

    public void mostrarDetalleCelular(Celular c) {
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

    public void mostrarTablaClientes(List<Cliente> lista) {
        ConsolaUtils.imprimirTitulo("LISTADO DE CLIENTES");
        if (lista.isEmpty()) {
            ConsolaUtils.imprimirAviso("No hay clientes registrados.");
            return;
        }
        int[] anchos = {4, 22, 16, 13, 28};
        ConsolaUtils.imprimirEncabezado(anchos, "ID", "NOMBRE", "IDENTIFICACION", "TELEFONO", "CORREO");
        lista.forEach(c -> ConsolaUtils.imprimirFila(anchos,
            String.valueOf(c.getId()), c.getNombre(),
            c.getIdentificacion(), c.getTelefono(), c.getCorreo()
        ));
        ConsolaUtils.imprimirLinea();
    }

    public void mostrarDetalleCliente(Cliente c) {
        ConsolaUtils.imprimirTitulo("DATOS DEL CLIENTE");
        System.out.println("  ID             : " + c.getId());
        System.out.println("  Nombre         : " + c.getNombre());
        System.out.println("  Identificacion : " + c.getIdentificacion());
        System.out.println("  Telefono       : " + c.getTelefono());
        System.out.println("  Correo         : " + c.getCorreo());
        System.out.println("  RUT            : " + (c.getRut() != null ? c.getRut() : "N/A"));
        ConsolaUtils.imprimirLinea();
    }

    public void mostrarItemsEnCurso(List<ItemVenta> items, double totalEstimado) {
        if (items.isEmpty()) return;
        ConsolaUtils.imprimirLinea();
        System.out.println("  Items en la venta actual:");
        items.forEach(item -> System.out.printf(
            "    - %s x%d  ->  %s%n",
            item.getCelular().getMarca().getNombre() + " " + item.getCelular().getModelo(),
            item.getCantidad(),
            ConsolaUtils.formatearPrecio(item.getSubtotal())
        ));
        System.out.println("  Total estimado con IVA: " + ConsolaUtils.formatearPrecio(totalEstimado));
        ConsolaUtils.imprimirLinea();
    }

    public void mostrarComprobante(Venta venta) {
        ConsolaUtils.imprimirComprobante(venta);
    }

    public void cerrar() {
        sc.close();
    }
}