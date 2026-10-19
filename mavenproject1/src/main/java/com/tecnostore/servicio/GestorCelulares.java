package com.tecnostore.servicio;

import com.tecnostore.modelo.*;
import com.tecnostore.patron.FactoryCelular;
import com.tecnostore.persistencia.CelularDAO;
import com.tecnostore.util.ConsolaUtils;
import com.tecnostore.util.Validador;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Maneja las operaciones del inventario de celulares.
public class GestorCelulares {

    private final CelularDAO celularDAO;

    public GestorCelulares(CelularDAO celularDAO) {
        this.celularDAO = celularDAO;
    }

    // Agrega un celular nuevo al inventario.
    public void registrarCelular(String modelo, Marca marca,
                                  SistemaOperativo so, CategoriaGama gama,
                                  Precio precio, int stock) {
        try {
            Celular celular = FactoryCelular.crear(modelo, marca, so, gama, precio, stock);
            celularDAO.insertar(celular);
            ConsolaUtils.imprimirExito("Celular registrado correctamente. ID asignado: " + celular.getId());
        } catch (IllegalArgumentException e) {
            ConsolaUtils.imprimirError(e.getMessage());
        } catch (SQLException e) {
            ConsolaUtils.imprimirError("Error al acceder a la base de datos: " + e.getMessage());
        }
    }

    // Busca un celular por su id.
    public Celular buscarPorId(int id) {
        try {
            return celularDAO.buscarPorId(id);
        } catch (SQLException e) {
            ConsolaUtils.imprimirError("Error al acceder a la base de datos: " + e.getMessage());
            return null;
        }
    }

    // Devuelve todos los celulares.
    public List<Celular> listarTodos() {
        try {
            return celularDAO.listarTodos();
        } catch (SQLException e) {
            ConsolaUtils.imprimirError("Error al acceder a la base de datos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Cambia los datos de un celular que ya existe.
    public void actualizarCelular(int id, String modelo, Marca marca,
                                   SistemaOperativo so, CategoriaGama gama,
                                   Precio precio, int stock) {
        try {
            Celular existente = celularDAO.buscarPorId(id);
            if (existente == null) {
                throw new IllegalArgumentException("Celular no encontrado con id: " + id);
            }
            Celular actualizado = FactoryCelular.crear(modelo, marca, so, gama, precio, stock);
            actualizado.setId(id);
            celularDAO.actualizar(actualizado);
            ConsolaUtils.imprimirExito("Celular actualizado correctamente.");
        } catch (IllegalArgumentException e) {
            ConsolaUtils.imprimirError(e.getMessage());
        } catch (SQLException e) {
            ConsolaUtils.imprimirError("Error al acceder a la base de datos: " + e.getMessage());
        }
    }

    // Borra un celular por su id.
    public void eliminarCelular(int id) {
        try {
            Celular existente = celularDAO.buscarPorId(id);
            if (existente == null) {
                throw new IllegalArgumentException("Celular no encontrado con id: " + id);
            }
            celularDAO.eliminar(id);
            ConsolaUtils.imprimirExito("Celular eliminado correctamente.");
        } catch (IllegalArgumentException e) {
            ConsolaUtils.imprimirError(e.getMessage());
        } catch (SQLException e) {
            ConsolaUtils.imprimirError("Error al acceder a la base de datos: " + e.getMessage());
        }
    }
}