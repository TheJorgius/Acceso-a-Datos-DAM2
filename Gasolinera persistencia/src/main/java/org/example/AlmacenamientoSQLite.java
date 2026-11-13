
package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class AlmacenamientoSQLite implements I_Almacenamiento {

    // Directorio del proyecto
    private Path directorio = Path.of("Datos");

    // Archivo de base de datos
    private Path baseDatos = Path.of("Gasolinera.sqlite");

    // Crear URL de conexion
    private String url = "jdbc:sqlite:" +
            baseDatos.toAbsolutePath();

    // Establecer conexion
    public Connection conectar() throws SQLException {

        return DriverManager.getConnection(url);
    }

    // Crear directorio y tablas
    @Override
    public void crearDirectorioYArchivos() throws IOException {

        try {

            // Crear directorio Datos
            Files.createDirectories(directorio);

            // Comprobar que existe la base de datos
            if (!Files.isRegularFile(baseDatos)) {

                System.out.println(
                        "No existe la base de datos: "
                                + baseDatos.toAbsolutePath()
                );

                return;
            }

            // Establecer conexion
            try (Connection conexion = conectar()) {

                // Activar claves foraneas
                try (Statement configuracion =
                             conexion.createStatement()) {

                    configuracion.execute(
                            "PRAGMA foreign_keys = ON"
                    );
                }

                System.out.println("Conexion SQLite abierta.");

                System.out.println(
                        "Conexion valida: " + conexion.isValid(2)
                );

                // Crear tablas
                try (Statement sentencia =
                             conexion.createStatement()) {

                    String sqlClientes = """
                        CREATE TABLE IF NOT EXISTS clientes (
                            id INTEGER PRIMARY KEY,
                            nombre TEXT NOT NULL,
                            telefono TEXT,
                            matricula TEXT
                        )
                        """;

                    sentencia.execute(sqlClientes);

                    String sqlPagos = """
                        CREATE TABLE IF NOT EXISTS pagos (
                            id INTEGER PRIMARY KEY,
                            id_cliente INTEGER,
                            fecha TEXT,
                            importe REAL,
                            litros REAL,
                            combustible TEXT,
                            FOREIGN KEY (id_cliente)
                            REFERENCES clientes(id)
                        )
                        """;

                    sentencia.execute(sqlPagos);

                }

                System.out.println("Tablas creadas correctamente.");

            }

        } catch (SQLException e) {

            throw new IOException(
                    "Error al conectar o crear las tablas", e
            );
        }
    }

    @Override
    public boolean comprobarRuta(Path archivo) throws IOException {

        return Files.exists(archivo);
    }

    @Override
    public List<Cliente> leerClientes() throws IOException {

        throw new IOException("Metodo pendiente de implementar");
    }

    @Override
    public List<Pagos> leerPagos() throws IOException {

        throw new IOException("Metodo pendiente de implementar");
    }

    @Override
    public void escribirClientes(List<Cliente> listaClientes)
            throws IOException {

        throw new IOException("Metodo pendiente de implementar");
    }

    @Override
    public void escribirPagos(List<Pagos> listaPagos)
            throws IOException {

        throw new IOException("Metodo pendiente de implementar");
    }
}
