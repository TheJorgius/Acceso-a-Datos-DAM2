import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter
public class Main {






    public static void mostrarMenu() {


            System.out.println("=== GESTIÓN DE GASOLINERA ===");
            System.out.println("1. Dar de alta un cliente");
            System.out.println("2. Listar clientes");
            System.out.println("3. Buscar clientes");
            System.out.println("4. Procesar un pago de repostaje");
            System.out.println("5. Consultar pagos");
            System.out.println("0. Salir");
            System.out.println("Opcion:");


    }

    public static void main(String[] args){
        int opcion;
        Scanner sc = new Scanner(System.in);
        Gestion G1= new Gestion();
        do {
            mostrarMenu();
            opcion = sc.nextInt();
            switch (opcion) {
                case 1:
                    G1.darDeAlta();
                    break;
                case 2:
                    G1.listarClientes();
                    break;
                case 3:
                    G1.buscarClientes();
                    break;
                case 4:
                    G1.procesarPago();
                    break;
                case 5:
                    G1.consultarPagos();
                    break;
                case 0:

                    break;
                default:
                    System.out.println("Esa no es una opcion valida selecciona una opcion valida");

                    break;

            }

        }while (opcion != 0);
    }
}