package com.example.zoom_clone.ui.conference;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.zoom_clone.R;
import com.example.zoom_clone.utils.Secrets;
import com.zegocloud.uikit.prebuilt.videoconference.ZegoUIKitPrebuiltVideoConferenceConfig;
import com.zegocloud.uikit.prebuilt.videoconference.ZegoUIKitPrebuiltVideoConferenceFragment;
import com.zegocloud.uikit.prebuilt.videoconference.config.ZegoMenuBarButtonName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VideoConferenceActivity extends AppCompatActivity {

    private String conferenceID;
    private String userID;
    private String userName;
    private boolean isVideoOn;
    private boolean isAudioOn;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean cameraGranted = result.get(Manifest.permission.CAMERA);
                Boolean audioGranted = result.get(Manifest.permission.RECORD_AUDIO);

                boolean coreGranted = Boolean.TRUE.equals(cameraGranted) && Boolean.TRUE.equals(audioGranted);

                if (coreGranted) {
                    addConferenceFragment();
                } else {
                    Toast.makeText(this, "Camera and Microphone permissions are required to join the meeting.", Toast.LENGTH_LONG).show();
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_conference);

        conferenceID = getIntent().getStringExtra("conferenceID");
        userID = getIntent().getStringExtra("userID");
        userName = getIntent().getStringExtra("userName");
        isVideoOn = getIntent().getBooleanExtra("isVideoOn", true);
        isAudioOn = getIntent().getBooleanExtra("isAudioOn", true);

        if (conferenceID != null) {
            conferenceID = conferenceID.replaceAll("[^a-zA-Z0-9_]", "");
        }
        if (userID != null) {
            userID = userID.replaceAll("[^a-zA-Z0-9_]", "");
        }

        if (conferenceID == null || conferenceID.trim().isEmpty()) {
            conferenceID = "room_" + System.currentTimeMillis();
        }
        if (userID == null || userID.trim().isEmpty()) {
            userID = "user_" + System.currentTimeMillis();
        }
        if (userName == null || userName.trim().isEmpty()) {
            userName = "Guest";
        }

        if (checkPermissions()) {
            addConferenceFragment();
        } else {
            requestRequiredPermissions();
        }
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
               ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestRequiredPermissions() {
        requestPermissionLauncher.launch(getRequiredPermissions());
    }

    private String[] getRequiredPermissions() {
        List<String> list = new ArrayList<>();
        list.add(Manifest.permission.CAMERA);
        list.add(Manifest.permission.RECORD_AUDIO);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            list.add(Manifest.permission.BLUETOOTH_CONNECT);
        }
        return list.toArray(new String[0]);
    }

    private void addConferenceFragment() {
        long appID = Secrets.APP_ID;
        String appSign = Secrets.APP_SIGN;

        Log.d("VideoConference", "addConferenceFragment: appID=" + appID + ", conferenceID=" + conferenceID + ", userID=" + userID + ", userName=" + userName);

        try {
            ZegoUIKitPrebuiltVideoConferenceConfig config = new ZegoUIKitPrebuiltVideoConferenceConfig();
            config.turnOnCameraWhenJoining = isVideoOn;
            config.turnOnMicrophoneWhenJoining = isAudioOn;
            config.useSpeakerWhenJoining = true;

            // In-meeting Chat button
            config.bottomMenuBarConfig.buttons = new ArrayList<>(Arrays.asList(
                    ZegoMenuBarButtonName.TOGGLE_CAMERA_BUTTON,
                    ZegoMenuBarButtonName.TOGGLE_MICROPHONE_BUTTON,
                    ZegoMenuBarButtonName.CHAT_BUTTON,
                    ZegoMenuBarButtonName.LEAVE_BUTTON
            ));

            ZegoUIKitPrebuiltVideoConferenceFragment fragment = ZegoUIKitPrebuiltVideoConferenceFragment.newInstance(
                    appID, appSign, userID, userName, conferenceID, config);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commitAllowingStateLoss();
        } catch (Exception e) {
            Log.e("VideoConference", "Error embedding ZegoUIKit fragment: " + e.getMessage(), e);
            Toast.makeText(this, "Error starting conference: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}