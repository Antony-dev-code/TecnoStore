package com.tecnostore.modelo;

import java.io.Serializable;

/**
 * Clase base abstracta que representa a cualquier persona en el sistema.
 * Es abstracta: nunca se crea un objeto Persona directamente.
 * Siempre se crea un Cliente o un Empleado, que heredan de esta clase.
 */
public abstract class Persona implements Serializable {

    private static final long serialVersionUID = 1L;

    private int    id;
    private String nombre;
    private String identificacion;
    private String telefono;
    private String correo;

    /** Constructor para cargar desde la base de datos (con id). */
    public Persona(int id, String nombre, String identificacion,
                   String telefono, String correo) {
        this.id             = id;
        this.nombre         = nombre;
        this.identificacion = identificacion;
        this.telefono       = telefono;
        this.correo         = correo;
    }

    /** Constructor para crear una persona nueva (sin id todavia). */
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