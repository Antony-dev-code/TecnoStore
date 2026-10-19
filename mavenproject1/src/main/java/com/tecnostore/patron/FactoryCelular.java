package com.tecnostore.patron;

import com.tecnostore.modelo.*;
import com.tecnostore.util.Validador;

/**
 * FactoryCelular - patron Factory Method.
 *
 * Centraliza la construccion de objetos Celular.
 * En lugar de escribir "new Celular(...)" en varios lugares,
 * siempre se llama a FactoryCelular.crear(...).
 *
 * Ventaja: si la forma de construir un Celular cambia
 * (por ejemplo, se agrega un nuevo campo), solo se modifica aqui.
 *
 * El metodo valida los datos antes de construir el objeto.
 * Si algo es invalido, lanza IllegalArgumentException con un mensaje claro.
 */
public class FactoryCelular {

    // Constructor privado: esta clase no se instancia, solo se usa estaticamente
    private FactoryCelular() {}

    /**
     * Crea y retorna un objeto Celular validado.
     *
     * @param modelo          nombre del modelo (ej: "Galaxy S24 Ultra")
     * @param marca           objeto Marca ya existente
     * @param sistemaOperativo ANDROID o IOS
     * @param gama            ALTA, MEDIA o BAJA
     * @param precio          objeto Precio ya existente
     * @param stock           cantidad inicial en inventario
     * @return Celular construido y listo para guardar en la BD
     * @throws IllegalArgumentException si alguno de los datos es invalido
     */
    public static Celular crear(String modelo, Marca marca,
                                SistemaOperativo sistemaOperativo, CategoriaGama gama,
                                Precio precio, int stock) {

        // Validaciones antes de construir
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