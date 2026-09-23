package com.arr.supportservicel;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.arr.supportservicel.data.AdminRepository;
import com.arr.supportservicel.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AdminRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new AdminRepository(getApplication());

        if (repository.tieneSesionGuardada()) {
            irATickets();
            return;
        }

        binding.btnEntrar.setOnClickListener(v -> intentarLogin());
    }

    private void intentarLogin() {
        String password = binding.etPassword.getText().toString().trim();
        if (password.isEmpty()) {
            Toast.makeText(this, "Ingresá la contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnEntrar.setEnabled(false);

        repository.login(password, new AdminRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                irATickets();
            }

            @Override
            public void onError(String mensajeError) {
                binding.btnEntrar.setEnabled(true);
                Toast.makeText(LoginActivity.this, mensajeError, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void irATickets() {
        startActivity(new Intent(this, TicketListActivity.class));
        finish();
    }
}