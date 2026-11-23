package org.example.model;

/**
 * Base comun de todas las entidades del modelo de dominio.
 * El id tecnico es nullable: antes de persistir en MySQL todavia no existe.
 * Esta clase no contiene JDBC ni sentencias SQL.
 */
public abstract class Entidad {
    private Integer id;

    protected Entidad() {
    }

    protected Entidad(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean esNueva() {
        return id == null;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id=" + id + '}';
    }
}
