package com.tecnostore.patron;

// Distintas formas de calcular un descuento sobre el total.
public interface EstrategiaDescuento {

    // Recibe el total y devuelve el monto ya con el descuento.
    double aplicar(double totalSinIva);
}