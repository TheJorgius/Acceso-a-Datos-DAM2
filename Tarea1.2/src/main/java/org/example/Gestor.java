package org.example;

import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class Gestor {

    public void listarClientes(List<Cliente> clientes) {
        for (Cliente c : clientes) {
            System.out.println(c);
        }
    }

    public void consultarPagos(List<Pagos> pagos) {
        for (Pagos p : pagos) {
            System.out.println(p);
        }
    }


    public List<Cliente> darDeAlta(List<Cliente> clienteList){
        Scanner sc = new Scanner(System.in);
        System.out.println("Dime ID de cliente");
        int id= sc.nextInt();
        System.out.println("Dime Nombre de cliente");
        String nombre= sc.next();
        System.out.println("Dime Telefono de cliente");
        String tlfn=sc.next();
        System.out.println("Dime Matricula de cliente");
        String matricula=sc.next();
        Cliente c1 = new Cliente(id,nombre,tlfn,matricula);
        clienteList.add(c1);

        return clienteList;

    }

    public List<Pagos> procesarPago(List<Pagos> pagosList) {
        Scanner sc = new Scanner(System.in);

        System.out.println("Dime ID de pago");
        int id = sc.nextInt();

        System.out.println("Dime Id de cliente");
        int id_cliente = sc.nextInt();

        System.out.println("Dime Fecha de pago");
        String fecha = sc.next();

        System.out.println("Dime importe de pago");
        int importe = sc.nextInt();

        System.out.println("Dime litros de pago");
        int litros = sc.nextInt();

        System.out.println("Dime combustible de pago");
        String combustible = sc.next();

        Pagos p1 = new Pagos(id, id_cliente, fecha, importe, litros, combustible);
        pagosList.add(p1);

        return pagosList;
    }


    public void buscarClientes(List<Cliente> clientes) {
        Scanner sc = new Scanner(System.in);
        boolean encontrado = false;

        System.out.println("Pon algo que recuerdes del usuario:");
        String texto = sc.nextLine();

        while (texto=="") {
            System.out.println("El texto no puede estar vacío");
            texto = sc.nextLine();
        }

        texto = texto.toLowerCase();
        for (Cliente c : clientes) {
            if (c.getNombre().toLowerCase().contains(texto)
                    || c.getTlfn().toLowerCase().contains(texto)
                    || c.getMatricula().toLowerCase().contains(texto)) {

                System.out.println(c);
                encontrado = true;
            }
        }

        if (!encontrado) {
            System.out.println("No se han encontrado clientes");
        }
    }



}
