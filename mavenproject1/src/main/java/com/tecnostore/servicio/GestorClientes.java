package com.tecnostore.servicio;

import com.tecnostore.modelo.Cliente;
import com.tecnostore.persistencia.ClienteDAO;
import com.tecnostore.util.ConsolaUtils;
import com.tecnostore.util.Validador;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Maneja el registro y la consulta de clientes.
public class GestorClientes {

    private final ClienteDAO clienteDAO;

    public GestorClientes(ClienteDAO clienteDAO) {
        this.clienteDAO = clienteDAO;
    }

    // Registra un cliente nuevo revisando antes que los datos esten bien.
    public void registrarCliente(String nombre, String identificacion,
                                  String correo, String telefono) {
        if (!Validador.esTextoValido(nombre, 100)) {
            ConsolaUtils.imprimirError("El nombre no puede estar vacio (maximo 100 caracteres).");
            return;
        }
        if (!Validador.esTextoValido(identificacion, 20)) {
            ConsolaUtils.imprimirError("La identificacion no puede estar vacia (maximo 20 caracteres).");
            return;
        }
        if (!Validador.esCorreoValido(correo)) {
            ConsolaUtils.imprimirError("El correo no tiene un formato valido. Ejemplo: usuario@dominio.com");
            return;
        }
        if (!Validador.esTelefonoValido(telefono)) {
            ConsolaUtils.imprimirError("El telefono debe tener entre 7 y 15 digitos numericos.");
            return;
        }

        try {
            if (clienteDAO.existeIdentificacion(identificacion)) {
                ConsolaUtils.imprimirError("Ya existe un cliente con esa identificacion.");
                return;
            }
            Cliente cliente = new Cliente(nombre, identificacion, telefono, correo);
            clienteDAO.insertar(cliente);
            ConsolaUtils.imprimirExito("Cliente registrado correctamente. ID asignado: " + cliente.getId());
        } catch (SQLException e) {
            ConsolaUtils.imprimirError("Error al acceder a la base de datos: " + e.getMessage());
        }
    }

    // Busca un cliente por su identificacion.
    public Cliente buscarPorIdentificacion(String identificacion) {
        try {
            return clienteDAO.buscarPorIdentificacion(identificacion);
        } catch (SQLException e) {
            ConsolaUtils.imprimirError("Error al acceder a la base de datos: " + e.getMessage());
            return null;
        }
    }

    // Devuelve todos los clientes.
    public List<Cliente> listarTodos() {
        try {
            return clienteDAO.listarTodos();
        } catch (SQLException e) {
            ConsolaUtils.imprimirError("Error al acceder a la base de datos: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}