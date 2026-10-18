package com.tecnostore.modelo;

import java.io.Serializable;

/**
 * Representa un telefono celular en el inventario de TecnoStore.
 */
public class Celular implements Serializable {

    private static final long serialVersionUID = 1L;

    private int              id;
    private String           modelo;
    private Marca            marca;
    private SistemaOperativo sistemaOperativo;
    private CategoriaGama    gama;
    private Precio           precio;
    private int              stock;

    /** Para cargar desde la base de datos (con id). */
    public Celular(int id, String modelo, Marca marca,
                   SistemaOperativo sistemaOperativo, CategoriaGama gama,
                   Precio precio, int stock) {
        this.id = id; this.modelo = modelo; this.marca = marca;
        this.sistemaOperativo = sistemaOperativo; this.gama = gama;
        this.precio = precio; this.stock = stock;
    }

    /** Para crear un celular nuevo (sin id). */
    public Celular(String modelo, Marca marca,
                   SistemaOperativo sistemaOperativo, CategoriaGama gama,
                   Precio precio, int stock) {
        this.modelo = modelo; this.marca = marca;
        this.sistemaOperativo = sistemaOperativo; this.gama = gama;
        this.precio = precio; this.stock = stock;
    }

    public int              getId()                                    { return id; }
    public void             setId(int id)                              { this.id = id; }
    public String           getModelo()                                { return modelo; }
    public void             setModelo(String m)                        { this.modelo = m; }
    public Marca            getMarca()                                 { return marca; }
    public void             setMarca(Marca m)                          { this.marca = m; }
    public SistemaOperativo getSistemaOperativo()                      { return sistemaOperativo; }
    public void             setSistemaOperativo(SistemaOperativo so)   { this.sistemaOperativo = so; }
    public CategoriaGama    getGama()                                  { return gama; }
    public void             setGama(CategoriaGama g)                   { this.gama = g; }
    public Precio           getPrecio()                                { return precio; }
    public void             setPrecio(Precio p)                        { this.precio = p; }
    public int              getStock()                                 { return stock; }
    public void             setStock(int s)                            { this.stock = s; }

    @Override
    public String toString() {
        return "Celular{id=" + id
                + ", marca='" + (marca != null ? marca.getNombre() : "N/A") + "'"
                + ", modelo='" + modelo + "'"
                + ", so=" + sistemaOperativo + ", gama=" + gama
                + ", precio=" + (precio != null ? precio.getValor() : 0.0)
                + ", stock=" + stock + "}";
    }
}