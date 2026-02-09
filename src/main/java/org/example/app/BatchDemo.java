package org.example.app;

import java.sql.Connection;
import java.util.List;

import org.example.db.Db;
import org.example.model.Libro;
import org.example.repository.jdbc.JdbcLibroRepository;

public class BatchDemo {
    public static void main(String[] args) {
        var repo = new JdbcLibroRepository();
        List<Libro> libros = List.of(
                new Libro("Don Quijote de la Mancha", "ESP-0201", 1605, true),
                new Libro("La Celestina", "ESP-0202", 1499, true),
                new Libro("Lazarillo de Tormes", "ESP-0203", 1554, true),
                new Libro("La Regenta", "ESP-0204", 1884, true),
                new Libro("Fortunata y Jacinta", "ESP-0205", 1887, true),
                new Libro("Los pazos de Ulloa", "ESP-0206", 1886, true),
                new Libro("Niebla", "ESP-0207", 1914, true),
                new Libro("El capitán Alatriste", "ESP-0217", 1996, true),
                new Libro("El hereje", "ESP-0218", 1998, true),
                new Libro("Soldados de Salamina", "ESP-0220", 2001, true),
                new Libro("Los girasoles ciegos", "ESP-0221", 2004, true),
                new Libro("Patria", "ESP-0222", 2016, true));
        try (Connection con = Db.getConnection()) {
            con.setAutoCommit(false);
            try {
                int[] res = repo.insertBatch(con, libros);// devuelve array con nº de filas afectadas
                con.commit();
                System.out.println("Batch OK. Inserts=" + res.length);
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (Exception e) {
            System.out.println("Error en batch: " + e.getMessage());
        }
    }
}
