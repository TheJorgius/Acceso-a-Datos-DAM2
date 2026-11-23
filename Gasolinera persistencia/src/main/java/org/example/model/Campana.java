package org.example.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Campana promocional con un periodo de inscripcion acotado. */
public class Campana extends Entidad {
    private String codigoCampana;
    private String nombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private int limiteInscripciones;
    private final List<InscripcionCampana> inscripciones = new ArrayList<>();

    public Campana(String codigoCampana, String nombre, LocalDate fechaInicio,
                   LocalDate fechaFin, int limiteInscripciones) {
        this.codigoCampana = Validaciones.texto(codigoCampana, "codigoCampana");
        this.nombre = Validaciones.texto(nombre, "nombre");
        this.fechaInicio = Validaciones.noNulo(fechaInicio, "fechaInicio");
        this.fechaFin = Validaciones.noNulo(fechaFin, "fechaFin");
        if (fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("fechaFin no puede ser anterior a fechaInicio");
        }
        if (limiteInscripciones <= 0) {
            throw new IllegalArgumentException("limiteInscripciones debe ser mayor que 0");
        }
        this.limiteInscripciones = limiteInscripciones;
    }

    void registrarInscripcion(InscripcionCampana inscripcion) {
        if (!estaEnPeriodo(inscripcion.getFechaInscripcion())) {
            throw new IllegalArgumentException("La inscripcion queda fuera del periodo de la campana");
        }
        if (inscripciones.size() >= limiteInscripciones) {
            throw new IllegalStateException("La campana ha alcanzado su limite de inscripciones");
        }
        for (InscripcionCampana i : inscripciones) {
            if (i.getCliente() == inscripcion.getCliente()) {
                throw new IllegalArgumentException("El cliente ya esta inscrito en esta campana");
            }
        }
        inscripciones.add(inscripcion);
    }

    public boolean estaEnPeriodo(LocalDate fecha) {
        return fecha != null && !fecha.isBefore(fechaInicio) && !fecha.isAfter(fechaFin);
    }

    public String getCodigoCampana() {
        return codigoCampana;
    }

    public String getNombre() {
        return nombre;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public int getLimiteInscripciones() {
        return limiteInscripciones;
    }

    public List<InscripcionCampana> getInscripciones() {
        return Collections.unmodifiableList(inscripciones);
    }
}
