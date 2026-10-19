package com.tecnostore.util;

import com.tecnostore.modelo.ItemVenta;
import com.tecnostore.modelo.Venta;

/**
 * ConsolaUtils - utilidad para dar formato estetico a la consola.
 *
 * Centraliza todo el formato visual: bordes, tablas, titulos y comprobantes.
 * Todos los metodos son estaticos. Uso: ConsolaUtils.imprimirTitulo("MENU")
 *
 * Las tablas usan ANCHOS FIJOS por columna: cada celda se rellena con espacios
 * o se trunca con "..." si el texto es mas largo que el ancho asignado.
 * Asi las columnas siempre quedan alineadas aunque los nombres sean largos.
 */
public class ConsolaUtils {

    private static final int ANCHO = 74;

    private ConsolaUtils() {}

    // -------------------------------------------------------------------------
    // Titulo enmarcado
    // -------------------------------------------------------------------------
    public static void imprimirTitulo(String titulo) {
        String linea = "=".repeat(ANCHO);
        System.out.println();
        System.out.println(linea);
        System.out.println(centrar(titulo));
        System.out.println(linea);
    }

    // -------------------------------------------------------------------------
    // Linea separadora simple
    // -------------------------------------------------------------------------
    public static void imprimirLinea() {
        System.out.println("-".repeat(ANCHO));
    }

    // -------------------------------------------------------------------------
    // Linea en blanco
    // -------------------------------------------------------------------------
    public static void imprimirEspacio() {
        System.out.println();
    }

    // -------------------------------------------------------------------------
    // TABLAS CON ANCHOS FIJOS
    //
    // Para imprimir una tabla:
    //   1. Define un arreglo int[] con el ancho de cada columna
    //   2. Llama a imprimirEncabezado(anchos, "COL1", "COL2", ...)
    //   3. Por cada fila llama a imprimirFila(anchos, "val1", "val2", ...)
    //
    // Cada celda se ajusta EXACTAMENTE al ancho dado:
    //   - Si el texto es mas corto, se rellena con espacios
    //   - Si es mas largo, se corta y termina en "..."
    // -------------------------------------------------------------------------

    /** Imprime la fila de encabezado (en mayusculas) seguida de una linea. */
    public static void imprimirEncabezado(int[] anchos, String... columnas) {
        imprimirFila(anchos, columnas);
        StringBuilder sb = new StringBuilder("+");
        for (int ancho : anchos) {
            sb.append("-".repeat(ancho + 2)).append("+");
        }
        System.out.println(sb.toString());
    }

    /** Imprime una fila de datos con cada valor ajustado al ancho de su columna. */
    public static void imprimirFila(int[] anchos, String... valores) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < anchos.length; i++) {
            String valor = (i < valores.length && valores[i] != null) ? valores[i] : "";
            sb.append(" ").append(ajustar(valor, anchos[i])).append(" |");
        }
        System.out.println(sb.toString());
    }

    /**
     * Ajusta un texto a un ancho exacto:
     *   - Si es mas largo, lo corta y agrega "..." al final
     *   - Si es mas corto, lo rellena con espacios a la derecha
     */
    private static String ajustar(String texto, int ancho) {
        if (texto == null) texto = "";
        if (texto.length() > ancho) {
            if (ancho <= 3) return texto.substring(0, ancho);
            return texto.substring(0, ancho - 3) + "...";
        }
        return String.format("%-" + ancho + "s", texto);
    }

    // -------------------------------------------------------------------------
    // Mensajes
    // -------------------------------------------------------------------------
    public static void imprimirExito(String mensaje) {
        System.out.println();
        System.out.println("  [OK] " + mensaje);
        System.out.println();
    }

    public static void imprimirError(String mensaje) {
        System.out.println();
        System.out.println("  [ERROR] " + mensaje);
        System.out.println();
    }

    public static void imprimirAviso(String mensaje) {
        System.out.println();
        System.out.println("  [!] " + mensaje);
        System.out.println();
    }

    // -------------------------------------------------------------------------
    // Comprobante de venta completo
    // -------------------------------------------------------------------------
    public static void imprimirComprobante(Venta venta) {
        String linea = "=".repeat(ANCHO);
        String sep   = "-".repeat(ANCHO);

        System.out.println();
        System.out.println(linea);
        System.out.println(centrar("COMPROBANTE DE VENTA"));
        System.out.println(linea);

        System.out.printf("  Venta #%-10d %s%n",
            venta.getId(),
            venta.getFecha().toString().replace("T", " ").substring(0, 16));
        System.out.printf("  Cliente : %s%n", venta.getCliente().getNombre());
        System.out.printf("  Cajero  : %s%n", venta.getEmpleado().getNombre());

        System.out.println(sep);
        System.out.printf("  %-40s %6s  %18s%n", "PRODUCTO", "CANT", "SUBTOTAL");
        System.out.println(sep);

        for (ItemVenta item : venta.getItems()) {
            String nombreCel = item.getCelular().getMarca().getNombre()
                             + " " + item.getCelular().getModelo();
            if (nombreCel.length() > 40) nombreCel = nombreCel.substring(0, 37) + "...";
            System.out.printf("  %-40s %6d  %18s%n",
                nombreCel, item.getCantidad(), formatearPrecio(item.getSubtotal()));
        }

        System.out.println(sep);

        double totalConIva    = venta.getTotal();
        double subtotalSinIva = Math.round((totalConIva / 1.19) * 100.0) / 100.0;
        double iva            = Math.round((totalConIva - subtotalSinIva) * 100.0) / 100.0;

        System.out.printf("  %-48s %18s%n", "Subtotal sin IVA:", formatearPrecio(subtotalSinIva));
        System.out.printf("  %-48s %18s%n", "IVA (19%):",        formatearPrecio(iva));
        System.out.println(sep);
        System.out.printf("  %-48s %18s%n", "TOTAL:", formatearPrecio(totalConIva));
        System.out.println(linea);
        System.out.println();
    }

    // -------------------------------------------------------------------------
    // Metodos de apoyo
    // -------------------------------------------------------------------------

    private static String centrar(String texto) {
        if (texto.length() >= ANCHO) return texto;
        int espacios = (ANCHO - texto.length()) / 2;
        return " ".repeat(espacios) + texto;
    }

    public static String formatearPrecio(double valor) {
        long entero   = (long) valor;
        long centavos = Math.round((valor - entero) * 100);
        return String.format("$%s,%02d", formatearMiles(entero), centavos);
    }

    private static String formatearMiles(long numero) {
        String s = Long.toString(numero);
        StringBuilder sb = new StringBuilder();
        int inicio = s.length() % 3;
        if (inicio > 0) sb.append(s, 0, inicio);
        for (int i = inicio; i < s.length(); i += 3) {
            if (sb.length() > 0) sb.append('.');
            sb.append(s, i, i + 3);
        }
        return sb.toString();
    }
}