package com.tecnostore.modelo;

/**
 * Representa a un cliente de TecnoStore.
 * Hereda de Persona: id, nombre, identificacion, telefono, correo.
 * El rut es opcional (puede ser null).
 */
public class Cliente extends Persona {

    private static final long serialVersionUID = 1L;
    private String rut;

    /** Para cargar desde la base de datos. */
    public Cliente(int id, String nombre, String identificacion,
                   String telefono, String correo, String rut) {
        super(id, nombre, identificacion, telefono, correo);
        this.rut = rut;
    }

    /** Para registrar un cliente nuevo sin rut. */
    public Cliente(String nombre, String identificacion,
                   String telefono, String correo) {
        super(nombre, identificacion, telefono, correo);
        this.rut = null;
    }

    /** Para registrar un cliente nuevo con rut. */
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