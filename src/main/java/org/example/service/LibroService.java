package org.example.service;

import java.util.List;

import org.example.model.Libro;
import org.example.repository.LibroRepository;

public class LibroService {
    private final LibroRepository repo;

    public LibroService(LibroRepository repo) {
        this.repo = repo;
    }

    public List<Libro> listar() {
        return repo.findAll();
    }

    public Libro verDetalle(int id) {
        return repo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("No existe libro con id=" + id));
    }

    public int alta(Libro libro) {
        if (libro.getTitulo() == null || libro.getTitulo().isBlank())
            throw new IllegalArgumentException("Título obligatorio");
        if (libro.getIsbn() == null || libro.getIsbn().isBlank())
            throw new IllegalArgumentException("ISBN obligatorio");
        return repo.insert(libro);
    }

    public void marcarNoDisponible(int id) {
        Libro l = verDetalle(id);
        l.setDisponible(false);
        if (!repo.update(l)) throw new IllegalStateException("No se pudo actualizar");
    }

    public void borrar(int id) {
        if (!repo.deleteById(id)) throw new IllegalArgumentException("No existe id=" + id);
    }
}