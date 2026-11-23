package org.example.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Estacion de servicio. Ficha 1:1, varios surtidores, empleados, ventas e incidencias. */
public class Estacion extends Entidad {
    private String codigoEstacion;
    private String nombreComercial;
    private String direccion;
    private String telefono;
    private LocalDate fechaApertura;
    private FichaFacturacion ficha;
    private final List<Surtidor> surtidores = new ArrayList<>();
    private final List<Empleado> empleados = new ArrayList<>();
    private final List<Incidencia> incidencias = new ArrayList<>();
    private final List<Venta> ventas = new ArrayList<>();

    public Estacion(String codigoEstacion, String nombreComercial, String direccion,
                    String telefono, LocalDate fechaApertura) {
        this.codigoEstacion = Validaciones.texto(codigoEstacion, "codigoEstacion");
        this.nombreComercial = Validaciones.texto(nombreComercial, "nombreComercial");
        this.direccion = Validaciones.texto(direccion, "direccion");
        this.telefono = telefono;
        this.fechaApertura = Validaciones.noNulo(fechaApertura, "fechaApertura");
    }

    /** Vincula la ficha en los dos sentidos; una estacion solo puede tener una. */
    public void setFicha(FichaFacturacion ficha) {
        if (this.ficha != null && this.ficha != ficha) {
            throw new IllegalStateException("La estacion ya tiene una ficha de facturacion");
        }
        this.ficha = Validaciones.noNulo(ficha, "ficha");
        if (ficha.getEstacion() != this) {
            ficha.setEstacionSinSincronizar(this);
        }
    }

    void registrarSurtidor(Surtidor surtidor) {
        for (Surtidor s : surtidores) {
            if (s.getNumeroVisible() == surtidor.getNumeroVisible()) {
                throw new IllegalArgumentException(
                        "El numero visible " + surtidor.getNumeroVisible() + " ya existe en esta estacion");
            }
        }
        surtidores.add(surtidor);
    }

    void registrarEmpleado(Empleado empleado) {
        if (!empleados.contains(empleado)) {
            empleados.add(empleado);
        }
    }

    void registrarIncidencia(Incidencia incidencia) {
        incidencias.add(incidencia);
    }

    void registrarVenta(Venta venta) {
        ventas.add(venta);
    }

    public String getCodigoEstacion() {
        return codigoEstacion;
    }

    public String getNombreComercial() {
        return nombreComercial;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public LocalDate getFechaApertura() {
        return fechaApertura;
    }

    public FichaFacturacion getFicha() {
        return ficha;
    }

    public List<Surtidor> getSurtidores() {
        return Collections.unmodifiableList(surtidores);
    }

    public List<Empleado> getEmpleados() {
        return Collections.unmodifiableList(empleados);
    }

    public List<Incidencia> getIncidencias() {
        return Collections.unmodifiableList(incidencias);
    }

    public List<Venta> getVentas() {
        return Collections.unmodifiableList(ventas);
    }

    @Override
    public String toString() {
        return "Estacion{" + codigoEstacion + ", " + nombreComercial + '}';
    }
}
