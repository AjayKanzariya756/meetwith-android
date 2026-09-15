package com.example.zoom_clone.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.zoom_clone.R;
import com.example.zoom_clone.data.FirebaseManager;
import com.example.zoom_clone.databinding.FragmentSettingsBinding;
import com.example.zoom_clone.models.MeetUser;
import com.example.zoom_clone.ui.auth.LoginActivity;
import com.example.zoom_clone.ui.profile.ProfileActivity;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private FirebaseManager firebaseManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebaseManager = FirebaseManager.getInstance();

        updateProfileUI();

        binding.btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ProfileActivity.class));
        });

        binding.cardProfile.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ProfileActivity.class));
        });

        binding.btnShareMeetingId.setOnClickListener(v -> {
            MeetUser user = firebaseManager.getCurUser();
            if (user != null) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My MeetWith ID");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Here is my MeetWith Personal ID to connect: " + user.getMeetingId());
                startActivity(Intent.createChooser(shareIntent, "Share Meeting ID"));
            }
        });

        binding.btnSignOut.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Sign Out")
                    .setMessage("Are you sure you want to sign out?")
                    .setPositiveButton("Sign Out", (dialog, which) -> {
                        firebaseManager.signOut();
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateProfileUI();
    }

    private void updateProfileUI() {
        MeetUser user = firebaseManager.getCurUser();
        if (user != null) {
            binding.tvSettingsName.setText(user.getName());
            binding.tvSettingsEmail.setText(user.getEmail());
            binding.tvSettingsMeetingId.setText("Personal ID: " + user.getMeetingId());

            if (user.getImage() != null && !user.getImage().isEmpty()) {
                Glide.with(this)
                        .load(user.getImage())
                        .placeholder(R.drawable.student)
                        .error(R.drawable.student)
                        .into(binding.ivSettingsAvatar);
            }
        }
    }
}
