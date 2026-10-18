package com.tecnostore.modelo;

/**
 * Rol del empleado dentro de la tienda TecnoStore.
 * CAJERO        - atiende ventas en caja
 * BODEGUERO     - gestiona el inventario fisico
 * ADMINISTRADOR - acceso completo al sistema
 */
public enum TipoEmpleado {
    CAJERO,
    BODEGUERO,
    ADMINISTRADOR
}