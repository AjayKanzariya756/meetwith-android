package com.example.zoom_clone.ui.meetings;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zoom_clone.data.FirebaseManager;
import com.example.zoom_clone.databinding.ActivityNewMeetingBinding;
import com.example.zoom_clone.models.MeetUser;
import com.example.zoom_clone.ui.conference.VideoConferenceActivity;

import java.util.Random;

public class NewMeetingActivity extends AppCompatActivity {

    private ActivityNewMeetingBinding binding;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewMeetingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseManager = FirebaseManager.getInstance();

        binding.btnStartMeeting.setOnClickListener(v -> {
            String title = binding.etMeetingTitle.getText() != null ? binding.etMeetingTitle.getText().toString().trim() : "Instant Meeting";
            String password = binding.etMeetingPassword.getText() != null ? binding.etMeetingPassword.getText().toString().trim() : "";

            MeetUser user = firebaseManager.getCurUser();
            String meetingId;
            if (binding.switchPersonalId.isChecked() && user != null && !user.getMeetingId().isEmpty()) {
                meetingId = user.getMeetingId();
            } else {
                meetingId = String.format("%010d", Math.abs(new Random().nextLong() % 10000000000L));
            }

            binding.btnStartMeeting.setEnabled(false);
            if (binding.progressBar != null) {
                binding.progressBar.setVisibility(View.VISIBLE);
            }

            try {
                firebaseManager.createMeeting(title, password)
                        .addOnSuccessListener(ref -> showMeetingInfoDialog(meetingId, password))
                        .addOnFailureListener(e -> showMeetingInfoDialog(meetingId, password));
            } catch (Exception e) {
                showMeetingInfoDialog(meetingId, password);
            }
        });
    }

    private void showMeetingInfoDialog(String meetingId, String password) {
        if (binding.progressBar != null) {
            binding.progressBar.setVisibility(View.GONE);
        }

        String passText = password.isEmpty() ? "(No password required)" : password;
        String dialogMessage = "Meeting ID: " + meetingId + "\nPassword: " + passText;

        new AlertDialog.Builder(this)
                .setTitle("Meeting Created")
                .setMessage(dialogMessage)
                .setCancelable(false)
                .setNeutralButton("Copy & Start", (dialog, which) -> {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Meeting Info", "Join my meeting on MeetWith!\nMeeting ID: " + meetingId + "\nPassword: " + (password.isEmpty() ? "None" : password));
                    if (clipboard != null) {
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(this, "Meeting info copied to clipboard!", Toast.LENGTH_SHORT).show();
                    }
                    launchConference(meetingId);
                })
                .setPositiveButton("Start Now", (dialog, which) -> launchConference(meetingId))
                .show();
    }

    private void launchConference(String meetingId) {
        MeetUser currentUser = firebaseManager.getCurUser();
        String userId = currentUser != null && !currentUser.getId().isEmpty() ? currentUser.getId() : "user_" + System.currentTimeMillis();
        String userName = currentUser != null && !currentUser.getName().isEmpty() ? currentUser.getName() : "Host";

        Intent intent = new Intent(NewMeetingActivity.this, VideoConferenceActivity.class);
        intent.putExtra("conferenceID", meetingId);
        intent.putExtra("userID", userId);
        intent.putExtra("userName", userName);
        intent.putExtra("isVideoOn", binding.switchVideo.isChecked());
        intent.putExtra("isAudioOn", binding.switchAudio.isChecked());
        startActivity(intent);
        finish();
    }
}
