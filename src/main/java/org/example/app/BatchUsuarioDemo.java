package org.example.app;

import java.sql.Connection;
import java.util.List;

import org.example.db.Db;
import org.example.model.Usuario;
import org.example.repository.jdbc.JdbcUsuarioRepository;

public class BatchUsuarioDemo {
    public static void main(String[] args) {
        var repo = new JdbcUsuarioRepository();

        List<Usuario> usuarios = List.of(
                new Usuario("Ana García", "ana.garcia@email.com"),
                new Usuario("Luis Pérez", "luis.perez@email.com"),
                new Usuario("Marta Sánchez", "marta.sanchez@email.com"),
                new Usuario("Roberto Gómez", "roberto.gomez@email.com"),
                new Usuario("Elena Beltrán", "elena.beltran@email.com"));

        try (Connection con = Db.getConnection()) {
            con.setAutoCommit(false);

            try {
                int[] resultados = repo.insertBatchUsuarios(con, usuarios);
                con.commit();
                System.out.println("Batch de usuarios OK. Registros insertados: " + resultados.length);

            } catch (Exception e) {
                con.rollback();
                System.err.println("Error en el batch de usuarios. Se ha realizado rollback.");
                e.printStackTrace();
            } finally {
                con.setAutoCommit(true);
            }

        } catch (Exception e) {
            System.err.println("Error de conexión a la base de datos: " + e.getMessage());
        }
    }
}