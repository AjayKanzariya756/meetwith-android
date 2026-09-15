package com.example.zoom_clone.ui.conference;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zoom_clone.R;
import com.example.zoom_clone.utils.Secrets;
import com.zegocloud.uikit.prebuilt.videoconference.ZegoUIKitPrebuiltVideoConferenceConfig;
import com.zegocloud.uikit.prebuilt.videoconference.ZegoUIKitPrebuiltVideoConferenceFragment;

public class VideoConferenceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_conference);

        String conferenceID = getIntent().getStringExtra("conferenceID");
        String userID = getIntent().getStringExtra("userID");
        String userName = getIntent().getStringExtra("userName");

        if (conferenceID == null) conferenceID = "demo_room";
        if (userID == null) userID = "user_" + System.currentTimeMillis();
        if (userName == null) userName = "Guest";

        addConferenceFragment(conferenceID, userID, userName);
    }

    private void addConferenceFragment(String conferenceID, String userID, String userName) {
        long appID = Secrets.APP_ID;
        String appSign = Secrets.APP_SIGN;

        ZegoUIKitPrebuiltVideoConferenceConfig config = new ZegoUIKitPrebuiltVideoConferenceConfig();
        ZegoUIKitPrebuiltVideoConferenceFragment fragment = ZegoUIKitPrebuiltVideoConferenceFragment.newInstance(
                appID, appSign, userID, userName, conferenceID, config);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitNow();
    }
}
