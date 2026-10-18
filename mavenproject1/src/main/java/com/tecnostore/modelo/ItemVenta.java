package com.tecnostore.modelo;

import java.io.Serializable;

/**
 * Representa una linea dentro de una venta.
 * Ejemplo: 2 unidades de iPhone 15 Pro a 5.299.990 c/u = subtotal 10.599.980
 * Se guarda el precio en el momento de la compra para preservar el historial.
 */
public class ItemVenta implements Serializable {

    private static final long serialVersionUID = 1L;

    private int     id;
    private Celular celular;
    private int     cantidad;
    private double  subtotal;

    /** Para crear un item nuevo: el subtotal se calcula automaticamente. */
    public ItemVenta(Celular celular, int cantidad) {
        this.celular  = celular;
        this.cantidad = cantidad;
        this.subtotal = Math.round(celular.getPrecio().getValor() * cantidad * 100.0) / 100.0;
    }

    /** Para cargar desde la base de datos (subtotal ya calculado). */
    public ItemVenta(int id, Celular celular, int cantidad, double subtotal) {
        this.id = id; this.celular = celular;
        this.cantidad = cantidad; this.subtotal = subtotal;
    }

    public int     getId()                    { return id; }
    public void    setId(int id)              { this.id = id; }
    public Celular getCelular()               { return celular; }
    public void    setCelular(Celular c)      {
        this.celular  = c;
        this.subtotal = Math.round(c.getPrecio().getValor() * cantidad * 100.0) / 100.0;
    }
    public int     getCantidad()              { return cantidad; }
    public void    setCantidad(int cantidad)  {
        this.cantidad = cantidad;
        this.subtotal = Math.round(celular.getPrecio().getValor() * cantidad * 100.0) / 100.0;
    }
    public double  getSubtotal()              { return subtotal; }

    @Override
    public String toString() {
        String nombre = celular != null
                ? celular.getMarca().getNombre() + " " + celular.getModelo() : "N/A";
        return "ItemVenta{id=" + id + ", celular='" + nombre + "'"
                + ", cantidad=" + cantidad + ", subtotal=" + subtotal + "}";
    }
}