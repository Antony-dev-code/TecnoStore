package com.tecnostore.modelo;

// Un empleado de la tienda. El tipo dice si es cajero, bodeguero o administrador.
public class Empleado extends Persona {

    private static final long serialVersionUID = 1L;
    private TipoEmpleado tipo;

    public Empleado(int id, String nombre, String identificacion,
                    String telefono, String correo, TipoEmpleado tipo) {
        super(id, nombre, identificacion, telefono, correo);
        this.tipo = tipo;
    }

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