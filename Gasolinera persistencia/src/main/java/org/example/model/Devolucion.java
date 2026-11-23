package org.example.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Devolucion de una linea de venta, limitada a la cantidad comprada. */
public class Devolucion extends Entidad {
    private final LineaVenta lineaVenta;
    private final Empleado autoriza;
    private final BigDecimal cantidad;
    private final String motivo;
    private final LocalDateTime fechaHora;

    public Devolucion(LineaVenta lineaVenta, Empleado autoriza, BigDecimal cantidad,
                      String motivo, LocalDateTime fechaHora) {
        this.lineaVenta = Validaciones.noNulo(lineaVenta, "lineaVenta");
        this.autoriza = Validaciones.noNulo(autoriza, "autoriza");
        this.cantidad = lineaVenta.getProducto().validarCantidad(cantidad);
        if (this.cantidad.compareTo(lineaVenta.getCantidad()) > 0) {
            throw new IllegalArgumentException("No se puede devolver mas de lo vendido en la linea");
        }
        this.motivo = Validaciones.texto(motivo, "motivo");
        this.fechaHora = Validaciones.noNulo(fechaHora, "fechaHora");
        lineaVenta.getVenta().registrarDevolucion(this);
    }

    public BigDecimal getImporte() {
        return lineaVenta.getProducto().calcularImporte(cantidad);
    }

    public LineaVenta getLineaVenta() {
        return lineaVenta;
    }

    public Empleado getAutoriza() {
        return autoriza;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public String getMotivo() {
        return motivo;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }
}
