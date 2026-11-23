package org.example.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Proveedor del catalogo: solo se registra si tiene algun acuerdo de suministro. */
public class Proveedor extends Entidad {
    private String codigoProveedor;
    private String identificacionFiscal;
    private String razonSocial;
    private String telefono;
    private String email;
    private String direccion;
    private final List<AcuerdoSuministro> acuerdos = new ArrayList<>();

    public Proveedor(String codigoProveedor, String identificacionFiscal, String razonSocial,
                     String telefono, String email, String direccion) {
        this.codigoProveedor = Validaciones.texto(codigoProveedor, "codigoProveedor");
        this.identificacionFiscal = Validaciones.texto(identificacionFiscal, "identificacionFiscal");
        this.razonSocial = Validaciones.texto(razonSocial, "razonSocial");
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
    }

    void registrarAcuerdo(AcuerdoSuministro acuerdo) {
        acuerdos.add(acuerdo);
    }

    public String getCodigoProveedor() {
        return codigoProveedor;
    }

    public String getIdentificacionFiscal() {
        return identificacionFiscal;
    }

    public String getRazonSocial() {
        return razonSocial;
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

    public List<AcuerdoSuministro> getAcuerdos() {
        return Collections.unmodifiableList(acuerdos);
    }
}
