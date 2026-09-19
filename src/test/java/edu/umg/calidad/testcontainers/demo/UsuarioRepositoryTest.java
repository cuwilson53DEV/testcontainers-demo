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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
                    "email VARCHAR(100) UNIQUE)");
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
    
    @Test
    void debeDevolverNullSiElUsuarioNoExiste() throws SQLException {
        Usuario resultado = repo.buscarPorEmail("noexiste@umg.edu");

        assertNull(resultado);
    }

    @Test
    void debeRechazarEmailDuplicado() throws SQLException {
        repo.guardar(new Usuario("Ana", "ana@umg.edu"));

        assertThrows(SQLException.class, () -> {
            repo.guardar(new Usuario("Ana Duplicada", "ana@umg.edu"));
        });
    }
}
