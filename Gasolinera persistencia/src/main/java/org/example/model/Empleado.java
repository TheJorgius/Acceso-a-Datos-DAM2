package org.example.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Empleado con adscripcion a una estacion, supervisor actual y funciones
 * modeladas por composicion: varias simultaneas, historial y ampliaciones.
 */
public class Empleado extends Entidad {
    private String numeroInterno;
    private String documentoIdentidad;
    private String nombre;
    private String telefono;
    private String email;
    private LocalDate fechaIncorporacion;
    private Estacion estacion;
    private Empleado supervisor;
    private final List<AsignacionFuncion> asignaciones = new ArrayList<>();
    private final List<Incidencia> incidencias = new ArrayList<>();

    public Empleado(String numeroInterno, String documentoIdentidad, String nombre,
                    String telefono, String email, LocalDate fechaIncorporacion, Estacion estacion) {
        this.numeroInterno = Validaciones.texto(numeroInterno, "numeroInterno");
        this.documentoIdentidad = Validaciones.texto(documentoIdentidad, "documentoIdentidad");
        this.nombre = Validaciones.texto(nombre, "nombre");
        this.telefono = telefono;
        this.email = email;
        this.fechaIncorporacion = Validaciones.noNulo(fechaIncorporacion, "fechaIncorporacion");
        this.estacion = Validaciones.noNulo(estacion, "estacion");
        estacion.registrarEmpleado(this);
    }

    public void setSupervisor(Empleado supervisor) {
        if (supervisor == this) {
            throw new IllegalArgumentException("Un empleado no puede supervisarse a si mismo");
        }
        this.supervisor = supervisor;
    }

    public void agregarAsignacion(AsignacionFuncion asignacion) {
        AsignacionFuncion a = Validaciones.noNulo(asignacion, "asignacion");
        if (a.getEmpleado() != this) {
            throw new IllegalArgumentException("La asignacion pertenece a otro empleado");
        }
        asignaciones.add(a);
    }

    void registrarIncidencia(Incidencia incidencia) {
        incidencias.add(incidencia);
    }

    /** Funciones vigentes en una fecha: pueden ser varias y de tipos distintos. */
    public List<AsignacionFuncion> getFuncionesActivasEn(LocalDate fecha) {
        List<AsignacionFuncion> activas = new ArrayList<>();
        for (AsignacionFuncion a : asignaciones) {
            if (a.estaActivaEn(fecha)) {
                activas.add(a);
            }
        }
        return activas;
    }

    public String getNumeroInterno() {
        return numeroInterno;
    }

    public String getDocumentoIdentidad() {
        return documentoIdentidad;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getFechaIncorporacion() {
        return fechaIncorporacion;
    }

    public Estacion getEstacion() {
        return estacion;
    }

    public Empleado getSupervisor() {
        return supervisor;
    }

    public List<AsignacionFuncion> getAsignaciones() {
        return Collections.unmodifiableList(asignaciones);
    }

    public List<Incidencia> getIncidencias() {
        return Collections.unmodifiableList(incidencias);
    }

    @Override
    public String toString() {
        return "Empleado{" + numeroInterno + ", " + nombre + '}';
    }
}
