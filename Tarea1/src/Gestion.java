import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;


public class Gestion {


    public static void listarClientes(Ficheros F1){

    }
    public  void buscarClientes(Ficheros F1){
        
    }
    public static Clientes darDeAlta(Ficheros F1){


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


    public static void procesarPago(Ficheros F1){
        Scanner sc = new Scanner(System.in);
        listarClientes(F1);
        System.out.println("Dime el id de cliente");
        int id=sc.nextInt();
        //comprobar que existe el cliente por id
        System.out.println("Dime fecha, importe, litros y combustible");
        //Asignar un identificador de pago, guardar la operación y mostrar una
        //confirmación con el identificador, el nombre del cliente y el importe
    }
    public static void consultarPagos(Ficheros F1){
        //Leeremos todos los tados en formato ID,Nombre Cliente, Fecha, Importe, Litros, Tipo_Combustible
    }

}
