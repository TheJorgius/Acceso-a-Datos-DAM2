package org.example.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Cajero con limite autorizado para devoluciones. */
public class AsignacionCajero extends AsignacionFuncion {
    private final BigDecimal limiteDevoluciones;

    public AsignacionCajero(Empleado empleado, LocalDate fechaInicio, LocalDate fechaFin,
                            BigDecimal limiteDevoluciones) {
        super(empleado, TipoFuncion.CAJERO, fechaInicio, fechaFin);
        this.limiteDevoluciones = Validaciones.positivo(limiteDevoluciones, "limiteDevoluciones");
    }

    public BigDecimal getLimiteDevoluciones() {
        return limiteDevoluciones;
    }

    @Override
    public String getDetalle() {
        return "Limite devoluciones: " + limiteDevoluciones;
    }
}
