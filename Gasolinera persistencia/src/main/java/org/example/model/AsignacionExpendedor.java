package org.example.model;

import java.time.LocalDate;

/** Expendedor con acreditacion para manipular combustibles. */
public class AsignacionExpendedor extends AsignacionFuncion {
    private final String numeroAcreditacion;
    private final LocalDate caducidadAcreditacion;

    public AsignacionExpendedor(Empleado empleado, LocalDate fechaInicio, LocalDate fechaFin,
                                String numeroAcreditacion, LocalDate caducidadAcreditacion) {
        super(empleado, TipoFuncion.EXPENDEDOR, fechaInicio, fechaFin);
        this.numeroAcreditacion = Validaciones.texto(numeroAcreditacion, "numeroAcreditacion");
        this.caducidadAcreditacion = Validaciones.noNulo(caducidadAcreditacion, "caducidadAcreditacion");
    }

    public String getNumeroAcreditacion() {
        return numeroAcreditacion;
    }

    public LocalDate getCaducidadAcreditacion() {
        return caducidadAcreditacion;
    }

    @Override
    public String getDetalle() {
        return "Acreditacion " + numeroAcreditacion + " (caduca " + caducidadAcreditacion + ')';
    }
}
