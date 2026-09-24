package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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



}