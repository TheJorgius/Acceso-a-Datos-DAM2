package org.example.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Producto envasado: se cobra por unidades completas. */
public class ProductoEnvasado extends Producto {
    private String formatoEnvase;

    public ProductoEnvasado(Integer id, String referenciaComercial, String descripcion,
                            BigDecimal precioVenta, String formatoEnvase) {
        super(id, referenciaComercial, descripcion, precioVenta,
                EstadoComercializacion.ACTIVO);
        this.formatoEnvase = Validaciones.texto(formatoEnvase, "formatoEnvase");
    }

    @Override
    public ModalidadProducto getModalidad() {
        return ModalidadProducto.ENVASADO;
    }

    @Override
    public BigDecimal validarCantidad(BigDecimal cantidad) {
        Validaciones.positivo(cantidad, "cantidad");
        try {
            return cantidad.setScale(0, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(
                    "Un producto envasado solo admite unidades enteras: " + cantidad, e);
        }
    }

    @Override
    public BigDecimal calcularImporte(BigDecimal cantidad) {
        return importeRedondeado(cantidad);
    }

    public String getFormatoEnvase() {
        return formatoEnvase;
    }
}
