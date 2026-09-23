package com.arr.supportservicel.model;

import java.util.List;

public class TicketDetalle {
    private final String estado;
    private final List<AdminMessage> mensajes;

    public TicketDetalle(String estado, List<AdminMessage> mensajes) {
        this.estado = estado;
        this.mensajes = mensajes;
    }

    public String getEstado() {
        return estado;
    }

    public List<AdminMessage> getMensajes() {
        return mensajes;
    }
}
