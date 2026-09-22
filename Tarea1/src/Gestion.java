import java.util.Scanner;

public class Gestion {

    public static void listarClientes(){

    }
    public  void buscarClientes(){

    }
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

}
