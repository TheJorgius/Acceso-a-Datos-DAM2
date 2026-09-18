import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    public static Clientes darDeAlta(){
        Scanner sc = new Scanner(System.in);
        System.out.println("Escribe los datos");
        System.out.println("Nombre: ");
        String nombre= sc.next();
        System.out.println("Telefono: ");
        String telefono= sc.next();
        System.out.println("Matricula: ");
        String matricula= sc.next();
        Clientes cliente= new Clientes(nombre,telefono,matricula);


        return cliente;
    }
    public static void listarClientes(){

    }
    public static void buscarClientes(){

    }
    public static void procesarPago(){
        Scanner sc = new Scanner(System.in);
        listarClientes();
        System.out.println("Dime el id de cliente");
        int id=sc.nextInt();
        //comprobar que existe el cliente por id
        System.out.println("Dime fecha, importe, litros y combustible");
        //Asignar un identificador de pago, guardar la operación y mostrar una
        //confirmación con el identificador, el nombre del cliente y el importe
    }
    public static void consultarPagos(){
        //Leeremos todos los tados en formato ID,Nombre Cliente, Fecha, Importe, Litros, Tipo_Combustible
    }




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

        do {
            mostrarMenu();
            opcion = sc.nextInt();
            switch (opcion) {
                case 1:
                    darDeAlta();
                    break;
                case 2:
                    listarClientes();
                    break;
                case 3:
                    buscarClientes();
                    break;
                case 4:
                    procesarPago();
                    break;
                case 5:
                    consultarPagos();
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