package com.arr.supportservicel.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeFormatter {

    private static final SimpleDateFormat ENTRADA;
    private static final SimpleDateFormat SALIDA;

    static {
        ENTRADA = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        ENTRADA.setTimeZone(TimeZone.getTimeZone("UTC"));
        SALIDA = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
    }

    public static String formatear(String timestampIso) {
        if (timestampIso == null) return "";
        try {
            Date fecha = ENTRADA.parse(timestampIso);
            return fecha != null ? SALIDA.format(fecha) : "";
        } catch (ParseException e) {
            return "";
        }
    }
}