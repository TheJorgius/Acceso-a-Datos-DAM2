package org.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MigraCSVToJson {
    public static List<Cliente> clientesCsvAJson() throws IOException {
        I_Almacenamiento LeectorCSV= new AlmacenamientoCSV();
        List<Cliente> listaClientes = LeectorCSV.leerClientes();
        return listaClientes;
    }
    public static List<Pagos> pagosCsvAJson() throws IOException {
        I_Almacenamiento LeectorCSV= new AlmacenamientoCSV();
        List<Pagos> listaPagos = LeectorCSV.leerPagos();
        return listaPagos;
    }
}
