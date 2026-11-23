package org.example.model;

/** Equipo de una estacion; el numero visible es unico dentro de la estacion. */
public class Surtidor extends Entidad {
    private String codigoEquipo;
    private Estacion estacion;
    private int numeroVisible;
    private String tipoCombustible;
    private EstadoServicioSurtidor estadoServicio;

    public Surtidor(String codigoEquipo, Estacion estacion, int numeroVisible,
                    String tipoCombustible, EstadoServicioSurtidor estadoServicio) {
        this.codigoEquipo = Validaciones.texto(codigoEquipo, "codigoEquipo");
        this.estacion = Validaciones.noNulo(estacion, "estacion");
        if (numeroVisible <= 0) {
            throw new IllegalArgumentException("numeroVisible debe ser mayor que 0");
        }
        this.numeroVisible = numeroVisible;
        this.tipoCombustible = Validaciones.texto(tipoCombustible, "tipoCombustible");
        this.estadoServicio = estadoServicio == null
                ? EstadoServicioSurtidor.OPERATIVO : estadoServicio;
        estacion.registrarSurtidor(this);
    }

    public String getCodigoEquipo() {
        return codigoEquipo;
    }

    public Estacion getEstacion() {
        return estacion;
    }

    public int getNumeroVisible() {
        return numeroVisible;
    }

    public String getTipoCombustible() {
        return tipoCombustible;
    }

    public EstadoServicioSurtidor getEstadoServicio() {
        return estadoServicio;
    }

    public void setEstadoServicio(EstadoServicioSurtidor estadoServicio) {
        this.estadoServicio = Validaciones.noNulo(estadoServicio, "estadoServicio");
    }
}
