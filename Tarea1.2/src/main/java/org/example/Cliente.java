package org.example;

public class Cliente {
    private int id;
    String nombre;
    String tlfn;
    String matricula;

    public Cliente(int id, String nombre, String telefono, String matricula) {
        this.id = id;
        this.nombre = nombre;
        this.tlfn = telefono;
        this.matricula = matricula;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTlfn() {
        return tlfn;
    }

    public String getMatricula() {
        return matricula;
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", tlfn='" + tlfn + '\'' +
                ", matricula='" + matricula + '\'' +
                '}';
    }
}
