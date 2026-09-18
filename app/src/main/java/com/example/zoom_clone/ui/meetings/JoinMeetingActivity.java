package com.example.zoom_clone.ui.meetings;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zoom_clone.data.FirebaseManager;
import com.example.zoom_clone.databinding.ActivityJoinMeetingBinding;
import com.example.zoom_clone.models.MeetUser;
import com.example.zoom_clone.models.Meeting;
import com.example.zoom_clone.ui.conference.VideoConferenceActivity;

public class JoinMeetingActivity extends AppCompatActivity {

    private ActivityJoinMeetingBinding binding;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityJoinMeetingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseManager = FirebaseManager.getInstance();

        MeetUser currentUser = firebaseManager.getCurUser();
        if (currentUser != null) {
            binding.etJoinName.setText(currentUser.getName());
        }

        binding.btnConfirmJoin.setOnClickListener(v -> {
            String meetingId = binding.etMeetingId.getText() != null ? binding.etMeetingId.getText().toString().trim() : "";
            String joinName = binding.etJoinName.getText() != null ? binding.etJoinName.getText().toString().trim() : "";
            String password = binding.etJoinPassword.getText() != null ? binding.etJoinPassword.getText().toString().trim() : "";

            if (meetingId.isEmpty()) {
                Toast.makeText(this, "Please enter a Meeting ID", Toast.LENGTH_SHORT).show();
                return;
            }

            if (joinName.isEmpty()) {
                joinName = "Guest";
            }

            binding.btnConfirmJoin.setEnabled(false);

            final String finalJoinName = joinName;
            try {
                firebaseManager.findHostByMeetingId(meetingId).addOnCompleteListener(task -> {
                    try {
                        MeetUser host = task.getResult();
                        String hostName = host != null ? host.getName() : "Host";
                        String hostEmail = host != null ? host.getEmail() : "";

                        Meeting joinedRecord = new Meeting(
                                "Meeting with " + hostName,
                                meetingId,
                                hostName,
                                hostEmail,
                                String.valueOf(System.currentTimeMillis()),
                                ""
                        );
                        firebaseManager.saveJoinedMeeting(joinedRecord);
                    } catch (Exception ignored) {}

                    launchConference(meetingId, finalJoinName);
                });
            } catch (Exception e) {
                launchConference(meetingId, finalJoinName);
            }
        });
    }

    private void launchConference(String meetingId, String joinName) {
        MeetUser user = firebaseManager.getCurUser();
        String userId = user != null && !user.getId().isEmpty() ? user.getId() : "user_" + System.currentTimeMillis();

        Intent intent = new Intent(JoinMeetingActivity.this, VideoConferenceActivity.class);
        intent.putExtra("conferenceID", meetingId);
        intent.putExtra("userID", userId);
        intent.putExtra("userName", joinName);
        intent.putExtra("isVideoOn", !binding.switchJoinVideo.isChecked());
        intent.putExtra("isAudioOn", !binding.switchJoinAudio.isChecked());
        startActivity(intent);
        finish();
    }
}