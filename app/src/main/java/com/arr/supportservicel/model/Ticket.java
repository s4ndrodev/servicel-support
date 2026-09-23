package com.arr.supportservicel.model;

public class Ticket {
    private String id;
    private String estado;
    private String ultimoMensaje;
    private String ultimaActualizacion;

    public Ticket(String id, String estado, String ultimoMensaje, String ultimaActualizacion) {
        this.id = id;
        this.estado = estado;
        this.ultimoMensaje = ultimoMensaje;
        this.ultimaActualizacion = ultimaActualizacion;
    }

    public String getId() {
        return id;
    }

    public String getEstado() {
        return estado;
    }

    public String getUltimoMensaje() {
        return ultimoMensaje;
    }

    public String getUltimaActualizacion() {
        return ultimaActualizacion;
    }
}
