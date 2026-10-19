package com.tecnostore.patron;

// Resta un porcentaje al total. Por ejemplo, 0.10 quita el 10%.
public class DescuentoPorcentaje implements EstrategiaDescuento {

    private final double porcentaje; // valor entre 0.0 y 1.0

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