package org.example.model;

import java.time.LocalDate;

/** Inscripcion de un cliente en una campana; pertenece a ambas entidades. */
public class InscripcionCampana extends Entidad {
    private final Campana campana;
    private final Cliente cliente;
    private final LocalDate fechaInscripcion;

    public InscripcionCampana(Campana campana, Cliente cliente, LocalDate fechaInscripcion) {
        this.campana = Validaciones.noNulo(campana, "campana");
        this.cliente = Validaciones.noNulo(cliente, "cliente");
        this.fechaInscripcion = Validaciones.noNulo(fechaInscripcion, "fechaInscripcion");
        campana.registrarInscripcion(this);
        cliente.registrarInscripcion(this);
    }

    public Campana getCampana() {
        return campana;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public LocalDate getFechaInscripcion() {
        return fechaInscripcion;
    }
}
