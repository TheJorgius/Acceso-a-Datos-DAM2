package org.example.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Representacion en el modelo de un pago de repostaje heredado del sistema
 * anterior. No sustituye a org.example.Pagos: permite relacionar esos datos
 * historicos con el cliente y la estacion del nuevo modelo.
 */
public class PagoCombustibleLegado extends Entidad {
    private final org.example.Cliente clienteLegado;
    private final Estacion estacion;
    private final LocalDate fecha;
    private final BigDecimal importe;
    private final int litros;
    private final String tipoCombustible;

    public PagoCombustibleLegado(org.example.Cliente clienteLegado, Estacion estacion,
                                 LocalDate fecha, BigDecimal importe, int litros,
                                 String tipoCombustible) {
        this.clienteLegado = Validaciones.noNulo(clienteLegado, "clienteLegado");
        this.estacion = Validaciones.noNulo(estacion, "estacion");
        this.fecha = Validaciones.noNulo(fecha, "fecha");
        this.importe = Validaciones.positivo(importe, "importe");
        if (litros <= 0) {
            throw new IllegalArgumentException("litros debe ser mayor que 0");
        }
        this.litros = litros;
        this.tipoCombustible = Validaciones.texto(tipoCombustible, "tipoCombustible");
    }

    public org.example.Cliente getClienteLegado() {
        return clienteLegado;
    }

    public Estacion getEstacion() {
        return estacion;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public int getLitros() {
        return litros;
    }

    public String getTipoCombustible() {
        return tipoCombustible;
    }
}
