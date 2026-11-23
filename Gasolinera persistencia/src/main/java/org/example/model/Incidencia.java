package org.example.model;

import java.time.LocalDateTime;

/** Incidencia registrada en una estacion, con seguimiento de estado. */
public class Incidencia extends Entidad {
    private String codigoIncidencia;
    private final Estacion estacion;
    private final Empleado empleado;
    private final Surtidor surtidor;
    private String descripcion;
    private LocalDateTime fechaDeteccion;
    private EstadoIncidencia estado;
    private LocalDateTime fechaResolucion;

    public Incidencia(String codigoIncidencia, Estacion estacion, Empleado empleado,
                      Surtidor surtidor, String descripcion, LocalDateTime fechaDeteccion) {
        this.codigoIncidencia = Validaciones.texto(codigoIncidencia, "codigoIncidencia");
        this.estacion = Validaciones.noNulo(estacion, "estacion");
        this.empleado = Validaciones.noNulo(empleado, "empleado");
        if (surtidor != null && surtidor.getEstacion() != estacion) {
            throw new IllegalArgumentException("El surtidor pertenece a otra estacion");
        }
        this.surtidor = surtidor;
        this.descripcion = Validaciones.texto(descripcion, "descripcion");
        this.fechaDeteccion = Validaciones.noNulo(fechaDeteccion, "fechaDeteccion");
        this.estado = EstadoIncidencia.ABIERTA;
        estacion.registrarIncidencia(this);
        empleado.registrarIncidencia(this);
    }

    public void resolver(LocalDateTime fechaResolucion) {
        LocalDateTime fecha = Validaciones.noNulo(fechaResolucion, "fechaResolucion");
        if (fecha.isBefore(fechaDeteccion)) {
            throw new IllegalArgumentException("fechaResolucion no puede ser anterior a fechaDeteccion");
        }
        this.estado = EstadoIncidencia.RESUELTA;
        this.fechaResolucion = fecha;
    }

    public String getCodigoIncidencia() {
        return codigoIncidencia;
    }

    public Estacion getEstacion() {
        return estacion;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public Surtidor getSurtidor() {
        return surtidor;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public LocalDateTime getFechaDeteccion() {
        return fechaDeteccion;
    }

    public EstadoIncidencia getEstado() {
        return estado;
    }

    public LocalDateTime getFechaResolucion() {
        return fechaResolucion;
    }
}
