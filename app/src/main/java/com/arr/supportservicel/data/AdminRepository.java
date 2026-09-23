package com.arr.supportservicel.data;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.arr.supportservicel.model.AdminMessage;
import com.arr.supportservicel.model.Ticket;

import com.arr.supportservicel.model.TicketDetalle;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AdminRepository {

    private static final String BASE_URL = "https://servicelapp.vercel.app";
    private static final MediaType JSON_MEDIA_TYPE =
            MediaType.get("application/json; charset=utf-8");
    private static final String PREFS_NAME = "admin_prefs";
    private static final String KEY_SESSION_COOKIE = "admin_session_cookie";

    /** Mensaje especial de error: indica que la sesión venció y hay que volver al login. */
    public static final String SESSION_EXPIRADA = "SESSION_EXPIRADA";

    private final OkHttpClient client;
    private final SharedPreferences prefs;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public AdminRepository(@NonNull Application application) {
        client =
                new OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(10, TimeUnit.SECONDS)
                        .build();
        prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean tieneSesionGuardada() {
        return getCookie() != null;
    }

    @Nullable
    private String getCookie() {
        return prefs.getString(KEY_SESSION_COOKIE, null);
    }

    private void guardarCookie(@NonNull String cookie) {
        prefs.edit().putString(KEY_SESSION_COOKIE, cookie).apply();
    }

    public void cerrarSesion() {
        prefs.edit().remove(KEY_SESSION_COOKIE).apply();
    }

    private Request.Builder autenticado(String url) {
        return new Request.Builder()
                .url(url)
                .header("Cookie", getCookie() != null ? getCookie() : "");
    }

    public void eliminarTicket(@NonNull String ticketId, @NonNull SimpleCallback callback) {
        Request request = autenticado(BASE_URL + "/api/admin/tickets/" + ticketId).delete().build();
        ejecutarSimple(request, callback);
    }

    public void login(@NonNull String password, @NonNull SimpleCallback callback) {
        try {
            JSONObject body = new JSONObject();
            body.put("password", password);

            Request request =
                    new Request.Builder()
                            .url(BASE_URL + "/api/admin/login")
                            .post(RequestBody.create(body.toString(), JSON_MEDIA_TYPE))
                            .build();

            client.newCall(request)
                    .enqueue(
                            new Callback() {
                                @Override
                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                    mainHandler.post(() -> callback.onError("Sin conexión"));
                                }

                                @Override
                                public void onResponse(
                                        @NonNull Call call, @NonNull Response response) {
                                    try (Response r = response) {
                                        if (!r.isSuccessful()) {
                                            mainHandler.post(
                                                    () ->
                                                            callback.onError(
                                                                    "Contraseña incorrecta"));
                                            return;
                                        }

                                        String sessionCookie = null;
                                        for (String setCookie : r.headers("Set-Cookie")) {
                                            if (setCookie.startsWith("admin_session=")) {
                                                sessionCookie = setCookie.split(";")[0];
                                                break;
                                            }
                                        }

                                        if (sessionCookie == null) {
                                            mainHandler.post(
                                                    () ->
                                                            callback.onError(
                                                                    "No se recibió sesión del servidor"));
                                            return;
                                        }

                                        guardarCookie(sessionCookie);
                                        mainHandler.post(callback::onSuccess);
                                    }
                                }
                            });
        } catch (JSONException e) {
            callback.onError("Error al preparar la solicitud");
        }
    }

    public void listarTickets(@NonNull Callback2<List<Ticket>> callback) {
        Request request = autenticado(BASE_URL + "/api/admin/tickets").get().build();

        client.newCall(request)
                .enqueue(
                        new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                mainHandler.post(() -> callback.onError("Sin conexión"));
                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) {
                                try (Response r = response) {
                                    if (r.code() == 401) {
                                        cerrarSesion();
                                        mainHandler.post(() -> callback.onError(SESSION_EXPIRADA));
                                        return;
                                    }
                                    if (!r.isSuccessful() || r.body() == null) {
                                        mainHandler.post(
                                                () -> callback.onError("Error del servidor"));
                                        return;
                                    }
                                    JSONObject json = new JSONObject(r.body().string());
                                    JSONArray arr = json.getJSONArray("tickets");
                                    List<Ticket> tickets = new ArrayList<>();
                                    for (int i = 0; i < arr.length(); i++) {
                                        JSONObject t = arr.getJSONObject(i);
                                        tickets.add(
                                                new Ticket(
                                                        t.getString("id"),
                                                        t.optString("estado", "abierto"),
                                                        t.optString("ultimoMensaje", ""),
                                                        t.isNull("ultimaActualizacion")
                                                                ? null
                                                                : t.optString(
                                                                        "ultimaActualizacion")));
                                    }
                                    mainHandler.post(() -> callback.onSuccess(tickets));
                                } catch (IOException | JSONException e) {
                                    mainHandler.post(() -> callback.onError("Respuesta inválida"));
                                }
                            }
                        });
    }

    public void obtenerDetalle(
            @NonNull String ticketId, @NonNull Callback2<TicketDetalle> callback) {
        Request request = autenticado(BASE_URL + "/api/admin/tickets/" + ticketId).get().build();

        client.newCall(request)
                .enqueue(
                        new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                mainHandler.post(() -> callback.onError("Sin conexión"));
                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) {
                                try (Response r = response) {
                                    if (r.code() == 401) {
                                        cerrarSesion();
                                        mainHandler.post(() -> callback.onError(SESSION_EXPIRADA));
                                        return;
                                    }
                                    if (!r.isSuccessful() || r.body() == null) {
                                        mainHandler.post(
                                                () -> callback.onError("Error del servidor"));
                                        return;
                                    }

                                    JSONObject json = new JSONObject(r.body().string());
                                    String estado =
                                            json.getJSONObject("ticket")
                                                    .optString("estado", "abierto");

                                    JSONArray arr = json.getJSONArray("mensajes");
                                    List<AdminMessage> mensajes = new ArrayList<>();
                                    for (int i = 0; i < arr.length(); i++) {
                                        JSONObject m = arr.getJSONObject(i);
                                        mensajes.add(
                                                new AdminMessage(
                                                        m.getString("id"),
                                                        m.getString("texto"),
                                                        m.getString("remitente"),
                                                        m.isNull("timestamp")
                                                                ? null
                                                                : m.optString("timestamp")));
                                    }

                                    TicketDetalle detalle = new TicketDetalle(estado, mensajes);
                                    mainHandler.post(() -> callback.onSuccess(detalle));
                                } catch (IOException | JSONException e) {
                                    mainHandler.post(() -> callback.onError("Respuesta inválida"));
                                }
                            }
                        });
    }

    private void ejecutarSimple(Request request, SimpleCallback callback) {
        client.newCall(request)
                .enqueue(
                        new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                mainHandler.post(() -> callback.onError("Sin conexión"));
                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) {
                                try (Response r = response) {
                                    if (r.code() == 401) {
                                        cerrarSesion();
                                        mainHandler.post(() -> callback.onError(SESSION_EXPIRADA));
                                        return;
                                    }
                                    if (r.isSuccessful()) {
                                        mainHandler.post(callback::onSuccess);
                                    } else {
                                        mainHandler.post(
                                                () -> callback.onError("Error del servidor"));
                                    }
                                }
                            }
                        });
    }

    public void responder(
            @NonNull String ticketId, @NonNull String texto, @NonNull SimpleCallback callback) {
        try {
            JSONObject body = new JSONObject();
            body.put("texto", texto);

            Request request =
                    autenticado(BASE_URL + "/api/admin/tickets/" + ticketId + "/messages")
                            .post(RequestBody.create(body.toString(), JSON_MEDIA_TYPE))
                            .build();

            ejecutarSimple(request, callback);
        } catch (JSONException e) {
            callback.onError("Error al preparar el mensaje");
        }
    }

    public void enviarAviso(@NonNull String texto, @NonNull SimpleCallback callback) {
        try {
            JSONObject body = new JSONObject();
            body.put("texto", texto);

            Request request =
                    autenticado(BASE_URL + "/api/admin/broadcast")
                            .post(RequestBody.create(body.toString(), JSON_MEDIA_TYPE))
                            .build();

            ejecutarSimple(request, callback);
        } catch (JSONException e) {
            callback.onError("Error al preparar el aviso");
        }
    }

    public void cambiarEstado(
            @NonNull String ticketId, @NonNull String estado, @NonNull SimpleCallback callback) {
        try {
            JSONObject body = new JSONObject();
            body.put("estado", estado);

            Request request =
                    autenticado(BASE_URL + "/api/admin/tickets/" + ticketId)
                            .method("PATCH", RequestBody.create(body.toString(), JSON_MEDIA_TYPE))
                            .build();

            ejecutarSimple(request, callback);
        } catch (JSONException e) {
            callback.onError("Error al preparar la solicitud");
        }
    }

    public interface Callback2<T> {
        void onSuccess(T resultado);

        void onError(String mensajeError);
    }

    public interface SimpleCallback {
        void onSuccess();

        void onError(String mensajeError);
    }
}
