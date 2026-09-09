package com.example.zoom_clone.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zoom_clone.R;
import com.example.zoom_clone.data.FirebaseManager;
import com.example.zoom_clone.databinding.ActivityLoginBinding;
import com.example.zoom_clone.ui.MainActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseManager firebaseManager;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseManager = FirebaseManager.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("464605072934-qm2kp4if4o6u9ikfv4ncso1l3k64sn0i.apps.googleusercontent.com")
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            if (account != null && account.getIdToken() != null) {
                                setLoading(true);
                                firebaseManager.signInWithGoogle(account.getIdToken())
                                        .addOnSuccessListener(aVoid -> {
                                            firebaseManager.refreshAndSaveFcmToken();
                                            setLoading(false);
                                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            setLoading(false);
                                            Toast.makeText(LoginActivity.this, "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        });
                            }
                        } catch (ApiException e) {
                            setLoading(false);
                            Toast.makeText(LoginActivity.this, "Google Auth Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
            String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            setLoading(true);
            firebaseManager.signIn(email, password)
                    .addOnSuccessListener(authResult -> {
                        firebaseManager.fetchSelfData().addOnCompleteListener(t -> {
                            firebaseManager.refreshAndSaveFcmToken();
                            setLoading(false);
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        });
                    })
                    .addOnFailureListener(e -> {
                        setLoading(false);
                        Toast.makeText(LoginActivity.this, "Sign-in error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });

        binding.btnGoogleSignIn.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        binding.tvForgotPassword.setOnClickListener(v -> {
            String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email above first", Toast.LENGTH_SHORT).show();
                return;
            }
            new AlertDialog.Builder(this)
                    .setTitle("Reset Password")
                    .setMessage("Send a password reset email to:\n" + email + "?")
                    .setPositiveButton("Send", (dialog, which) -> {
                        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Password reset email sent! Check your inbox.", Toast.LENGTH_LONG).show())
                                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        binding.tvGoToSignUp.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        });
    }

    private void setLoading(boolean loading) {
        if (binding.progressBar != null) {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
        binding.btnLogin.setEnabled(!loading);
        binding.btnGoogleSignIn.setEnabled(!loading);
    }
}
