package com.arr.supportservicel.model;

public class AdminMessage {
    public static final String REMITENTE_USUARIO = "usuario";
    public static final String REMITENTE_SOPORTE = "soporte";

    private String id;
    private String texto;
    private String remitente;
    private String timestamp;

    public AdminMessage(String id, String texto, String remitente, String timestamp) {
        this.id = id;
        this.texto = texto;
        this.remitente = remitente;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public String getTexto() {
        return texto;
    }

    public String getRemitente() {
        return remitente;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
