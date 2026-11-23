package org.example;

import org.example.model.AcuerdoSuministro;
import org.example.model.AsignacionCajero;
import org.example.model.AsignacionExpendedor;
import org.example.model.AsignacionJefeEstacion;
import org.example.model.Campana;
import org.example.model.CambioPrecioHistorial;
import org.example.model.Cliente;
import org.example.model.Devolucion;
import org.example.model.Empleado;
import org.example.model.Estacion;
import org.example.model.EstadoServicioSurtidor;
import org.example.model.FichaFacturacion;
import org.example.model.Incidencia;
import org.example.model.InscripcionCampana;
import org.example.model.LineaVenta;
import org.example.model.MedioPago;
import org.example.model.PagoCombustibleLegado;
import org.example.model.PagoVenta;
import org.example.model.ProductoEnvasado;
import org.example.model.ProductoGranel;
import org.example.model.Proveedor;
import org.example.model.Surtidor;
import org.example.model.TarjetaFidelizacion;
import org.example.model.Venta;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Demostracion ejecutable de la Fase 1: modelo de dominio orientado a objetos
 * sin JDBC, convivencia con CSV/JSON y validacion polimorfica de cantidades.
 */
public class DemoModelo {

    public static void main(String[] args) throws IOException {
        System.out.println("=== 1. MODELO DE DOMINIO (sin JDBC) ===");
        demostrarModelo();

        System.out.println("\n=== 2. CSV / JSON DE LA PRACTICA ANTERIOR (sandbox temporal) ===");
        verificarPersistencia();

        System.out.println("\nDemo completada: ninguna clase del modelo abrio conexiones ni ejecuto SQL.");
    }

    private static void demostrarModelo() {
        LocalDate hoy = LocalDate.of(2026, 11, 23);

        Estacion estacion = new Estacion("EST-01", "Gasolinera Centro",
                "Calle Mayor 1", "910000000", LocalDate.of(2020, 1, 15));
        FichaFacturacion ficha = new FichaFacturacion("FIC-01", estacion, "A", 1, "Gracias");
        Surtidor surtidor = new Surtidor("EQ-1", estacion, 1, "95",
                EstadoServicioSurtidor.OPERATIVO);
        System.out.println("Estacion: " + estacion + " | ficha " + ficha.getCodigoFicha()
                + " | surtidores " + estacion.getSurtidores().size());

        Empleado jefa = new Empleado("E-01", "12345678Z", "Ana", "600000001",
                "ana@gasolinera.es", LocalDate.of(2021, 3, 1), estacion);
        new AsignacionJefeEstacion(jefa, LocalDate.of(2021, 3, 1), null, "ALTA");

        Empleado cajero = new Empleado("E-02", "87654321X", "Luis", "600000002",
                "luis@gasolinera.es", LocalDate.of(2023, 5, 10), estacion);
        new AsignacionCajero(cajero, LocalDate.of(2024, 1, 1), null,
                new BigDecimal("150.00"));
        new AsignacionExpendedor(cajero, hoy.minusMonths(2), hoy.plusMonths(6),
                "AC-77", hoy.plusYears(2));
        cajero.setSupervisor(jefa);
        System.out.println("Supervisor de " + cajero.getNombre() + ": " + cajero.getSupervisor().getNombre());
        System.out.println("Funciones activas de " + cajero.getNombre() + " el " + hoy + ":");
        cajero.getFuncionesActivasEn(hoy).forEach(a ->
                System.out.println("  - " + a.getTipoFuncion() + ": " + a.getDetalle()));

        Cliente cliente = new Cliente("C-001", "Jorge", "6552",
                "jorge@example.com", "Calle Sol 4");
        TarjetaFidelizacion tarjeta = new TarjetaFidelizacion("TF-001", LocalDate.of(2026, 1, 10));
        tarjeta.asignar(cliente, hoy);
        Campana campana = new Campana("CAM-01", "Cafe de verano",
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 9, 30), 100);
        new InscripcionCampana(campana, cliente, LocalDate.of(2026, 7, 1));
        System.out.println(cliente + " -> tarjeta " + tarjeta.getNumeroTarjeta()
                + " | inscrito en " + campana.getNombre());

        Proveedor proveedor = new Proveedor("P-01", "B12345678", "Suministros SL",
                "910111213", "pedidos@suministros.es", "Poligono 2");
        ProductoEnvasado aceite = new ProductoEnvasado(null, "REF-ENV-1", "Aceite 1 L",
                new BigDecimal("12.995"), "Botella 1 L");
        ProductoGranel cafe = new ProductoGranel(null, "REF-GRA-1", "Cafe a granel",
                new BigDecimal("23.456"), new BigDecimal("0.250"));
        new AcuerdoSuministro(proveedor, aceite, "PROV-11", new BigDecimal("9.10"), 2);
        new AcuerdoSuministro(proveedor, cafe, "PROV-12", new BigDecimal("18.00"), 3);
        System.out.println("Catalogo: " + aceite + "; " + cafe
                + " | acuerdos de " + proveedor.getRazonSocial() + ": " + proveedor.getAcuerdos().size());

        BigDecimal precioAnterior = aceite.getPrecioVenta();
        aceite.setPrecioVenta(new BigDecimal("13.50"));
        CambioPrecioHistorial cambio = aceite.crearRegistroCambio(precioAnterior);
        System.out.println("Cambio de precio en memoria: " + cambio.getPrecioAnterior()
                + " -> " + cambio.getPrecioNuevo());

