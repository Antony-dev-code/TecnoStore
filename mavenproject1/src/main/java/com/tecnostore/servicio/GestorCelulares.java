package com.tecnostore.servicio;

import com.tecnostore.modelo.*;
import com.tecnostore.patron.FactoryCelular;
import com.tecnostore.persistencia.CelularDAO;
import com.tecnostore.util.ConsolaUtils;
import com.tecnostore.util.Validador;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * GestorCelulares - logica de negocio para el inventario de celulares.
 *
 * Esta clase NO tiene codigo SQL ni llamadas directas a la consola
 * dentro de la logica de negocio. Delega la BD a CelularDAO.
 *
 * Aplica el Principio de Responsabilidad Unica (SRP):
 * una sola razon para cambiar = las reglas de negocio de celulares.
 */
public class GestorCelulares {

    private final CelularDAO celularDAO;

    public GestorCelulares(CelularDAO celularDAO) {
        this.celularDAO = celularDAO;
    }

    /**
     * Registra un celular nuevo en el inventario.
     * Usa FactoryCelular para construir el objeto con validaciones incluidas.
     */
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

    /**
     * Busca un celular por su id. Retorna null si no existe.
     */
    public Celular buscarPorId(int id) {
        try {
            return celularDAO.buscarPorId(id);
        } catch (SQLException e) {
            ConsolaUtils.imprimirError("Error al acceder a la base de datos: " + e.getMessage());
            return null;
        }
    }

    /**
     * Retorna la lista completa de celulares. Retorna lista vacia si hay error.
     */
    public List<Celular> listarTodos() {
        try {
            return celularDAO.listarTodos();
        } catch (SQLException e) {
            ConsolaUtils.imprimirError("Error al acceder a la base de datos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Actualiza los datos de un celular existente.
     * Lanza IllegalArgumentException si el id no existe.
     */
    public void actualizarCelular(int id, String modelo, Marca marca,
                                   SistemaOperativo so, CategoriaGama gama,
                                   Precio precio, int stock) {
        try {
            Celular existente = celularDAO.buscarPorId(id);
            if (existente == null) {
                throw new IllegalArgumentException("Celular no encontrado con id: " + id);
            }
            // Validar datos nuevos antes de actualizar
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

    /**
     * Elimina un celular por su id.
     * Lanza IllegalArgumentException si el id no existe.
     */
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