package com.example.zoom_clone.ui.meetings;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

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
            try {
                firebaseManager.createMeeting(title, password)
                        .addOnSuccessListener(ref -> launchConference(meetingId))
                        .addOnFailureListener(e -> launchConference(meetingId)); // Launch anyway for instant meeting
            } catch (Exception e) {
                launchConference(meetingId);
            }
        });
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