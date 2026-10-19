package com.tecnostore.util;

import com.tecnostore.modelo.Celular;
import com.tecnostore.modelo.ItemVenta;
import com.tecnostore.modelo.Venta;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.*;

// Arma los reportes a partir de listas ya cargadas.
public class ReporteUtils {

    private ReporteUtils() {}

    private static final int[] ANCHOS_STOCK = {4, 14, 22, 8, 7};
    private static final int[] ANCHOS_TOP   = {10, 36, 18};
    private static final int[] ANCHOS_MES   = {12, 22};

    // Celulares con poco stock (menos de 5 unidades).
    public static void mostrarStockBajo(List<Celular> celulares) {
        ConsolaUtils.imprimirTitulo("REPORTE: STOCK BAJO (menos de 5 unidades)");

        List<Celular> stockBajo = celulares.stream()
                .filter(c -> c.getStock() < 5)
                .sorted(Comparator.comparingInt(Celular::getStock))
                .collect(Collectors.toList());

        if (stockBajo.isEmpty()) {
            ConsolaUtils.imprimirAviso("No hay datos disponibles para este reporte.");
            return;
        }

        ConsolaUtils.imprimirEncabezado(ANCHOS_STOCK, "ID", "MARCA", "MODELO", "GAMA", "STOCK");
        stockBajo.forEach(c ->
            ConsolaUtils.imprimirFila(ANCHOS_STOCK,
                String.valueOf(c.getId()),
                c.getMarca().getNombre(),
                c.getModelo(),
                c.getGama().name(),
                String.valueOf(c.getStock())
            )
        );
        ConsolaUtils.imprimirLinea();
        System.out.println("  Total celulares con stock bajo: " + stockBajo.size());
        ConsolaUtils.imprimirEspacio();
    }

    // Los celulares mas vendidos.
    public static void mostrarTopMasVendidos(List<Venta> ventas) {
        ConsolaUtils.imprimirTitulo("REPORTE: TOP CELULARES MAS VENDIDOS");

        if (ventas.isEmpty()) {
            ConsolaUtils.imprimirAviso("No hay datos disponibles para este reporte.");
            return;
        }

        Map<String, Integer> totalesPorModelo = ventas.stream()
                .flatMap(v -> v.getItems().stream())
                .collect(Collectors.groupingBy(
                    item -> item.getCelular().getMarca().getNombre()
                          + " " + item.getCelular().getModelo(),
                    Collectors.summingInt(ItemVenta::getCantidad)
                ));

        if (totalesPorModelo.isEmpty()) {
            ConsolaUtils.imprimirAviso("No hay datos disponibles para este reporte.");
            return;
        }

        int n = Math.min(3, totalesPorModelo.size());

        List<Map.Entry<String, Integer>> top = totalesPorModelo.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                    .thenComparing(Map.Entry.comparingByKey()))
                .limit(n)
                .collect(Collectors.toList());

        ConsolaUtils.imprimirEncabezado(ANCHOS_TOP, "POSICION", "MODELO", "UNIDADES");
        final int[] pos = {1};
        top.forEach(e ->
            ConsolaUtils.imprimirFila(ANCHOS_TOP,
                "#" + pos[0]++,
                e.getKey(),
                String.valueOf(e.getValue())
            )
        );
        ConsolaUtils.imprimirLinea();
        ConsolaUtils.imprimirEspacio();
    }

    // Cuanto se vendio en cada mes.
    public static void mostrarVentasPorMes(List<Venta> ventas) {
        ConsolaUtils.imprimirTitulo("REPORTE: VENTAS TOTALES POR MES");

        if (ventas.isEmpty()) {
            ConsolaUtils.imprimirAviso("No hay datos disponibles para este reporte.");
            return;
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/yyyy");

        Map<String, Double> totalesPorMes = ventas.stream()
                .collect(Collectors.groupingBy(
                    v -> v.getFecha().format(fmt),
                    Collectors.summingDouble(Venta::getTotal)
                ));

        List<Map.Entry<String, Double>> ordenado = totalesPorMes.entrySet().stream()
                .sorted(Comparator.comparing(e -> {
                    String[] partes = e.getKey().split("/");
                    return partes[1] + partes[0];
                }))
                .collect(Collectors.toList());

        ConsolaUtils.imprimirEncabezado(ANCHOS_MES, "PERIODO", "TOTAL RECAUDADO");
        ordenado.forEach(e ->
            ConsolaUtils.imprimirFila(ANCHOS_MES,
                e.getKey(),
                ConsolaUtils.formatearPrecio(e.getValue())
            )
        );

        double granTotal = ventas.stream().mapToDouble(Venta::getTotal).sum();
        ConsolaUtils.imprimirLinea();
        System.out.printf("  Gran total: %s%n", ConsolaUtils.formatearPrecio(granTotal));
        ConsolaUtils.imprimirEspacio();
    }
}