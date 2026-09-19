package edu.umg.calidad.testcontainers.demo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
public class UsuarioRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("test_db")
                    .withUsername("test")
                    .withPassword("test");

    static UsuarioRepository repo;

    @BeforeAll
    static void configurarBaseDeDatos() throws SQLException {
        repo = new UsuarioRepository(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword()
        );

        try (Connection conn = DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
             Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE usuarios (" +
                    "id SERIAL PRIMARY KEY, " +
                    "nombre VARCHAR(100), " +
                    "email VARCHAR(100))");
        }
    }

    @Test
    void debeGuardarYRecuperarUsuario() throws SQLException {
        repo.guardar(new Usuario("Wilson", "wilson@umg.edu"));

        Usuario encontrado = repo.buscarPorEmail("wilson@umg.edu");

        assertNotNull(encontrado);
        assertEquals("Wilson", encontrado.getNombre());
        assertEquals("wilson@umg.edu", encontrado.getEmail());
    }
}