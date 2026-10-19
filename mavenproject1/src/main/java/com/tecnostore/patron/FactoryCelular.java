package com.tecnostore.patron;

import com.tecnostore.modelo.*;
import com.tecnostore.util.Validador;

// Crea celulares en un solo lugar, revisando antes que los datos esten bien.
public class FactoryCelular {

    private FactoryCelular() {}

    // Revisa los datos y devuelve el celular ya armado.
    public static Celular crear(String modelo, Marca marca,
                                SistemaOperativo sistemaOperativo, CategoriaGama gama,
                                Precio precio, int stock) {

        if (!Validador.esTextoValido(modelo, 100)) {
            throw new IllegalArgumentException(
                "El modelo no puede estar vacio y debe tener maximo 100 caracteres.");
        }
        if (marca == null) {
            throw new IllegalArgumentException("La marca no puede ser nula.");
        }
        if (sistemaOperativo == null) {
            throw new IllegalArgumentException("El sistema operativo no puede ser nulo.");
        }
        if (gama == null) {
            throw new IllegalArgumentException("La gama no puede ser nula.");
        }
        if (precio == null || !Validador.esPrecioValido(precio.getValor())) {
            throw new IllegalArgumentException(
                "El precio debe ser mayor a 0 y menor a 999.999.999,99.");
        }
        if (!Validador.esStockValido(stock)) {
            throw new IllegalArgumentException(
                "El stock debe ser un numero entre 0 y 999.999.");
        }

        return new Celular(modelo, marca, sistemaOperativo, gama, precio, stock);
    }
}