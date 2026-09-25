package org.example;

import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class Gestor {
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
    public void listarClientes(List<Cliente> clientes) {
        for (Cliente cliente : clientes) {
            System.out.println(cliente);
        }
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

}
