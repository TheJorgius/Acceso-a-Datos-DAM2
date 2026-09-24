package org.example;

import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Ficheros F1= new Ficheros();

        try {
            System.out.println("Lista de clientes");
            List <Cliente> clientes = F1.leerClientes();
            System.out.println(clientes);
        }catch (IOException e){
            System.out.println(e);
        }


    }
}