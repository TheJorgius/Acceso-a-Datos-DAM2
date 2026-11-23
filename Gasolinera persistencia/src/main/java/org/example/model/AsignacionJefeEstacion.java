package org.example.model;

import java.time.LocalDate;

/** Jefe de estacion con nivel de autorizacion para operaciones excepcionales. */
public class AsignacionJefeEstacion extends AsignacionFuncion {
    private final String nivelAutorizacion;

    public AsignacionJefeEstacion(Empleado empleado, LocalDate fechaInicio, LocalDate fechaFin,
                                  String nivelAutorizacion) {
        super(empleado, TipoFuncion.JEFE_ESTACION, fechaInicio, fechaFin);
        this.nivelAutorizacion = Validaciones.texto(nivelAutorizacion, "nivelAutorizacion");
    }

    public String getNivelAutorizacion() {
        return nivelAutorizacion;
    }

    @Override
    public String getDetalle() {
        return "Nivel autorizacion: " + nivelAutorizacion;
    }
}
