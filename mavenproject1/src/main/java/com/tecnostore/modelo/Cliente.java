package com.tecnostore.modelo;

// Un cliente de la tienda. El rut puede quedar vacio.
public class Cliente extends Persona {

    private static final long serialVersionUID = 1L;
    private String rut;

    public Cliente(int id, String nombre, String identificacion,
                   String telefono, String correo, String rut) {
        super(id, nombre, identificacion, telefono, correo);
        this.rut = rut;
    }

    public Cliente(String nombre, String identificacion,
                   String telefono, String correo) {
        super(nombre, identificacion, telefono, correo);
        this.rut = null;
    }

    public Cliente(String nombre, String identificacion,
                   String telefono, String correo, String rut) {
        super(nombre, identificacion, telefono, correo);
        this.rut = rut;
    }

    public String getRut()           { return rut; }
    public void   setRut(String rut) { this.rut = rut; }

    @Override
    public String toString() {
        return "Cliente{id=" + getId() + ", nombre='" + getNombre() + "'"
                + ", identificacion='" + getIdentificacion() + "'"
                + ", telefono='" + getTelefono() + "'"
                + ", correo='" + getCorreo() + "'"
                + ", rut='" + (rut != null ? rut : "N/A") + "'}";
    }
}