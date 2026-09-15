package com.example.zoom_clone.ui.profile;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.zoom_clone.R;
import com.example.zoom_clone.data.FirebaseManager;
import com.example.zoom_clone.databinding.ActivityProfileBinding;
import com.example.zoom_clone.models.MeetUser;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private FirebaseManager firebaseManager;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseManager = FirebaseManager.getInstance();

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        binding.ivProfileAvatar.setImageURI(uri);
                    }
                }
        );

        loadUserData();

        binding.btnChangeAvatar.setOnClickListener(v -> {
            imagePickerLauncher.launch("image/*");
        });

        binding.ivProfileAvatar.setOnClickListener(v -> {
            imagePickerLauncher.launch("image/*");
        });

        binding.btnSaveProfile.setOnClickListener(v -> {
            String newName = binding.etProfileName.getText() != null ? binding.etProfileName.getText().toString().trim() : "";
            if (newName.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            binding.btnSaveProfile.setEnabled(false);

            if (selectedImageUri != null) {
                firebaseManager.uploadProfilePicture(selectedImageUri)
                        .addOnSuccessListener(uri -> updateNameAndFinish(newName))
                        .addOnFailureListener(e -> {
                            binding.btnSaveProfile.setEnabled(true);
                            Toast.makeText(this, "Avatar upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                updateNameAndFinish(newName);
            }
        });
    }

    private void updateNameAndFinish(String name) {
        firebaseManager.updateUserName(name)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    binding.btnSaveProfile.setEnabled(true);
                    Toast.makeText(ProfileActivity.this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadUserData() {
        MeetUser user = firebaseManager.getCurUser();
        if (user != null) {
            binding.etProfileName.setText(user.getName());
            if (user.getImage() != null && !user.getImage().isEmpty()) {
                Glide.with(this)
                        .load(user.getImage())
                        .placeholder(R.drawable.student)
                        .error(R.drawable.student)
                        .into(binding.ivProfileAvatar);
            }
        }
    }
}
