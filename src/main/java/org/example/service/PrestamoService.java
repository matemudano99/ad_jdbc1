package org.example.service;

import java.sql.Connection;
import java.time.LocalDate;

import org.example.db.Db;
import org.example.model.Prestamo;
import org.example.repository.jdbc.JdbcLibroRepository;
import org.example.repository.jdbc.JdbcPrestamoRepository;

public class PrestamoService {
    private final JdbcLibroRepository libroRepo;
    private final JdbcPrestamoRepository prestamoRepo;

    public PrestamoService(JdbcLibroRepository libroRepo, JdbcPrestamoRepository prestamoRepo) {
        this.libroRepo = libroRepo;
        this.prestamoRepo = prestamoRepo;
    }

    public Prestamo prestarLibro(long usuarioId, int libroId, LocalDate inicio,
            LocalDate fin) {
        try (Connection con = Db.getConnection()) {
            con.setAutoCommit(false);
            try {
                if (!libroRepo.isDisponible(con, libroId)) {
                    throw new IllegalStateException("El libro no está disponible (o no existe). id=" + libroId);
                }
                Prestamo p = new Prestamo(usuarioId, libroId, inicio, fin);
                Prestamo guardado = prestamoRepo.insert(con, p);
                boolean ok = libroRepo.updateDisponible(con, libroId, false);
                if (!ok)
                    throw new IllegalStateException("No se pudo marcar como no disponible id=" + libroId);
                con.commit();
                return guardado;
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al prestar (rollback realizado): " +
                    e.getMessage(), e);
        }
    }

    public long devolverLibro(int libroId) {
        try (Connection con = Db.getConnection()) {
            con.setAutoCommit(false);
            try {
                long prestamoId = prestamoRepo.marcarDevuelto(con, libroId);
                boolean ok = libroRepo.updateDisponible(con, libroId, false);
                if (!ok)
                    throw new IllegalStateException("No se pudo marcar disponible id=" + libroId);
                con.commit();
                return prestamoId;
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al devolver (rollback realizado): " +
                    e.getMessage(), e);
        }
    }
}