package com.example.zoom_clone.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zoom_clone.data.FirebaseManager;
import com.example.zoom_clone.databinding.ActivitySignUpBinding;
import com.example.zoom_clone.ui.MainActivity;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseManager = FirebaseManager.getInstance();

        binding.btnSignUp.setOnClickListener(v -> {
            String name = binding.etName.getText() != null ? binding.etName.getText().toString().trim() : "";
            String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
            String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            binding.btnSignUp.setEnabled(false);
            firebaseManager.signUp(email, password, name)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(SignUpActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                        finishAffinity();
                    })
                    .addOnFailureListener(e -> {
                        binding.btnSignUp.setEnabled(true);
                        Toast.makeText(SignUpActivity.this, "Registration failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });

        binding.tvGoToLogin.setOnClickListener(v -> finish());
    }
}
