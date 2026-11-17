
package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class AlmacenamientoSQLite implements I_Almacenamiento {

    private final Path directorio = Path.of("Datos");

    private final Path baseDatos = Path.of("Gasolinera.sqlite");

    private final String url = "jdbc:sqlite:" + baseDatos.toAbsolutePath();

    public Connection conectar() throws SQLException {
        Connection conexion = DriverManager.getConnection(url);
        try (Statement configuracion = conexion.createStatement()) {
            configuracion.execute("PRAGMA foreign_keys = ON");
            return conexion;
        } catch (SQLException e) {
            conexion.close();
            throw e;
        }
    }

    @Override
    public void crearDirectorioYArchivos() throws IOException {
        try {
            Files.createDirectories(directorio);

            try (Connection conexion = conectar()) {
                try (Statement sentencia = conexion.createStatement()) {
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
                            id_cliente INTEGER NOT NULL,
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
            }
        } catch (SQLException e) {
            throw new IOException(
                    "Error al conectar o crear las tablas SQLite", e
            );
        }
    }

    @Override
    public boolean comprobarRuta(Path archivo) throws IOException {

        return Files.exists(archivo);
    }

    @Override
    public List<Cliente> leerClientes() throws IOException {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT id, nombre, telefono, matricula FROM clientes ORDER BY id";

        try (Connection conexion = conectar();
             Statement sentencia = conexion.createStatement();
             ResultSet resultado = sentencia.executeQuery(sql)) {
            while (resultado.next()) {
                clientes.add(new Cliente(
                        resultado.getInt("id"),
                        resultado.getString("nombre"),
                        resultado.getString("telefono"),
                        resultado.getString("matricula")
                ));
            }
            return clientes;
        } catch (SQLException e) {
            throw new IOException("Error al leer clientes de SQLite", e);
        }
    }

    @Override
    public List<Pagos> leerPagos() throws IOException {
        List<Pagos> pagos = new ArrayList<>();
        String sql = "SELECT id, id_cliente, fecha, importe, litros, combustible FROM pagos ORDER BY id";

        try (Connection conexion = conectar();
             Statement sentencia = conexion.createStatement();
             ResultSet resultado = sentencia.executeQuery(sql)) {
            while (resultado.next()) {
                pagos.add(new Pagos(
                        resultado.getInt("id"),
                        resultado.getInt("id_cliente"),
                        resultado.getString("fecha"),
                        resultado.getInt("importe"),
                        resultado.getInt("litros"),
                        resultado.getString("combustible")
                ));
            }
            return pagos;
        } catch (SQLException e) {
            throw new IOException("Error al leer pagos de SQLite", e);
        }
    }

    @Override
    public void escribirClientes(List<Cliente> listaClientes)
            throws IOException {
        String sql = """
                INSERT INTO clientes (id, nombre, telefono, matricula)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    nombre = excluded.nombre,
                    telefono = excluded.telefono,
                    matricula = excluded.matricula
                """;

        try (Connection conexion = conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            for (Cliente cliente : listaClientes) {
                sentencia.setInt(1, cliente.getId());
                sentencia.setString(2, cliente.getNombre());
                sentencia.setString(3, cliente.getTlfn());
                sentencia.setString(4, cliente.getMatricula());
                sentencia.addBatch();
            }
            sentencia.executeBatch();
        } catch (SQLException e) {
            throw new IOException("Error al guardar clientes en SQLite", e);
        }
    }

    @Override
    public void escribirPagos(List<Pagos> listaPagos)
            throws IOException {
        String sql = """
                INSERT INTO pagos (id, id_cliente, fecha, importe, litros, combustible)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    id_cliente = excluded.id_cliente,
                    fecha = excluded.fecha,
                    importe = excluded.importe,
                    litros = excluded.litros,
                    combustible = excluded.combustible
                """;

        try (Connection conexion = conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            for (Pagos pago : listaPagos) {
                sentencia.setInt(1, pago.getId());
                sentencia.setInt(2, pago.getId_cliente());
                sentencia.setString(3, pago.getFecha());
                sentencia.setInt(4, pago.getImporte());
                sentencia.setInt(5, pago.getLitros());
                sentencia.setString(6, pago.getCombustible());
                sentencia.addBatch();
            }
            sentencia.executeBatch();
        } catch (SQLException e) {
            throw new IOException("Error al guardar pagos en SQLite", e);
        }
    }

    public void guardarDatos(List<Cliente> listaClientes, List<Pagos> listaPagos)
            throws IOException {
        String borrarPagos = "DELETE FROM pagos";
        String borrarClientes = "DELETE FROM clientes";

        try (Connection conexion = conectar()) {
            conexion.setAutoCommit(false);
            try (Statement sentencia = conexion.createStatement()) {
                sentencia.executeUpdate(borrarPagos);
                sentencia.executeUpdate(borrarClientes);
                insertarClientes(conexion, listaClientes);
                insertarPagos(conexion, listaPagos);
                conexion.commit();
            } catch (SQLException e) {
                try {
                    conexion.rollback();
                } catch (SQLException errorRollback) {
                    e.addSuppressed(errorRollback);
                }
                throw e;
            }
        } catch (SQLException e) {
            throw new IOException("Error al guardar los datos en SQLite", e);
        }
    }

    private void insertarClientes(Connection conexion, List<Cliente> clientes)
            throws SQLException {
        String sql = "INSERT INTO clientes (id, nombre, telefono, matricula) VALUES (?, ?, ?, ?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            for (Cliente cliente : clientes) {
                sentencia.setInt(1, cliente.getId());
                sentencia.setString(2, cliente.getNombre());
                sentencia.setString(3, cliente.getTlfn());
                sentencia.setString(4, cliente.getMatricula());
                sentencia.addBatch();
            }
            sentencia.executeBatch();
        }
    }

    private void insertarPagos(Connection conexion, List<Pagos> pagos)
            throws SQLException {
        String sql = "INSERT INTO pagos (id, id_cliente, fecha, importe, litros, combustible) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            for (Pagos pago : pagos) {
                sentencia.setInt(1, pago.getId());
                sentencia.setInt(2, pago.getId_cliente());
                sentencia.setString(3, pago.getFecha());
                sentencia.setInt(4, pago.getImporte());
                sentencia.setInt(5, pago.getLitros());
                sentencia.setString(6, pago.getCombustible());
                sentencia.addBatch();
            }
            sentencia.executeBatch();
        }
    }
}
