package com.tecnostore;

import com.tecnostore.controlador.Controlador;
import com.tecnostore.persistencia.*;
import com.tecnostore.servicio.*;
import com.tecnostore.util.ConsolaUtils;
import com.tecnostore.vista.Vista;
import java.sql.Connection;

// Punto de arranque: crea las piezas del programa y le cede el control al menu.
public class Main {

    public static void main(String[] args) {

        Vista vista = new Vista();
        vista.mostrarBienvenida();

        // Conectar a la base de datos
        ConexionDB db;
        try {
            db = ConexionDB.getInstancia();
            if (!db.estaConectado()) {
                vista.mostrarError("No se pudo establecer la conexion a la base de datos.");
                return;
            }
            vista.mostrarExito("Conexion establecida correctamente.");
        } catch (RuntimeException e) {
            vista.mostrarError(e.getMessage());
            return;
        }

        Connection conn = db.getConexion();

        // Acceso a datos
        CelularDAO  celularDAO  = new CelularDAO(conn);
        ClienteDAO  clienteDAO  = new ClienteDAO(conn);
        VentaDAO    ventaDAO    = new VentaDAO(conn);
        MarcaDAO    marcaDAO    = new MarcaDAO(conn);
        PrecioDAO   precioDAO   = new PrecioDAO(conn);
        EmpleadoDAO empleadoDAO = new EmpleadoDAO(conn);

        // Logica del negocio
        GestorCelulares gestorCelulares = new GestorCelulares(celularDAO);
        GestorClientes  gestorClientes  = new GestorClientes(clienteDAO);
        GestorVentas    gestorVentas    = new GestorVentas(ventaDAO, clienteDAO, celularDAO);

        Controlador controlador = new Controlador(
            vista,
            gestorCelulares, gestorClientes, gestorVentas,
            marcaDAO, precioDAO, empleadoDAO,
            db
        );

        controlador.iniciar();
    }
}