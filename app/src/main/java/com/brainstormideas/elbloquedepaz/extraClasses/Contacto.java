package com.brainstormideas.elbloquedepaz.extraClasses;

public class Contacto {

    private String nombre;
    private String numero;
    private String id;

    public Contacto(){

    }

    public Contacto(String nombre, String numero) {
        this.nombre = nombre;
        this.numero = numero;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Nombre: " + nombre + "\nNumero: " + numero;
    }
}
