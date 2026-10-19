package com.tecnostore.patron;

// Deja el total igual, sin ningun descuento.
public class SinDescuento implements EstrategiaDescuento {

    @Override
    public double aplicar(double totalSinIva) {
        return totalSinIva; // sin cambios
    }
}