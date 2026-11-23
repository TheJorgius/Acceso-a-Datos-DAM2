package org.example.model;

import java.math.BigDecimal;

/** Producto a granel: decimal positivo y multiplo exacto de la precision de pesada. */
public class ProductoGranel extends Producto {
    private final BigDecimal precisionPesada;

    public ProductoGranel(Integer id, String referenciaComercial, String descripcion,
                          BigDecimal precioVenta, BigDecimal precisionPesada) {
        super(id, referenciaComercial, descripcion, precioVenta,
                EstadoComercializacion.ACTIVO);
        this.precisionPesada = Validaciones.positivo(precisionPesada, "precisionPesada");
    }

    @Override
    public ModalidadProducto getModalidad() {
        return ModalidadProducto.GRANEL;
    }

    @Override
    public BigDecimal validarCantidad(BigDecimal cantidad) {
        Validaciones.positivo(cantidad, "cantidad");
        if (cantidad.remainder(precisionPesada).compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalArgumentException(
                    "Cantidad " + cantidad + " no es multiplo de la precision "
                            + precisionPesada + " kg");
        }
        return cantidad;
    }

    @Override
    public BigDecimal calcularImporte(BigDecimal cantidad) {
        return importeRedondeado(cantidad);
    }

    public BigDecimal getPrecisionPesada() {
        return precisionPesada;
    }
}
