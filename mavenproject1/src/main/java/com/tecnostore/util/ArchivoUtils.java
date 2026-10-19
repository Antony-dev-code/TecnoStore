package com.tecnostore.util;

import com.tecnostore.modelo.Venta;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// Guarda y lee el reporte de ventas en el archivo reporte_ventas.dat.
public class ArchivoUtils {

    private static final String NOMBRE_ARCHIVO = "reporte_ventas.dat";
    private static final int[] ANCHOS = {4, 12, 20, 18, 16};

    private ArchivoUtils() {}

    // Crea el archivo con las ventas que haya en ese momento
    public static void generarReporte(List<Venta> ventas) {
        File archivo = new File(NOMBRE_ARCHIVO);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(archivo))) {
            oos.writeObject(ventas);
            if (ventas.isEmpty()) {
                ConsolaUtils.imprimirAviso("Reporte generado: no hay ventas registradas aun.");
            } else {
                ConsolaUtils.imprimirExito(
                    "Archivo reporte_ventas.dat generado correctamente.\n"
                    + "  Ruta: " + archivo.getAbsolutePath()
                    + "\n  Ventas incluidas: " + ventas.size()
                );
            }
        } catch (IOException e) {
            ConsolaUtils.imprimirError("No se pudo acceder al archivo de reporte.");
            e.printStackTrace(System.err);
        }
    }

    // Lee el archivo. Si no existe o falla, devuelve una lista vacia
    @SuppressWarnings("unchecked")
    public static List<Venta> leerReporte() {
        File archivo = new File(NOMBRE_ARCHIVO);
        if (!archivo.exists()) {
            ConsolaUtils.imprimirAviso(
                "El archivo reporte_ventas.dat no existe todavia. "
                + "Genera el reporte primero desde el menu.");
            return new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivo))) {
            return (List<Venta>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            ConsolaUtils.imprimirError("No se pudo acceder al archivo de reporte.");
            e.printStackTrace(System.err);
            return new ArrayList<>();
        }
    }

    // Muestra en pantalla lo que guarda el archivo
    public static void mostrarReporte() {
        List<Venta> ventas = leerReporte();
        ConsolaUtils.imprimirTitulo("REPORTE DE VENTAS - ARCHIVO SERIALIZADO");

        if (ventas.isEmpty()) {
            ConsolaUtils.imprimirAviso("El reporte no contiene ventas.");
            return;
        }

        DateTimeFormatter fmtGen = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        DateTimeFormatter fmtDia = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        System.out.println("  Fecha de generacion: " + LocalDateTime.now().format(fmtGen));
        ConsolaUtils.imprimirLinea();
        ConsolaUtils.imprimirEncabezado(ANCHOS, "ID", "FECHA", "CLIENTE", "CAJERO", "TOTAL");

        ventas.forEach(v ->
            ConsolaUtils.imprimirFila(ANCHOS,
                String.valueOf(v.getId()),
                v.getFecha().format(fmtDia),
                v.getCliente().getNombre(),
                v.getEmpleado().getNombre(),
                ConsolaUtils.formatearPrecio(v.getTotal())
            )
        );

        double granTotal = ventas.stream().mapToDouble(Venta::getTotal).sum();
        ConsolaUtils.imprimirLinea();
        System.out.printf("  Total ventas: %-5d  Gran total: %s%n",
            ventas.size(), ConsolaUtils.formatearPrecio(granTotal));
        ConsolaUtils.imprimirEspacio();
    }
}