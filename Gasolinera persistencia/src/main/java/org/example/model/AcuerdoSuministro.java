package org.example.model;

import java.math.BigDecimal;

/** Asociacion N:M proveedor-producto con referencia, precio de compra y plazo. */
public class AcuerdoSuministro extends Entidad {
    private final Proveedor proveedor;
    private final Producto producto;
    private String referenciaProveedor;
    private BigDecimal precioCompra;
    private int plazoEntregaDias;

    public AcuerdoSuministro(Proveedor proveedor, Producto producto,
                             String referenciaProveedor, BigDecimal precioCompra,
                             int plazoEntregaDias) {
        this.proveedor = Validaciones.noNulo(proveedor, "proveedor");
        this.producto = Validaciones.noNulo(producto, "producto");
        this.referenciaProveedor = Validaciones.texto(referenciaProveedor, "referenciaProveedor");
        this.precioCompra = Validaciones.positivo(precioCompra, "precioCompra");
        if (plazoEntregaDias < 0) {
            throw new IllegalArgumentException("plazoEntregaDias no puede ser negativo");
        }
        this.plazoEntregaDias = plazoEntregaDias;
        proveedor.registrarAcuerdo(this);
        producto.agregarAcuerdo(this);
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public Producto getProducto() {
        return producto;
    }

    public String getReferenciaProveedor() {
        return referenciaProveedor;
    }

    public BigDecimal getPrecioCompra() {
        return precioCompra;
    }

    public int getPlazoEntregaDias() {
        return plazoEntregaDias;
    }
}
