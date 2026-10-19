package com.tecnostore;

import com.tecnostore.controlador.Controlador;
import com.tecnostore.persistencia.*;
import com.tecnostore.servicio.*;
import com.tecnostore.util.ConsolaUtils;
import com.tecnostore.vista.Vista;
import java.sql.Connection;

/**
 * Main - punto de entrada del sistema TecnoStore.
 *
 * En el patron MVC, Main actua como "punto de ensamblaje" (composition root):
 * su unica tarea es CREAR las piezas del sistema y CONECTARLAS entre si.
 * No contiene logica de negocio, ni menus, ni impresiones: todo eso vive
 * en el Controlador, la Vista y los gestores.
 *
 * Flujo de arranque:
 *   1. Abrir la conexion a la base de datos (Singleton)
 *   2. Crear los DAOs (capa de persistencia -> parte del MODELO)
 *   3. Crear los gestores (capa de servicio  -> parte del MODELO)
 *   4. Crear la Vista   (capa de PRESENTACION)
 *   5. Crear el Controlador e inyectarle todo lo anterior
 *   6. Iniciar el Controlador, que toma el control del programa
 */
public class Main {

    public static void main(String[] args) {

        Vista vista = new Vista();
        vista.mostrarBienvenida();

        // 1. Conexion a la base de datos (patron Singleton)
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

        // 2. Capa de persistencia (DAOs)
        CelularDAO  celularDAO  = new CelularDAO(conn);
        ClienteDAO  clienteDAO  = new ClienteDAO(conn);
        VentaDAO    ventaDAO    = new VentaDAO(conn);
        MarcaDAO    marcaDAO    = new MarcaDAO(conn);
        PrecioDAO   precioDAO   = new PrecioDAO(conn);
        EmpleadoDAO empleadoDAO = new EmpleadoDAO(conn);

        // 3. Capa de servicio (gestores de logica de negocio)
        GestorCelulares gestorCelulares = new GestorCelulares(celularDAO);
        GestorClientes  gestorClientes  = new GestorClientes(clienteDAO);
        GestorVentas    gestorVentas    = new GestorVentas(ventaDAO, clienteDAO, celularDAO);

        // 4 y 5. Crear el Controlador e inyectarle Vista + Modelo
        Controlador controlador = new Controlador(
            vista,
            gestorCelulares, gestorClientes, gestorVentas,
            marcaDAO, precioDAO, empleadoDAO,
            db
        );

        // 6. Ceder el control al Controlador
        controlador.iniciar();
    }
}