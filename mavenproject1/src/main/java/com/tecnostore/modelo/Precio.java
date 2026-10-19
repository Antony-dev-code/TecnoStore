package com.tecnostore.modelo;

import java.io.Serializable;

// El precio de un celular, con un nombre y su valor.
public class Precio implements Serializable {

    private static final long serialVersionUID = 1L;
    private int    id;
    private String nombre;
    private double valor;

    public Precio(int id, String nombre, double valor) {
        this.id = id; this.nombre = nombre; this.valor = valor;
    }
    public Precio(String nombre, double valor) {
        this.nombre = nombre; this.valor = valor;
    }

    public int    getId()               { return id; }
    public void   setId(int id)         { this.id = id; }
    public String getNombre()           { return nombre; }
    public void   setNombre(String n)   { this.nombre = n; }
    public double getValor()            { return valor; }
    public void   setValor(double v)    { this.valor = v; }

    @Override
    public String toString() {
        return "Precio{id=" + id + ", nombre='" + nombre + "', valor=" + valor + "}";
    }
}