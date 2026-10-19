package com.tecnostore.patron;

/**
 * DescuentoPorcentaje - aplica un descuento porcentual al total sin IVA.
 *
 * Ejemplo: DescuentoPorcentaje(0.10) aplica un 10% de descuento.
 *   totalSinIva = 1.000.000
 *   resultado   =   900.000  (1.000.000 * (1 - 0.10))
 */
public class DescuentoPorcentaje implements EstrategiaDescuento {

    private final double porcentaje; // valor entre 0.0 y 1.0

    /**
     * @param porcentaje valor del descuento, ejemplo: 0.10 para 10%, 0.05 para 5%
     */
    public DescuentoPorcentaje(double porcentaje) {
        if (porcentaje < 0 || porcentaje >= 1) {
            throw new IllegalArgumentException(
                "El porcentaje debe estar entre 0.0 y 0.99. Valor recibido: " + porcentaje);
        }
        this.porcentaje = porcentaje;
    }

    @Override
    public double aplicar(double totalSinIva) {
        double resultado = totalSinIva * (1 - porcentaje);
        return Math.round(resultado * 100.0) / 100.0;
    }

    public double getPorcentaje() { return porcentaje; }
}