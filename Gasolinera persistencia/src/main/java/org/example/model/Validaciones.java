package org.example.model;

import java.math.BigDecimal;

/** Validaciones basicas compartidas por el modelo de dominio. */
public final class Validaciones {
    private Validaciones() {
    }

    public static String texto(String valor, String nombre) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(nombre + " es obligatorio");
        }
        return valor;
    }

    public static <T> T noNulo(T valor, String nombre) {
        if (valor == null) {
            throw new IllegalArgumentException(nombre + " es obligatorio");
        }
        return valor;
    }

    public static BigDecimal positivo(BigDecimal valor, String nombre) {
        noNulo(valor, nombre);
        if (valor.signum() <= 0) {
            throw new IllegalArgumentException(nombre + " debe ser mayor que 0");
        }
        return valor;
    }
}
