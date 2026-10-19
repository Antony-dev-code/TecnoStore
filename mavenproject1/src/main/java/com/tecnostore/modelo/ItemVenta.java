package com.tecnostore.modelo;

import java.io.Serializable;

// Una linea de una venta: un celular, la cantidad y su subtotal.
public class ItemVenta implements Serializable {

    private static final long serialVersionUID = 1L;

    private int     id;
    private Celular celular;
    private int     cantidad;
    private double  subtotal;

    public ItemVenta(Celular celular, int cantidad) {
        this.celular  = celular;
        this.cantidad = cantidad;
        this.subtotal = Math.round(celular.getPrecio().getValor() * cantidad * 100.0) / 100.0;
    }

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