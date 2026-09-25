package org.example;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void mostrarMenu() {
        System.out.println("=== GESTIÓN DE GASOLINERA ===");
        System.out.println("1. Dar de alta un cliente");
        System.out.println("2. Listar clientes");
        System.out.println("3. Buscar clientes");
        System.out.println("4. Procesar un pago de repostaje");
        System.out.println("5. Consultar pagos");
        System.out.println("6. Crear directorio y archivos(Primera vez o reparar)");
        System.out.println("0. Salir");
        System.out.println("Opcion:");




    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int opcion;
        Ficheros F1= new Ficheros();
        Gestor G1= new Gestor();
        List<Cliente> clientesList=null;
        List<Pagos> pagosList=null;
        try {
            clientesList= F1.leerClientes();
            pagosList= F1.leerPagos();
        }catch (IOException e) {
            System.out.println(e);;
        }


        do {
            mostrarMenu();
            opcion = sc.nextInt();
            switch (opcion) {
                case 1:
                    clientesList = G1.darDeAlta(clientesList);
                    try {
                        F1.escribirClientes(clientesList);
                    }catch (IOException e){
                        System.out.println(e);
                    }

                    break;
                case 2:
                    G1.listarClientes(clientesList);
                    break;
                case 3:
                    G1.buscarClientes(clientesList);
                    break;
                case 4:
                    G1.procesarPago(pagosList);
                    try {
                        F1.escribirPagos(pagosList);
                    }catch (IOException e){
                        System.out.println(e);
                    }
                    break;
                case 5:
                    G1.consultarPagos(pagosList);
                    break;

                case 6:
                    try {
                        F1.crearDirectorioYArchivos();
                    }catch (IOException e){
                        System.out.println(e);
                    }
                case 0:

                    break;
                default:
                    System.out.println("Esa no es una opcion valida selecciona una opcion valida");
                    try {
                        F1.crearDirectorioYArchivos();
                    }catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;

            }

        }while (opcion != 0);



    }

}