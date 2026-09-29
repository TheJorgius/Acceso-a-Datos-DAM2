package org.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Ficheros {
    Path clientes = Path.of("Datos", "clientes.csv");
    Path pagos = Path.of("Datos", "pagos.csv");
    Path directorio = Path.of("Datos");

    public void crearDirectorioYArchivos() throws IOException {
        System.out.println("Creand directorio: " + directorio);
        Files.createDirectories(directorio);
        Files.createFile(pagos);
        Files.createFile(clientes);
    }
    public boolean comprobarRuta(Path archivo) {
        if (Files.exists(archivo)) {
            System.out.println("El archivo " + archivo + " existe");
            return true;
        } else {
            System.out.println("No, el archivo " + archivo + " no existe");
            return false;
        }
    }
    public List<Cliente> leerClientes() throws IOException {
        List<String> lineas = Files.readAllLines(clientes, StandardCharsets.UTF_8);
        List<Cliente> clientes = new ArrayList<>();

        for (String linea:lineas){
            String[] datos = linea.split(";");
            int id = Integer.parseInt(datos[0]);
            String nombre = datos[1];
            String tlfn = datos[2];
            String matricula = datos[3];
            Cliente cliente = new Cliente(id, nombre, tlfn, matricula);
            clientes.add(cliente);
        }

        return clientes;
    }

    public List<Pagos> leerPagos() throws IOException {
        List<String> lineas = Files.readAllLines(pagos, StandardCharsets.UTF_8);
        List<Pagos> pagosList = new ArrayList<>();

        for (String linea : lineas) {
            String[] datos = linea.split(";");

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

    public void escribirClientes(List<Cliente> listaClientes) throws IOException {
        List<String> lineas = new ArrayList<>();

        for (Cliente cliente : listaClientes) {
            lineas.add(cliente.getId() + ";" +
                    cliente.getNombre() + ";" +
                    cliente.getTlfn() + ";" +
                    cliente.getMatricula());
        }

        Files.write(clientes, lineas, StandardCharsets.UTF_8);
    }
    public void escribirPagos(List<Pagos> listaPagos) throws IOException {
        List<String> lineas = new ArrayList<>();

        for (Pagos pago : listaPagos) {
            lineas.add(pago.getId() + ";" +
                    pago.getId_cliente() + ";" +
                    pago.getFecha() + ";" +
                    pago.getImporte() + ";" +
                    pago.getLitros() + ";" +
                    pago.getCombustible());
        }

        Files.write(pagos, lineas, StandardCharsets.UTF_8);
    }





}