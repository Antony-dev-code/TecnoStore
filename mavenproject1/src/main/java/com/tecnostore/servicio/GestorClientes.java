package com.tecnostore.servicio;

import com.tecnostore.modelo.Cliente;
import com.tecnostore.persistencia.ClienteDAO;
import com.tecnostore.util.ConsolaUtils;
import com.tecnostore.util.Validador;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * GestorClientes - logica de negocio para el registro y consulta de clientes.
 *
 * Aplica el Principio de Responsabilidad Unica (SRP):
 * solo maneja las reglas de negocio de clientes.
 * El acceso a la BD lo delega a ClienteDAO.
 */
public class GestorClientes {

    private final ClienteDAO clienteDAO;

    public GestorClientes(ClienteDAO clienteDAO) {
        this.clienteDAO = clienteDAO;
    }

    /**
     * Registra un cliente nuevo despues de validar todos sus datos.
     *
     * Validaciones:
     *   - Ninguno de los campos puede estar vacio
     *   - Formato de correo valido
     *   - Telefono de 7 a 15 digitos
     *   - La identificacion no puede estar duplicada
     */
    public void registrarCliente(String nombre, String identificacion,
                                  String correo, String telefono) {
        // Validar campos obligatorios
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
            // Verificar que la identificacion no este duplicada
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

    /**
     * Busca un cliente por su numero de identificacion.
     * Retorna null si no existe.
     */
    public Cliente buscarPorIdentificacion(String identificacion) {
        try {
            return clienteDAO.buscarPorIdentificacion(identificacion);
        } catch (SQLException e) {
            ConsolaUtils.imprimirError("Error al acceder a la base de datos: " + e.getMessage());
            return null;
        }
    }

    /**
     * Retorna la lista completa de clientes registrados.
     */
    public List<Cliente> listarTodos() {
        try {
            return clienteDAO.listarTodos();
        } catch (SQLException e) {
            ConsolaUtils.imprimirError("Error al acceder a la base de datos: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}