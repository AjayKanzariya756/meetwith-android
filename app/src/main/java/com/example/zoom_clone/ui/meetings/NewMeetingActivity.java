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
            firebaseManager.createMeeting(title, password)
                    .addOnSuccessListener(ref -> {
                        String userId = user != null ? user.getId() : "user_" + System.currentTimeMillis();
                        String userName = user != null ? user.getName() : "Host";

                        Intent intent = new Intent(NewMeetingActivity.this, VideoConferenceActivity.class);
                        intent.putExtra("conferenceID", meetingId);
                        intent.putExtra("userID", userId);
                        intent.putExtra("userName", userName);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        binding.btnStartMeeting.setEnabled(true);
                        Toast.makeText(NewMeetingActivity.this, "Error starting meeting: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }
}
