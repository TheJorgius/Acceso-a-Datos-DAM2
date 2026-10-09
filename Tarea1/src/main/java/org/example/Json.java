package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Json implements I_Ficheros{
    Path clientes = Path.of("Datos", "clientes.json");
    Path pagos = Path.of("Datos", "pagos.json");
    Path directorio = Path.of("Datos");

    @Override
    public void crearDirectorioYArchivos() throws IOException {
        System.out.println("Creand directorio: " + directorio);
        Files.createDirectories(directorio);
        Files.createFile(pagos);
        Files.createFile(clientes);
    }

    @Override
    public boolean comprobarRuta(Path archivo) throws IOException {
        if (Files.exists(archivo)) {
            System.out.println("El archivo " + archivo + " existe");
            return true;
        } else {
            System.out.println("No, el archivo " + archivo + " no existe");
            return false;
        }
    }

    @Override
    public List<Cliente> leerClientes() throws IOException {
        List<Cliente> clientesLeidos = new ArrayList<>();

        try (BufferedReader lector = Files.newBufferedReader(clientes)) {
            String datos;

            while ((datos = lector.readLine()) != null) {

                if (!datos.contains("\"id\"")) {
                    continue;
                }
                datos = datos.substring(
                        datos.indexOf("{") + 1,
                        datos.indexOf("}")
                );
                String[] campos = datos.split(",");

                int id = Integer.parseInt(
                        campos[0].substring(
                                campos[0].indexOf(":") + 1
                        ).trim()
                );
                String nombre = campos[1].substring(
                        campos[1].indexOf(":") + 1
                ).trim().replace("\"", "");
                String telefono = campos[2].substring(
                        campos[2].indexOf(":") + 1
                ).trim().replace("\"", "");
                String matricula = campos[3].substring(
                        campos[3].indexOf(":") + 1
                ).trim().replace("\"", "");
                Cliente cliente = new Cliente(
                        id, nombre, telefono, matricula
                );
                clientesLeidos.add(cliente);
            }
        }

        return clientesLeidos;
    }

    @Override
    public List<Pagos> leerPagos() throws IOException {
        List<String> lineas = Files.readAllLines(pagos, StandardCharsets.UTF_8);
        List<Pagos> pagosList = new ArrayList<>();

        for (String linea : lineas) {
            String[] datos = linea.split(",");

            int id = Integer.parseInt(datos[0]);
            int id_cliente = Integer.parseInt(datos[1]);
            String fecha = datos[2];
            int importe = Integer.parseInt(datos[3]);
            int litros = Integer.parseInt(datos[4]);
            String combustible = datos[5];

            Pagos pago = new Pagos(id, id_cliente, fecha, importe, litros, combustible);
            pagosList.add(pago);
        }

        return pagosList;
    }

    @Override
    public void escribirClientes(List<Cliente> listaClientes) throws IOException {
        List<String> lineas = new ArrayList<>();

        for (Cliente cliente : listaClientes) {
            lineas.add("{\"id\": " +cliente.getId() + "," +
                    "\"nombre\": "+cliente.getNombre() + "," +
                    "\"telefono\": "+cliente.getTlfn() + "," +
                    "\"matricula\": "+cliente.getMatricula()+"},");
        }

        Files.write(clientes, lineas, StandardCharsets.UTF_8);
    }

    @Override
    public void escribirPagos(List<Pagos> listaPagos) throws IOException {
        List<String> lineas = new ArrayList<>();

        for (Pagos pago : listaPagos) {
            lineas.add("{\"id\": " +pago.getId() + "," +
                    "\"id_cliente\": " +pago.getId_cliente() + "," +
                    "\"fecha\": " +pago.getFecha() + "," +
                    "\"importe\": " +pago.getImporte() + "," +
                    "\"Litros\": " +pago.getLitros() + "," +
                    "\"tipo combustible\": " +pago.getCombustible()+"},");
        }

        Files.write(pagos, lineas, StandardCharsets.UTF_8);
    }

    @Override
    public void CsvAJson() throws IOException {

    }
}
