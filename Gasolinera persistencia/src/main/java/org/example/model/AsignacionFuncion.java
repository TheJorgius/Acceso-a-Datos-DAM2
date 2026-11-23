package org.example.model;

import java.time.LocalDate;

/**
 * Una funcion es un periodo con fechas y condiciones propias. Se conserva el
 * historial: cerrar una funcion y recuperarla despues crea otra asignacion.
 */
public abstract class AsignacionFuncion extends Entidad {
    private final Empleado empleado;
    private final TipoFuncion tipoFuncion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    protected AsignacionFuncion(Empleado empleado, TipoFuncion tipoFuncion,
                                LocalDate fechaInicio, LocalDate fechaFin) {
        this.empleado = Validaciones.noNulo(empleado, "empleado");
        this.tipoFuncion = Validaciones.noNulo(tipoFuncion, "tipoFuncion");
        this.fechaInicio = Validaciones.noNulo(fechaInicio, "fechaInicio");
        if (fechaFin != null && fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("fechaFin no puede ser anterior a fechaInicio");
        }
        this.fechaFin = fechaFin;
        empleado.agregarAsignacion(this);
    }

    public boolean estaActivaEn(LocalDate fecha) {
        if (fecha == null) {
            return false;
        }
        return !fecha.isBefore(fechaInicio) && (fechaFin == null || !fecha.isAfter(fechaFin));
    }

    public void cerrar(LocalDate fechaFin) {
        LocalDate fin = Validaciones.noNulo(fechaFin, "fechaFin");
        if (fin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("fechaFin no puede ser anterior a fechaInicio");
        }
        this.fechaFin = fin;
    }

    /** Devuelve las condiciones especificas de la funcion (polimorfico). */
    public abstract String getDetalle();

    public Empleado getEmpleado() {
        return empleado;
    }

    public TipoFuncion getTipoFuncion() {
        return tipoFuncion;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    @Override
    public String toString() {
        return tipoFuncion + "{" + fechaInicio + (fechaFin == null ? ", activa" : ", fin " + fechaFin) + '}';
    }
}
