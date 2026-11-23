package org.example.model;

import java.math.BigDecimal;

/** Linea de venta: valida la cantidad segun el producto y fija el importe. */
public class LineaVenta extends Entidad {
    private final Venta venta;
    private final Producto producto;
    private final BigDecimal cantidad;
    private final BigDecimal precioUnitario;
    private final BigDecimal importe;

    public LineaVenta(Venta venta, Producto producto, BigDecimal cantidad) {
        this.venta = Validaciones.noNulo(venta, "venta");
        this.producto = Validaciones.noNulo(producto, "producto");
        this.cantidad = producto.validarCantidad(cantidad);
        this.precioUnitario = producto.getPrecioVenta();
        this.importe = producto.calcularImporte(cantidad);
        venta.registrarLinea(this);
    }

    public Venta getVenta() {
        return venta;
    }

    public Producto getProducto() {
        return producto;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public BigDecimal getImporte() {
        return importe;
    }
}
