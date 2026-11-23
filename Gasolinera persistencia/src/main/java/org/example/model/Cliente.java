package org.example.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Cliente registrado del nuevo modelo (distinto de org.example.Cliente, que
 * se conserva intacto para CSV/JSON). Codigo y nombre obligatorios; el resto
 * de contactos son opcionales segun el criterio documentado.
 */
public class Cliente extends Entidad {
    private String codigoCliente;
    private String nombre;
    private String telefono;
    private String email;
    private String direccion;
    private TarjetaFidelizacion tarjeta;
    private final List<InscripcionCampana> inscripciones = new ArrayList<>();

    public Cliente(String codigoCliente, String nombre, String telefono,
                   String email, String direccion) {
        this.codigoCliente = Validaciones.texto(codigoCliente, "codigoCliente");
        this.nombre = Validaciones.texto(nombre, "nombre");
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
    }

    void setTarjeta(TarjetaFidelizacion tarjeta) {
        this.tarjeta = tarjeta;
    }

    void registrarInscripcion(InscripcionCampana inscripcion) {
        inscripciones.add(inscripcion);
    }

    public String getCodigoCliente() {
        return codigoCliente;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getEmail() {
        return email;
    }

    public String getDireccion() {
        return direccion;
    }

    public TarjetaFidelizacion getTarjeta() {
        return tarjeta;
    }

    public List<InscripcionCampana> getInscripciones() {
        return Collections.unmodifiableList(inscripciones);
    }

    @Override
    public String toString() {
        return "Cliente{" + codigoCliente + ", " + nombre + '}';
    }
}