        System.out.println("\nCalculos validos (BigDecimal, HALF_UP a 2 decimales):");
        System.out.println("  Envasado 2 x " + aceite.getPrecioVenta() + " = "
                + aceite.calcularImporte(new BigDecimal("2")));
        System.out.println("  Granel 1.750 kg x " + cafe.getPrecioVenta() + " = "
                + cafe.calcularImporte(new BigDecimal("1.750")));

        System.out.println("\nCantidades invalidas capturadas:");
        cantidadInvalida(aceite, new BigDecimal("2.5"));
        cantidadInvalida(cafe, new BigDecimal("1.300"));

        Venta venta = new Venta(ficha.siguienteTicket(), estacion, cajero,
                LocalDateTime.of(2026, 11, 23, 10, 30));
        LineaVenta lineaAceite = new LineaVenta(venta, aceite, new BigDecimal("2"));
        LineaVenta lineaCafe = new LineaVenta(venta, cafe, new BigDecimal("0.750"));
        new PagoVenta(venta, new BigDecimal("30.00"), MedioPago.TARJETA,
                LocalDateTime.of(2026, 11, 23, 10, 32));
        new PagoVenta(venta, new BigDecimal("14.59"), MedioPago.EFECTIVO,
                LocalDateTime.of(2026, 11, 23, 10, 33));
        new Devolucion(lineaCafe, jefa, new BigDecimal("0.250"), "Peso incorrecto",
                LocalDateTime.of(2026, 11, 23, 10, 40));
        System.out.println("\nVenta " + venta.getNumeroTicket() + ": " + venta.getLineas().size()
                + " lineas, total " + venta.getTotal() + ", pagado " + venta.getTotalPagado()
                + ", pendiente " + venta.getPendiente() + ", estado " + venta.getEstadoPago());
        System.out.println("Devolucion autorizada por " + lineaCafe.getVenta().getDevoluciones().get(0).getAutoriza().getNombre()
                + ": " + venta.getDevoluciones().get(0).getImporte());
        System.out.println("Importes fijados en linea: " + lineaAceite.getImporte()
                + " + " + lineaCafe.getImporte());

        Incidencia incidencia = new Incidencia("INC-01", estacion, cajero, surtidor,
                "Manguera con fuga", LocalDateTime.of(2026, 11, 23, 9, 0));
        incidencia.resolver(LocalDateTime.of(2026, 11, 23, 9, 45));
        System.out.println("Incidencia " + incidencia.getCodigoIncidencia() + ": "
                + incidencia.getEstado());

        new PagoCombustibleLegado(new org.example.Cliente(1, "Jorge", "6552", "MAR"),
                estacion, LocalDate.of(2026, 9, 25), new BigDecimal("40"), 14, "98");
        System.out.println("Pago historico de repostaje incorporado al modelo (cliente legacy).");
    }

    private static void cantidadInvalida(org.example.model.Producto producto, BigDecimal cantidad) {
        try {
            producto.calcularImporte(cantidad);
            throw new IllegalStateException("La cantidad " + cantidad + " deberia haberse rechazado");
        } catch (IllegalArgumentException e) {
            System.out.println("  " + producto.getReferenciaComercial() + " / " + cantidad
                    + " -> rechazada: " + e.getMessage());
        }
    }

    private static void verificarPersistencia() throws IOException {
        Path sandbox = Files.createTempDirectory("gasolinera-demo-");

        AlmacenamientoCSV csv = new AlmacenamientoCSV();
        csv.directorio = sandbox;
        csv.clientes = sandbox.resolve("clientes.csv");
        csv.pagos = sandbox.resolve("pagos.csv");
        csv.crearDirectorioYArchivos();

        List<org.example.Cliente> clientes = List.of(
                new org.example.Cliente(1, "Jorge", "6552", "MAR"),
                new org.example.Cliente(2, "Emma", "62346", "AMO"));
        List<Pagos> pagos = List.of(
                new Pagos(1, 1, "25/09/2026", 40, 14, "98"),
                new Pagos(2, 2, "25/09/2026", 50, 25, "95"));

        csv.escribirClientes(clientes);
        csv.escribirPagos(pagos);
        List<org.example.Cliente> clientesCsv = csv.leerClientes();
        List<Pagos> pagosCsv = csv.leerPagos();
        comprobar(clientes.toString().equals(clientesCsv.toString()), "CSV clientes");
        comprobar(pagos.toString().equals(pagosCsv.toString()), "CSV pagos");
        System.out.println("CSV -> " + clientesCsv.size() + " clientes, " + pagosCsv.size()
                + " pagos. Ejemplo: " + clientesCsv.get(0));

        AlmacenamientoJson json = new AlmacenamientoJson();
        json.directorio = sandbox;
        json.clientes = sandbox.resolve("clientes.json");
        json.pagos = sandbox.resolve("pagos.json");
        json.crearDirectorioYArchivos();

        json.escribirClientes(clientes);
        json.escribirPagos(pagos);
        List<org.example.Cliente> clientesJson = json.leerClientes();
        List<Pagos> pagosJson = json.leerPagos();
        comprobar(clientes.toString().equals(clientesJson.toString()), "JSON clientes");
        comprobar(pagos.toString().equals(pagosJson.toString()), "JSON pagos");
        System.out.println("JSON -> " + clientesJson.size() + " clientes, " + pagosJson.size()
                + " pagos. Ejemplo: " + pagosJson.get(0));

        System.out.println("Archivos reales de Datos/ intactos; usados solo en " + sandbox);
    }

    private static void comprobar(boolean condicion, String que) {
        if (!condicion) {
            throw new IllegalStateException("Fallo en la comprobacion de " + que);
        }
        System.out.println("  OK: " + que);
    }
}
