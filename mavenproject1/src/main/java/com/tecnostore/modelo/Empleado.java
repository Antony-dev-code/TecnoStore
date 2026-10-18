package com.tecnostore.modelo;

/**
 * Representa a un empleado de TecnoStore.
 * Hereda de Persona: id, nombre, identificacion, telefono, correo.
 * El tipo indica su rol: CAJERO, BODEGUERO o ADMINISTRADOR.
 */
public class Empleado extends Persona {

    private static final long serialVersionUID = 1L;
    private TipoEmpleado tipo;

    /** Para cargar desde la base de datos. */
    public Empleado(int id, String nombre, String identificacion,
                    String telefono, String correo, TipoEmpleado tipo) {
        super(id, nombre, identificacion, telefono, correo);
        this.tipo = tipo;
    }

    /** Para registrar un empleado nuevo. */
    public Empleado(String nombre, String identificacion,
                    String telefono, String correo, TipoEmpleado tipo) {
        super(nombre, identificacion, telefono, correo);
        this.tipo = tipo;
    }

    public TipoEmpleado getTipo()              { return tipo; }
    public void         setTipo(TipoEmpleado t){ this.tipo = t; }

    @Override
    public String toString() {
        return "Empleado{id=" + getId() + ", nombre='" + getNombre() + "'"
                + ", identificacion='" + getIdentificacion() + "'"
                + ", tipo=" + tipo + "}";
    }
}