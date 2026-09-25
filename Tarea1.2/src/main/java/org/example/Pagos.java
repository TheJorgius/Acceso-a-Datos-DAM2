package org.example;

import java.util.Date;

public class Pagos {
    private int id;
    int id_cliente;
    String fecha;
    int importe;
    int litros;
    String combustible;

    public Pagos(int id, int idCliente, String fecha, int importe, int litros, String combustible) {
        this.id = id;
        this.id_cliente = idCliente;
        this.fecha = fecha;
        this.importe = importe;
        this.litros = litros;
        this.combustible = combustible;
    }

    //getters

    public int getId() {
        return id;
    }

    public int getId_cliente() {
        return id_cliente;
    }

    public String getFecha() {
        return fecha;
    }

    public int getImporte() {
        return importe;
    }

    public int getLitros() {
        return litros;
    }

    public String getCombustible() {
        return combustible;
    }
}