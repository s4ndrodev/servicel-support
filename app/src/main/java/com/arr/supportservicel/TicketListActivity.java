package com.arr.supportservicel;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.arr.supportservicel.data.AdminRepository;
import com.arr.supportservicel.databinding.ActivityTicketListBinding;
import com.arr.supportservicel.model.Ticket;
import com.arr.supportservicel.R;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import java.util.List;

public class TicketListActivity extends AppCompatActivity {

    private static final long REFRESH_INTERVAL_MS = 8000;

    private ActivityTicketListBinding binding;
    private AdminRepository repository;
    private TicketAdapter adapter;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable refreshRunnable =
            new Runnable() {
                @Override
                public void run() {
                    cargarTickets();
                    handler.postDelayed(this, REFRESH_INTERVAL_MS);
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTicketListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new AdminRepository(getApplication());

        adapter = new TicketAdapter(this::abrirDetalle);
        binding.rvTickets.setLayoutManager(new LinearLayoutManager(this));
        binding.rvTickets.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(this::cargarTickets);
        binding.comunicado.setOnClickListener(this::setDifusion);
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

    private void cargarTickets() {
        repository.listarTickets(
                new AdminRepository.Callback2<>() {
                    @Override
                    public void onSuccess(List<Ticket> resultado) {
                        adapter.actualizar(resultado);
                        binding.swipeRefresh.setRefreshing(false);
                    }

                    @Override
                    public void onError(String mensajeError) {
                        binding.swipeRefresh.setRefreshing(false);
                        if (AdminRepository.SESSION_EXPIRADA.equals(mensajeError)) {
                            volverALogin();
                        } else {
                            Toast.makeText(
                                            TicketListActivity.this,
                                            mensajeError,
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });
    }

    private void volverALogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void abrirDetalle(Ticket ticket) {
        Intent intent = new Intent(this, TicketDetailActivity.class);
        intent.putExtra("ticketId", ticket.getId());
        startActivity(intent);
    }

    public void setDifusion(View view) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View customView = inflater.inflate(R.layout.layout_dialog_message, null);
        TextInputEditText etUsername = customView.findViewById(R.id.message);
        new MaterialAlertDialogBuilder(this)
                .setTitle("Difusión")
                .setView(customView)
                .setCancelable(false)
                .setPositiveButton(
                        "Enviar",
                        (dialog, which) -> {
                            String message = etUsername.getText().toString();
                            repository.enviarAviso(
                                    message,
                                    new AdminRepository.SimpleCallback() {

                                        @Override
                                        public void onSuccess() {
                                            Toast.makeText(
                                                            TicketListActivity.this,
                                                            "Mensaje de difusión enviado",
                                                            Toast.LENGTH_LONG)
                                                    .show();
                                        }

                                        @Override
                                        public void onError(String mensajeError) {
                                            Toast.makeText(
                                                            TicketListActivity.this,
                                                            "Error: " + mensajeError,
                                                            Toast.LENGTH_LONG)
                                                    .show();
                                        }
                                    });
                        })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
