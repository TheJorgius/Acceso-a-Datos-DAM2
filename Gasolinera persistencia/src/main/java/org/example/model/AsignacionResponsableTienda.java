package org.example.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Responsable de tienda con presupuesto autorizado de compras. */
public class AsignacionResponsableTienda extends AsignacionFuncion {
    private final BigDecimal presupuestoCompras;

    public AsignacionResponsableTienda(Empleado empleado, LocalDate fechaInicio, LocalDate fechaFin,
                                       BigDecimal presupuestoCompras) {
        super(empleado, TipoFuncion.RESPONSABLE_TIENDA, fechaInicio, fechaFin);
        this.presupuestoCompras = Validaciones.positivo(presupuestoCompras, "presupuestoCompras");
    }

    public BigDecimal getPresupuestoCompras() {
        return presupuestoCompras;
    }

    @Override
    public String getDetalle() {
        return "Presupuesto compras: " + presupuestoCompras;
    }
}
