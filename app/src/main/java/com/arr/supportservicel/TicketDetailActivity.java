package com.arr.supportservicel;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.arr.supportservicel.data.AdminRepository;
import com.arr.supportservicel.model.AdminMessage;
import com.arr.supportservicel.model.TicketDetalle;
import java.util.List;

import com.arr.supportservicel.databinding.ActivityTicketDetailBinding;

public class TicketDetailActivity extends AppCompatActivity {

    private static final long REFRESH_INTERVAL_MS = 4000;

    private ActivityTicketDetailBinding binding;
    private AdminRepository repository;
    private AdminMessageAdapter adapter;
    private String ticketId;
    private boolean cerrado = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable refreshRunnable =
            new Runnable() {
                @Override
                public void run() {
                    cargarDetalle();
                    handler.postDelayed(this, REFRESH_INTERVAL_MS);
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTicketDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ticketId = getIntent().getStringExtra("ticketId");
        repository = new AdminRepository(getApplication());

        binding.tvTicketIdDetail.setText("Ticket #" + ticketId.substring(0, 8).toUpperCase());

        adapter = new AdminMessageAdapter();
        binding.rvMensajesAdmin.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMensajesAdmin.setAdapter(adapter);

        binding.btnCambiarEstado.setEnabled(false);
        binding.btnCambiarEstado.setText("...");
        binding.btnCambiarEstado.setOnClickListener(v -> cerrarTicket());

        binding.btnResponder.setOnClickListener(v -> enviarRespuesta());
        binding.btnEliminarTicket.setOnClickListener(v -> confirmarEliminar());
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(refreshRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(refreshRunnable);
    }

    private void confirmarEliminar() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar ticket")
                .setMessage(
                        "Esto borra el ticket y todos sus mensajes de forma permanente. ¿Continuar?")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarTicket())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarTicket() {
        repository.eliminarTicket(
                ticketId,
                new AdminRepository.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(
                                        TicketDetailActivity.this,
                                        "Ticket eliminado",
                                        Toast.LENGTH_SHORT)
                                .show();
                        finish();
                    }

                    @Override
                    public void onError(String mensajeError) {
                        if (AdminRepository.SESSION_EXPIRADA.equals(mensajeError)) {
                            startActivity(
                                    new android.content.Intent(
                                            TicketDetailActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            Toast.makeText(
                                            TicketDetailActivity.this,
                                            mensajeError,
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });
    }

    private void cargarDetalle() {
        repository.obtenerDetalle(
                ticketId,
                new AdminRepository.Callback2<>() {
                    @Override
                    public void onSuccess(TicketDetalle detalle) {
                        adapter.actualizar(detalle.getMensajes());
                        if (!detalle.getMensajes().isEmpty()) {
                            binding.rvMensajesAdmin.scrollToPosition(
                                    detalle.getMensajes().size() - 1);
                        }
                        actualizarBotonEstado(detalle.getEstado());
                    }

                    @Override
                    public void onError(String mensajeError) {
                        if (AdminRepository.SESSION_EXPIRADA.equals(mensajeError)) {
                            startActivity(
                                    new Intent(TicketDetailActivity.this, LoginActivity.class));
                            finish();
                        }
                    }
                });
    }

    private void actualizarBotonEstado(String estado) {
        cerrado = "cerrado".equals(estado);
        binding.btnCambiarEstado.setText(cerrado ? "Cerrado" : "Cerrar");
        binding.btnCambiarEstado.setEnabled(!cerrado);
    }

    private void enviarRespuesta() {
        String texto = binding.etRespuesta.getText().toString().trim();
        if (texto.isEmpty()) return;

        repository.responder(
                ticketId,
                texto,
                new AdminRepository.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        binding.etRespuesta.setText("");
                        cargarDetalle();
                    }

                    @Override
                    public void onError(String mensajeError) {
                        Toast.makeText(TicketDetailActivity.this, mensajeError, Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    private void cerrarTicket() {
        repository.cambiarEstado(
                ticketId,
                "cerrado",
                new AdminRepository.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        actualizarBotonEstado("cerrado");
                        Toast.makeText(
                                        TicketDetailActivity.this,
                                        "Ticket cerrado",
                                        Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onError(String mensajeError) {
                        if (AdminRepository.SESSION_EXPIRADA.equals(mensajeError)) {
                            startActivity(
                                    new Intent(TicketDetailActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            Toast.makeText(
                                            TicketDetailActivity.this,
                                            mensajeError,
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });
    }
}
