package org.example;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface I_Ficheros {
    public void crearDirectorioYArchivos()throws IOException;
    public boolean comprobarRuta(Path archivo)throws IOException;
    public List<Cliente> leerClientes()throws IOException;
    public List<Pagos> leerPagos()throws IOException;
    public void escribirClientes(List<Cliente> listaClientes)throws IOException;
    public void escribirPagos(List<Pagos> listaPagos)throws IOException;
    public void CsvAJson()throws IOException;
}