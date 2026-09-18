package com.example.zoom_clone.ui.conference;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.zoom_clone.R;
import com.example.zoom_clone.utils.Secrets;
import com.zegocloud.uikit.prebuilt.videoconference.ZegoUIKitPrebuiltVideoConferenceConfig;
import com.zegocloud.uikit.prebuilt.videoconference.ZegoUIKitPrebuiltVideoConferenceFragment;

import java.util.ArrayList;
import java.util.List;

public class VideoConferenceActivity extends AppCompatActivity {

    private String conferenceID;
    private String userID;
    private String userName;
    private boolean isVideoOn;
    private boolean isAudioOn;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean allGranted = true;
                for (Boolean granted : result.values()) {
                    if (granted == null || !granted) {
                        allGranted = false;
                        break;
                    }
                }
                if (allGranted) {
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

        if (conferenceID == null || conferenceID.trim().isEmpty()) {
            conferenceID = "room_" + System.currentTimeMillis();
        }
        if (userID == null || userID.trim().isEmpty()) {
            userID = "user_" + System.currentTimeMillis();
        }
        if (userName == null || userName.trim().isEmpty()) {
            userName = "Guest";
        }

        // ZEGOCLOUD requires alphanumeric and underscores only for conferenceID and userID
        conferenceID = conferenceID.replaceAll("[^a-zA-Z0-9_]", "");
        userID = userID.replaceAll("[^a-zA-Z0-9_]", "");

        if (checkPermissions()) {
            addConferenceFragment();
        } else {
            requestRequiredPermissions();
        }
    }

    private boolean checkPermissions() {
        String[] permissions = getRequiredPermissions();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
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

        ZegoUIKitPrebuiltVideoConferenceConfig config = new ZegoUIKitPrebuiltVideoConferenceConfig();
        config.turnOnCameraWhenJoining = isVideoOn;
        config.turnOnMicrophoneWhenJoining = isAudioOn;

        ZegoUIKitPrebuiltVideoConferenceFragment fragment = ZegoUIKitPrebuiltVideoConferenceFragment.newInstance(
                appID, appSign, userID, userName, conferenceID, config);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss();
    }
}