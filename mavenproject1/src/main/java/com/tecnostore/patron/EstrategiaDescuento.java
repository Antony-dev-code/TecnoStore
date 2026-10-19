package com.tecnostore.patron;

/**
 * EstrategiaDescuento - interfaz del patron Strategy.
 *
 * El patron Strategy permite cambiar el algoritmo de calculo de descuentos
 * sin modificar GestorVentas. Solo cambia la implementacion que se usa.
 *
 * Ejemplo de uso en GestorVentas:
 *   gestor.setEstrategiaDescuento(new SinDescuento());
 *   gestor.setEstrategiaDescuento(new DescuentoPorcentaje(0.10));
 */
public interface EstrategiaDescuento {

    /**
     * Aplica el descuento al total sin IVA y retorna el resultado.
     * @param totalSinIva el monto antes de aplicar IVA
     * @return el monto despues de aplicar el descuento
     */
    double aplicar(double totalSinIva);
}