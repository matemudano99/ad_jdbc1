package org.example.repository.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet; // Ajustar según paquete real de Db
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.example.db.Db;
import org.example.model.Libro;
import org.example.repository.LibroRepository;

public class JdbcLibroRepository implements LibroRepository {

    private Libro mapRow(ResultSet rs) throws SQLException {
        return new Libro(
                rs.getInt("id"),
                rs.getString("titulo"),
                rs.getString("isbn"),
                (Integer) rs.getObject("anio"),
                rs.getBoolean("disponible"));
    }

    @Override
    public List<Libro> findAll() {
        String sql = "SELECT id, titulo, isbn, anio, disponible FROM libro ORDER BY id";
        try (Connection con = Db.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            List<Libro> libros = new ArrayList<>();
            while (rs.next()) {
                libros.add(mapRow(rs));
            }
            return libros;
        } catch (SQLException e) {
            throw new RuntimeException("Error leyendo libros", e);
        }
    }

    @Override
    public Optional<Libro> findById(int id) {
        String sql = "SELECT id, titulo, isbn, anio, disponible FROM libro WHERE id = ?";
        try (Connection con = Db.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return Optional.of(mapRow(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando libro id=" + id, e);
        }
    }

    @Override
    public int insert(Libro libro) {
        String sql = "INSERT INTO libro (titulo, isbn, anio, disponible) VALUES(?,?,?,?)";
        try (Connection con = Db.getConnection();
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, libro.getTitulo());
            ps.setString(2, libro.getIsbn());
            ps.setObject(3, libro.getAnio());
            ps.setBoolean(4, libro.isDisponible());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    libro.setId(id);
                    return id;
                }
                throw new SQLException("No se generó ID");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error insertando libro", e);
        }
    }

    @Override
    public boolean update(Libro libro) {
        String sql = "UPDATE libro SET titulo=?, isbn=?, anio=?, disponible=? WHERE id=?";
        try (Connection con = Db.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, libro.getTitulo());
            ps.setString(2, libro.getIsbn());
            ps.setObject(3, libro.getAnio());
            ps.setBoolean(4, libro.isDisponible());
            ps.setInt(5, libro.getId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando libro", e);
        }
    }

    @Override
    public boolean deleteById(int id) {
        String sql = "DELETE FROM libro WHERE id=?";
        try (Connection con = Db.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error borrando libro", e);
        }
    }

    public Libro findByIdOrNull(Connection con, int id) throws SQLException {
        String sql = "SELECT id, titulo, isbn, anio, disponible FROM libro WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    return null;
                return mapRow(rs); // reutiliza tu mapRow(ResultSet)
            }
        }
    }

    // Update usando Connection compartida
    public boolean update(Connection con, Libro libro) throws SQLException {
        String sql = "UPDATE libro SET titulo=?, isbn=?, anio=?, disponible=? WHERE id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, libro.getTitulo());
            ps.setString(2, libro.getIsbn());
            if (libro.getAnio() == null)
                ps.setObject(3, null);
            else
                ps.setInt(3, libro.getAnio());
            ps.setBoolean(4, libro.isDisponible());
            ps.setInt(5, libro.getId());
            return ps.executeUpdate() == 1;
        }
    }

    // ¿Está disponible el libro?
    public boolean isDisponible(Connection con, int libroId) throws SQLException {
        String sql = "SELECT disponible FROM libro WHERE id = ?";
        try (var ps = con.prepareStatement(sql)) {
            ps.setInt(1, libroId);
            try (var rs = ps.executeQuery()) {
                if (!rs.next())
                    return false;
                return rs.getBoolean("disponible");
            }
        }
    }

    // Actualizar disponibilidad
    public boolean updateDisponible(Connection con, int libroId, boolean disponible) throws SQLException {
        String sql = "UPDATE libro SET disponible = ? WHERE id = ?";
        try (var ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, disponible);
            ps.setInt(2, libroId);
            return ps.executeUpdate() == 1;
        }
    }

    public int[] insertBatch(Connection con, List<Libro> libros) throws SQLException {
    String sql = "INSERT INTO libro (titulo, isbn, anio, disponible) VALUES (?, ?, ?, ?)"; 
    try (PreparedStatement ps = con.prepareStatement(sql)) { 
        for (Libro l : libros) {
            ps.setString(1, l.getTitulo());
            ps.setString(2, l.getIsbn());
            
            // Manejo de nulos para el año
            if (l.getAnio() == null) {
                ps.setObject(3, null);
            } else {
                ps.setInt(3, l.getAnio());
            }
            
            ps.setBoolean(4, l.isDisponible()); 
            ps.addBatch(); 
        
        }
        return ps.executeBatch(); 
    
    }
}

}