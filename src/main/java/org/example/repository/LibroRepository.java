package org.example.repository;
import java.util.List;
import java.util.Optional;

import org.example.model.Libro;

public interface LibroRepository {
    List<Libro> findAll();
    Optional<Libro> findById(int id);
    int insert(Libro libro);
    boolean update(Libro libro);
    boolean deleteById(int id);
}