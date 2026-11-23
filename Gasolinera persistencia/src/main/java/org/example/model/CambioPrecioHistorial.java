package org.example.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Representa un cambio real de precio; en BD lo genera el trigger de MySQL. */
public class CambioPrecioHistorial extends Entidad {
    private final Producto producto;
    private final BigDecimal precioAnterior;
    private final BigDecimal precioNuevo;
    private final LocalDateTime fechaCambio;

    public CambioPrecioHistorial(Producto producto, BigDecimal precioAnterior,
                                 BigDecimal precioNuevo, LocalDateTime fechaCambio) {
        this.producto = Validaciones.noNulo(producto, "producto");
        this.precioAnterior = Validaciones.positivo(precioAnterior, "precioAnterior");
        this.precioNuevo = Validaciones.positivo(precioNuevo, "precioNuevo");
        this.fechaCambio = Validaciones.noNulo(fechaCambio, "fechaCambio");
        if (precioAnterior.compareTo(precioNuevo) == 0) {
            throw new IllegalArgumentException("Guardar el mismo precio no es un cambio");
        }
    }

    public Producto getProducto() {
        return producto;
    }

    public BigDecimal getPrecioAnterior() {
        return precioAnterior;
    }

    public BigDecimal getPrecioNuevo() {
        return precioNuevo;
    }

    public LocalDateTime getFechaCambio() {
        return fechaCambio;
    }
}
