package com.tecnostore.util;

/**
 * Validador - clase de utilidad con metodos estaticos de validacion.
 *
 * Centraliza todas las reglas de validacion en un solo lugar.
 * Todos los metodos son estaticos: no se necesita crear un objeto Validador.
 * Uso: Validador.esCorreoValido("correo@dominio.com")
 */
public class Validador {

    // Constructor privado: esta clase no se instancia
    private Validador() {}

    /**
     * Valida que el precio sea mayor a 0 y no supere el limite maximo.
     * Ejemplos validos:   0.01, 999990.00, 5299990.00
     * Ejemplos invalidos: 0, -100, 1000000000.00
     */
    public static boolean esPrecioValido(double precio) {
        return precio >= 0.01 && precio <= 999_999_999.99;
    }

    /**
     * Valida que el stock sea un numero no negativo dentro del limite.
     * Ejemplos validos:   0, 1, 30, 999999
     * Ejemplos invalidos: -1, 1000000
     */
    public static boolean esStockValido(int stock) {
        return stock >= 0 && stock <= 999_999;
    }

    /**
     * Valida el formato basico de un correo electronico.
     * Reglas:
     *   - Debe contener exactamente un '@'
     *   - Debe haber al menos un caracter antes del '@'
     *   - Debe haber al menos un '.' despues del '@' con caracteres en ambos lados
     *
     * Ejemplos validos:   ana@gmail.com, usuario@empresa.co
     * Ejemplos invalidos: sinArroba, @sinNombre.com, correo@sinPunto
     */
    public static boolean esCorreoValido(String correo) {
        if (correo == null || correo.isBlank()) return false;

        int posArroba = correo.indexOf('@');

        // Debe existir exactamente un '@'
        if (posArroba <= 0) return false;
        if (correo.indexOf('@', posArroba + 1) != -1) return false;

        // La parte despues del '@' debe tener un '.' con caracteres en ambos lados
        String despues = correo.substring(posArroba + 1);
        int posPunto = despues.indexOf('.');
        if (posPunto <= 0) return false;
        if (posPunto >= despues.length() - 1) return false;

        return true;
    }

    /**
     * Valida que el telefono tenga entre 7 y 15 digitos numericos.
     * Ejemplos validos:   3001234567, 1234567
     * Ejemplos invalidos: 123, 3001234567890123, 300-123-4567
     */
    public static boolean esTelefonoValido(String telefono) {
        if (telefono == null || telefono.isBlank()) return false;
        if (telefono.length() < 7 || telefono.length() > 15) return false;

        // Todos los caracteres deben ser digitos
        for (char c : telefono.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    /**
     * Valida que un texto no sea nulo, no este vacio y no supere la longitud maxima.
     * Uso: Validador.esTextoValido(nombre, 100)
     */
    public static boolean esTextoValido(String texto, int max) {
        if (texto == null || texto.isBlank()) return false;
        return texto.trim().length() <= max;
    }

    /**
     * Valida que una cantidad sea un entero entre 1 y 100.
     * Ejemplos validos:   1, 5, 100
     * Ejemplos invalidos: 0, -1, 101
     */
    public static boolean esCantidadValida(int cantidad) {
        return cantidad >= 1 && cantidad <= 100;
    }
}