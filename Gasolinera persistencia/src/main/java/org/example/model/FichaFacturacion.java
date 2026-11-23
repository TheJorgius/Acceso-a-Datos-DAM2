package org.example.model;

/** Configuracion de facturacion de una estacion (relacion 1:1 estricta). */
public class FichaFacturacion extends Entidad {
    private String codigoFicha;
    private Estacion estacion;
    private String serieTickets;
    private int siguienteNumero;
    private String textoPie;

    public FichaFacturacion(String codigoFicha, Estacion estacion, String serieTickets,
                            int siguienteNumero, String textoPie) {
        this.codigoFicha = Validaciones.texto(codigoFicha, "codigoFicha");
        this.estacion = Validaciones.noNulo(estacion, "estacion");
        this.serieTickets = Validaciones.texto(serieTickets, "serieTickets");
        if (siguienteNumero <= 0) {
            throw new IllegalArgumentException("siguienteNumero debe ser mayor que 0");
        }
        this.siguienteNumero = siguienteNumero;
        this.textoPie = textoPie;
        estacion.setFicha(this);
    }

    void setEstacionSinSincronizar(Estacion estacion) {
        this.estacion = estacion;
    }

    public String getCodigoFicha() {
        return codigoFicha;
    }

    public Estacion getEstacion() {
        return estacion;
    }

    public String getSerieTickets() {
        return serieTickets;
    }

    public int getSiguienteNumero() {
        return siguienteNumero;
    }

    public String getTextoPie() {
        return textoPie;
    }

    public String siguienteTicket() {
        return serieTickets + "-" + siguienteNumero++;
    }
}
