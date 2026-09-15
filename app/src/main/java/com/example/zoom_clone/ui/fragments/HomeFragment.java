package com.example.zoom_clone.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.zoom_clone.R;
import com.example.zoom_clone.adapters.MeetingAdapter;
import com.example.zoom_clone.data.FirebaseManager;
import com.example.zoom_clone.databinding.FragmentHomeBinding;
import com.example.zoom_clone.models.MeetUser;
import com.example.zoom_clone.models.Meeting;
import com.example.zoom_clone.ui.conference.VideoConferenceActivity;
import com.example.zoom_clone.ui.meetings.JoinMeetingActivity;
import com.example.zoom_clone.ui.meetings.NewMeetingActivity;
import com.example.zoom_clone.ui.meetings.ScheduleMeetingActivity;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements MeetingAdapter.OnMeetingClickListener {

    private FragmentHomeBinding binding;
    private FirebaseManager firebaseManager;
    private MeetingAdapter meetingAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseManager = FirebaseManager.getInstance();

        meetingAdapter = new MeetingAdapter(this);
        binding.rvRecentMeetings.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvRecentMeetings.setAdapter(meetingAdapter);

        loadUserData();
        loadRecentMeetings();

        // 4 Quick Actions
        binding.btnActionNewMeeting.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), NewMeetingActivity.class));
        });

        binding.btnActionJoin.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), JoinMeetingActivity.class));
        });

        binding.btnActionSchedule.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ScheduleMeetingActivity.class));
        });

        binding.btnActionShare.setOnClickListener(v -> {
            MeetUser user = firebaseManager.getCurUser();
            if (user != null) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Join my MeetWith conference");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Join my MeetWith conference using Meeting ID: " + user.getMeetingId());
                startActivity(Intent.createChooser(shareIntent, "Share Meeting ID"));
            }
        });
    }

    private void loadUserData() {
        MeetUser user = firebaseManager.getCurUser();
        if (user != null) {
            binding.tvGreeting.setText("Hello, " + user.getName() + "!");
            binding.tvUserMeetingId.setText("Personal ID: " + user.getMeetingId());
            if (user.getImage() != null && !user.getImage().isEmpty()) {
                Glide.with(this)
                        .load(user.getImage())
                        .placeholder(R.drawable.student)
                        .error(R.drawable.student)
                        .into(binding.ivHeaderAvatar);
            }
        } else {
            firebaseManager.fetchSelfData().addOnSuccessListener(u -> {
                if (u != null && isAdded()) {
                    loadUserData();
                }
            });
        }
    }

    private void loadRecentMeetings() {
        if (firebaseManager.getCreatedMeetingsQuery() != null) {
            firebaseManager.getCreatedMeetingsQuery().limit(5).get().addOnSuccessListener(snapshots -> {
                if (!isAdded()) return;
                List<Meeting> list = new ArrayList<>();
                for (QueryDocumentSnapshot doc : snapshots) {
                    list.add(doc.toObject(Meeting.class));
                }
                meetingAdapter.setMeetings(list);
                binding.tvNoRecentMeetings.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
            });
        }
    }

    @Override
    public void onJoinClick(Meeting meeting) {
        MeetUser user = firebaseManager.getCurUser();
        String userId = user != null ? user.getId() : "user_" + System.currentTimeMillis();
        String userName = user != null ? user.getName() : "Guest";

        Intent intent = new Intent(getActivity(), VideoConferenceActivity.class);
        intent.putExtra("conferenceID", meeting.getMeetingId());
        intent.putExtra("userID", userId);
        intent.putExtra("userName", userName);
        startActivity(intent);
    }
}
