package com.example.zoom_clone.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zoom_clone.R;
import com.example.zoom_clone.models.Meeting;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MeetingAdapter extends RecyclerView.Adapter<MeetingAdapter.MeetingViewHolder> {

    public interface OnMeetingClickListener {
        void onJoinClick(Meeting meeting);
    }

    private List<Meeting> meetingList = new ArrayList<>();
    private final OnMeetingClickListener listener;

    public MeetingAdapter(OnMeetingClickListener listener) {
        this.listener = listener;
    }

    public void setMeetings(List<Meeting> meetings) {
        this.meetingList = meetings != null ? meetings : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MeetingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meeting, parent, false);
        return new MeetingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MeetingViewHolder holder, int position) {
        Meeting meeting = meetingList.get(position);
        holder.bind(meeting, listener);
    }

    @Override
    public int getItemCount() {
        return meetingList.size();
    }

    static class MeetingViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvDate;
        private final TextView tvId;
        private final TextView tvHost;
        private final Button btnJoin;

        public MeetingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvMeetingTitle);
            tvDate = itemView.findViewById(R.id.tvMeetingDate);
            tvId = itemView.findViewById(R.id.tvMeetingId);
            tvHost = itemView.findViewById(R.id.tvHostName);
            btnJoin = itemView.findViewById(R.id.btnJoin);
        }

        public void bind(Meeting meeting, OnMeetingClickListener listener) {
            tvTitle.setText(meeting.getName().isEmpty() ? "Meeting" : meeting.getName());
            tvId.setText("ID: " + meeting.getMeetingId());
            tvHost.setText("Host: " + meeting.getHostName());

            String formattedDate = meeting.getDate();
            try {
                long timestamp = Long.parseLong(meeting.getDate());
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());
                formattedDate = sdf.format(new Date(timestamp));
            } catch (Exception ignored) {}
            tvDate.setText(formattedDate);

            btnJoin.setOnClickListener(v -> {
                if (listener != null) listener.onJoinClick(meeting);
            });
        }
    }
}
