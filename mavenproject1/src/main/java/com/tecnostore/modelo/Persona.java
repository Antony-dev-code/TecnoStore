package com.tecnostore.modelo;

import java.io.Serializable;

// Base comun para Cliente y Empleado.
public abstract class Persona implements Serializable {

    private static final long serialVersionUID = 1L;

    private int    id;
    private String nombre;
    private String identificacion;
    private String telefono;
    private String correo;

    public Persona(int id, String nombre, String identificacion,
                   String telefono, String correo) {
        this.id             = id;
        this.nombre         = nombre;
        this.identificacion = identificacion;
        this.telefono       = telefono;
        this.correo         = correo;
    }

    public Persona(String nombre, String identificacion,
                   String telefono, String correo) {
        this.nombre         = nombre;
        this.identificacion = identificacion;
        this.telefono       = telefono;
        this.correo         = correo;
    }

    public int    getId()               { return id; }
    public void   setId(int id)         { this.id = id; }

    public String getNombre()           { return nombre; }
    public void   setNombre(String n)   { this.nombre = n; }

    public String getIdentificacion()            { return identificacion; }
    public void   setIdentificacion(String i)    { this.identificacion = i; }

    public String getTelefono()         { return telefono; }
    public void   setTelefono(String t) { this.telefono = t; }

    public String getCorreo()           { return correo; }
    public void   setCorreo(String c)   { this.correo = c; }

    @Override
    public String toString() {
        return "Persona{id=" + id + ", nombre='" + nombre + "'"
                + ", identificacion='" + identificacion + "'"
                + ", telefono='" + telefono + "', correo='" + correo + "'}";
    }
}