package org.example.model;

import java.time.LocalDate;

/** Tarjeta que puede registrarse antes de entregarse (sin titular). */
public class TarjetaFidelizacion extends Entidad {
    private String numeroTarjeta;
    private LocalDate fechaEmision;
    private Cliente cliente;
    private LocalDate fechaAsignacion;

    public TarjetaFidelizacion(String numeroTarjeta, LocalDate fechaEmision) {
        this.numeroTarjeta = Validaciones.texto(numeroTarjeta, "numeroTarjeta");
        this.fechaEmision = Validaciones.noNulo(fechaEmision, "fechaEmision");
    }

    public void asignar(Cliente cliente, LocalDate fechaAsignacion) {
        Cliente c = Validaciones.noNulo(cliente, "cliente");
        LocalDate fecha = Validaciones.noNulo(fechaAsignacion, "fechaAsignacion");
        if (fecha.isBefore(fechaEmision)) {
            throw new IllegalArgumentException("fechaAsignacion no puede ser anterior a fechaEmision");
        }
        if (this.cliente != null && this.cliente != c) {
            throw new IllegalStateException("La tarjeta ya tiene titular");
        }
        this.cliente = c;
        this.fechaAsignacion = fecha;
        c.setTarjeta(this);
    }

    public String getNumeroTarjeta() {
        return numeroTarjeta;
    }

    public LocalDate getFechaEmision() {
        return fechaEmision;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public LocalDate getFechaAsignacion() {
        return fechaAsignacion;
    }
}
