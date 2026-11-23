package org.example.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Producto del catalogo. La modalidad es estable y la validacion de cantidad
 * se delega a las subclases: el calculo del importe es comun y usa siempre
 * BigDecimal con HALF_UP a 2 decimales.
 */
public abstract class Producto extends Entidad {
    private String referenciaComercial;
    private String descripcion;
    private BigDecimal precioVenta;
    private EstadoComercializacion estadoComercializacion;
    private final List<AcuerdoSuministro> acuerdos = new ArrayList<>();

    protected Producto(Integer id, String referenciaComercial, String descripcion,
                       BigDecimal precioVenta, EstadoComercializacion estadoComercializacion) {
        super(id);
        this.referenciaComercial = Validaciones.texto(referenciaComercial, "referenciaComercial");
        this.descripcion = Validaciones.texto(descripcion, "descripcion");
        this.precioVenta = Validaciones.positivo(precioVenta, "precioVenta");
        this.estadoComercializacion = estadoComercializacion == null
                ? EstadoComercializacion.ACTIVO : estadoComercializacion;
    }

    public abstract ModalidadProducto getModalidad();

    /** Valida la cantidad segun la modalidad y la devuelve normalizada. */
    public abstract BigDecimal validarCantidad(BigDecimal cantidad);

    /** Calcula el importe usando siempre el precio vigente del producto. */
    public abstract BigDecimal calcularImporte(BigDecimal cantidad);

    protected final BigDecimal importeRedondeado(BigDecimal cantidad) {
        return validarCantidad(cantidad)
                .multiply(precioVenta)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public void setPrecioVenta(BigDecimal precioVenta) {
        this.precioVenta = Validaciones.positivo(precioVenta, "precioVenta");
    }

    /**
     * Crea en memoria el dato que el trigger de MySQL insertara en la BD.
     * No escribe nada: solo representa el hecho para el modelo de dominio.
     */
    public CambioPrecioHistorial crearRegistroCambio(BigDecimal precioAnterior) {
        return new CambioPrecioHistorial(this, precioAnterior, precioVenta, LocalDateTime.now());
    }

    void agregarAcuerdo(AcuerdoSuministro acuerdo) {
        for (AcuerdoSuministro a : acuerdos) {
            if (a.getProveedor() == acuerdo.getProveedor()) {
                throw new IllegalArgumentException("Ese proveedor ya tiene acuerdo con este producto");
            }
        }
        acuerdos.add(acuerdo);
    }

    public String getReferenciaComercial() {
        return referenciaComercial;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    public EstadoComercializacion getEstadoComercializacion() {
        return estadoComercializacion;
    }

    public List<AcuerdoSuministro> getAcuerdos() {
        return Collections.unmodifiableList(acuerdos);
    }

    @Override
    public String toString() {
        return referenciaComercial + " (" + getModalidad() + ", " + precioVenta + ')';
    }
}
