package com.tecnostore.patron;

/**
 * SinDescuento - implementacion por defecto del patron Strategy.
 * No aplica ningun descuento: retorna el total exactamente como viene.
 */
public class SinDescuento implements EstrategiaDescuento {

    @Override
    public double aplicar(double totalSinIva) {
        return totalSinIva; // sin cambios
    }
}