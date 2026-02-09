package org.example.repository.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.example.model.Usuario;

public class JdbcUsuarioRepository {
    public int[] insertBatchUsuarios(Connection con, List<Usuario> usuarios) throws SQLException {
        String sql = "INSERT INTO usuario (nombre, email) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (Usuario u : usuarios) {
                ps.setString(1, u.getNombre());
                ps.setString(2, u.getEmail());
                ps.addBatch();
            }
            return ps.executeBatch();
        }
    }
}