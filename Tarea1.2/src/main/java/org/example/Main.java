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
        System.out.println("0. Salir");
        System.out.println("Opcion:");




    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int opcion;
        Ficheros F1= new Ficheros();
        Gestor G1= new Gestor();
        do {
            mostrarMenu();
            opcion = sc.nextInt();
            switch (opcion) {
                case 1:
                    Cliente altaCliente = G1.darDeAlta();

                    break;
                case 2:
                    G1.listarClientes(F1);
                    break;
                case 3:
                    G1.buscarClientes(F1);
                    break;
                case 4:
                    G1.procesarPago(F1);
                    break;
                case 5:
                    G1.consultarPagos(F1);
                    break;
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