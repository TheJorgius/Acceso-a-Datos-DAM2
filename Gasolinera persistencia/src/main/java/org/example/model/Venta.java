package org.example.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Venta de tienda. El total se recalcula desde las lineas: no se almacena un
 * importe que pueda quedar desincronizado.
 */
public class Venta extends Entidad {
    private String numeroTicket;
    private final Estacion estacion;
    private final Empleado cajero;
    private final LocalDateTime fechaHora;
    private final List<LineaVenta> lineas = new ArrayList<>();
    private final List<PagoVenta> pagos = new ArrayList<>();
    private final List<Devolucion> devoluciones = new ArrayList<>();

    public Venta(String numeroTicket, Estacion estacion, Empleado cajero, LocalDateTime fechaHora) {
        this.numeroTicket = Validaciones.texto(numeroTicket, "numeroTicket");
        this.estacion = Validaciones.noNulo(estacion, "estacion");
        this.cajero = Validaciones.noNulo(cajero, "cajero");
        this.fechaHora = Validaciones.noNulo(fechaHora, "fechaHora");
        estacion.registrarVenta(this);
    }

    void registrarLinea(LineaVenta linea) {
        for (LineaVenta l : lineas) {
            if (l.getProducto() == linea.getProducto()) {
                throw new IllegalArgumentException("El producto ya aparece en otra linea de esta venta");
            }
        }
        lineas.add(linea);
    }

    void registrarPago(PagoVenta pago) {
        pagos.add(pago);
    }

    void registrarDevolucion(Devolucion devolucion) {
        devoluciones.add(devolucion);
    }

    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (LineaVenta linea : lineas) {
            total = total.add(linea.getImporte());
        }
        return total.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public BigDecimal getTotalPagado() {
        BigDecimal total = BigDecimal.ZERO;
        for (PagoVenta pago : pagos) {
            total = total.add(pago.getImporte());
        }
        return total.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public BigDecimal getPendiente() {
        return getTotal().subtract(getTotalPagado()).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public EstadoPago getEstadoPago() {
        BigDecimal pendiente = getPendiente();
        if (pendiente.compareTo(BigDecimal.ZERO) == 0) {
            return EstadoPago.PAGADA;
        }
        return getTotalPagado().compareTo(BigDecimal.ZERO) > 0
                ? EstadoPago.PARCIAL : EstadoPago.PENDIENTE;
    }

    public String getNumeroTicket() {
        return numeroTicket;
    }

    public Estacion getEstacion() {
        return estacion;
    }

    public Empleado getCajero() {
        return cajero;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public List<LineaVenta> getLineas() {
        return Collections.unmodifiableList(lineas);
    }

    public List<PagoVenta> getPagos() {
        return Collections.unmodifiableList(pagos);
    }

    public List<Devolucion> getDevoluciones() {
        return Collections.unmodifiableList(devoluciones);
    }
}
