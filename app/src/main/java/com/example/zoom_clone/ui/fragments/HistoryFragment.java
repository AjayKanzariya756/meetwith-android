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

import com.example.zoom_clone.adapters.MeetingAdapter;
import com.example.zoom_clone.data.FirebaseManager;
import com.example.zoom_clone.databinding.FragmentHistoryBinding;
import com.example.zoom_clone.models.MeetUser;
import com.example.zoom_clone.models.Meeting;
import com.example.zoom_clone.ui.conference.VideoConferenceActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment implements MeetingAdapter.OnMeetingClickListener {

    private FragmentHistoryBinding binding;
    private FirebaseManager firebaseManager;
    private MeetingAdapter adapter;
    private boolean isCreatedTab = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseManager = FirebaseManager.getInstance();
        adapter = new MeetingAdapter(this);
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvHistory.setAdapter(adapter);

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                isCreatedTab = (tab.getPosition() == 0);
                loadHistory();
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        binding.swipeRefresh.setOnRefreshListener(this::loadHistory);
        loadHistory();
    }

    private void loadHistory() {
        binding.swipeRefresh.setRefreshing(true);
        Query query = isCreatedTab ? firebaseManager.getCreatedMeetingsQuery() : firebaseManager.getJoinedMeetingsQuery();
        if (query == null) {
            binding.swipeRefresh.setRefreshing(false);
            return;
        }

        query.get().addOnSuccessListener(snapshots -> {
            if (!isAdded()) return;
            binding.swipeRefresh.setRefreshing(false);
            List<Meeting> meetings = new ArrayList<>();
            for (QueryDocumentSnapshot doc : snapshots) {
                meetings.add(doc.toObject(Meeting.class));
            }
            adapter.setMeetings(meetings);
            binding.tvNoHistory.setVisibility(meetings.isEmpty() ? View.VISIBLE : View.GONE);
        }).addOnFailureListener(e -> {
            if (!isAdded()) return;
            binding.swipeRefresh.setRefreshing(false);
        });
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
