package com.tecnostore.util;

// Reune las revisiones de datos que usa el resto del programa.
public class Validador {

    private Validador() {}

    // El precio debe ser mayor a cero y no pasarse del limite.
    public static boolean esPrecioValido(double precio) {
        return precio >= 0.01 && precio <= 999_999_999.99;
    }

    // El stock no puede ser negativo ni pasarse del limite.
    public static boolean esStockValido(int stock) {
        return stock >= 0 && stock <= 999_999;
    }

    // Revisa que el correo tenga una forma valida, por ejemplo ana@gmail.com.
    public static boolean esCorreoValido(String correo) {
        if (correo == null || correo.isBlank()) return false;

        int posArroba = correo.indexOf('@');

        if (posArroba <= 0) return false;
        if (correo.indexOf('@', posArroba + 1) != -1) return false;

        String despues = correo.substring(posArroba + 1);
        int posPunto = despues.indexOf('.');
        if (posPunto <= 0) return false;
        if (posPunto >= despues.length() - 1) return false;

        return true;
    }

    // El telefono debe tener entre 7 y 15 numeros.
    public static boolean esTelefonoValido(String telefono) {
        if (telefono == null || telefono.isBlank()) return false;
        if (telefono.length() < 7 || telefono.length() > 15) return false;

        for (char c : telefono.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    // El texto no puede estar vacio ni pasarse del largo maximo.
    public static boolean esTextoValido(String texto, int max) {
        if (texto == null || texto.isBlank()) return false;
        return texto.trim().length() <= max;
    }

    // La cantidad debe estar entre 1 y 100.
    public static boolean esCantidadValida(int cantidad) {
        return cantidad >= 1 && cantidad <= 100;
    }
}