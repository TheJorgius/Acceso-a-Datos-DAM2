import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Ficheros {
    Path clientes = Path.of("Datos", "clientes.csv");
    Path pagos = Path.of("Datos", "pagos.csv");
    Path directorio = Path.of("Datos");

    public void crearDirectorioYArchivos() throws IOException {
        // 1. Creamos el directorio siempre. Si ya existe, no pasa nada.
        System.out.println("Asegurando la existencia del directorio: " + directorio);
        Files.createDirectories(directorio);

        // 2. Ahora que el directorio existe seguro, comprobamos y creamos los archivos
        if (!comprobarRuta(pagos)){
            System.out.println("Creando archivo pagos.csv");
            Files.createFile(pagos);
        }
        if (!comprobarRuta(clientes)){
            System.out.println("Creando archivo clientes.csv");
            Files.createFile(clientes);
        }
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
