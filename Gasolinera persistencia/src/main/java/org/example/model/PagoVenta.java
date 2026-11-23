package org.example.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Cobro asociado a una venta; la suma de cobros no puede superar el total. */
public class PagoVenta extends Entidad {
    private final Venta venta;
    private final BigDecimal importe;
    private final MedioPago medioPago;
    private final LocalDateTime fechaHora;

    public PagoVenta(Venta venta, BigDecimal importe, MedioPago medioPago, LocalDateTime fechaHora) {
        this.venta = Validaciones.noNulo(venta, "venta");
        this.importe = Validaciones.positivo(importe, "importe");
        this.medioPago = Validaciones.noNulo(medioPago, "medioPago");
        this.fechaHora = Validaciones.noNulo(fechaHora, "fechaHora");
        BigDecimal resto = venta.getPendiente();
        if (this.importe.compareTo(resto) > 0) {
            throw new IllegalArgumentException(
                    "El cobro (" + this.importe + ") supera el pendiente (" + resto + ')');
        }
        venta.registrarPago(this);
    }

    public Venta getVenta() {
        return venta;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public MedioPago getMedioPago() {
        return medioPago;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }
}
