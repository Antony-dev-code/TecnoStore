package com.tecnostore.modelo;

import java.io.Serializable;

// Una marca de celular, por ejemplo Samsung o Apple.
public class Marca implements Serializable {

    private static final long serialVersionUID = 1L;
    private int    id;
    private String nombre;

    public Marca(int id, String nombre) { this.id = id; this.nombre = nombre; }
    public Marca(String nombre)         { this.nombre = nombre; }

    public int    getId()                 { return id; }
    public void   setId(int id)           { this.id = id; }
    public String getNombre()             { return nombre; }
    public void   setNombre(String n)     { this.nombre = n; }

    @Override
    public String toString() { return "Marca{id=" + id + ", nombre='" + nombre + "'}"; }
}

//