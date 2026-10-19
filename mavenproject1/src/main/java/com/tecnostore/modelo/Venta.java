package com.tecnostore.modelo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Una venta con su cliente, su empleado, los items y el total.
public class Venta implements Serializable {

    private static final long serialVersionUID = 1L;

    private int             id;
    private Cliente         cliente;
    private Empleado        empleado;
    private List<ItemVenta> items;
    private LocalDateTime   fecha;
    private double          total;

    public Venta(Cliente cliente, Empleado empleado, double total) {
        this.cliente  = cliente;
        this.empleado = empleado;
        this.total    = total;
        this.items    = new ArrayList<>();
        this.fecha    = LocalDateTime.now();
    }

    public Venta(int id, Cliente cliente, Empleado empleado,
                 List<ItemVenta> items, LocalDateTime fecha, double total) {
        this.id = id; this.cliente = cliente; this.empleado = empleado;
        this.items = items != null ? items : new ArrayList<>();
        this.fecha = fecha; this.total = total;
    }

    public void agregarItem(ItemVenta item) { this.items.add(item); }

    public int             getId()                            { return id; }
    public void            setId(int id)                      { this.id = id; }
    public Cliente         getCliente()                       { return cliente; }
    public void            setCliente(Cliente c)              { this.cliente = c; }
    public Empleado        getEmpleado()                      { return empleado; }
    public void            setEmpleado(Empleado e)            { this.empleado = e; }
    public List<ItemVenta> getItems()                         { return items; }
    public void            setItems(List<ItemVenta> items)    { this.items = items; }
    public LocalDateTime   getFecha()                         { return fecha; }
    public void            setFecha(LocalDateTime f)          { this.fecha = f; }
    public double          getTotal()                         { return total; }
    public void            setTotal(double total)             { this.total = total; }

    @Override
    public String toString() {
        return "Venta{id=" + id
                + ", cliente='" + (cliente  != null ? cliente.getNombre()  : "N/A") + "'"
                + ", empleado='"+ (empleado != null ? empleado.getNombre() : "N/A") + "'"
                + ", items="    + (items != null ? items.size() : 0) + " item(s)"
                + ", fecha="    + fecha + ", total=" + total + "}";
    }
}