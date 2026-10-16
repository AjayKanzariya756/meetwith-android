package com.example.zoom_clone.ui.meetings;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zoom_clone.data.FirebaseManager;
import com.example.zoom_clone.databinding.ActivityScheduleMeetingBinding;
import com.example.zoom_clone.receivers.MeetingReminderReceiver;
import com.example.zoom_clone.utils.DateTimeUtils;

import java.util.Calendar;

public class ScheduleMeetingActivity extends AppCompatActivity {

    private ActivityScheduleMeetingBinding binding;
    private FirebaseManager firebaseManager;
    private final Calendar scheduledCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScheduleMeetingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseManager = FirebaseManager.getInstance();

        updateDateTimeLabel();

        binding.btnSelectDate.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        scheduledCalendar.set(Calendar.YEAR, year);
                        scheduledCalendar.set(Calendar.MONTH, month);
                        scheduledCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateDateTimeLabel();
                    },
                    scheduledCalendar.get(Calendar.YEAR),
                    scheduledCalendar.get(Calendar.MONTH),
                    scheduledCalendar.get(Calendar.DAY_OF_MONTH)
            );
            dialog.getDatePicker().setMinDate(System.currentTimeMillis());
            dialog.show();
        });

        binding.btnSelectTime.setOnClickListener(v -> {
            TimePickerDialog dialog = new TimePickerDialog(
                    this,
                    (view, hourOfDay, minute) -> {
                        scheduledCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        scheduledCalendar.set(Calendar.MINUTE, minute);
                        scheduledCalendar.set(Calendar.SECOND, 0);
                        updateDateTimeLabel();
                    },
                    scheduledCalendar.get(Calendar.HOUR_OF_DAY),
                    scheduledCalendar.get(Calendar.MINUTE),
                    false
            );
            dialog.show();
        });

        binding.btnConfirmSchedule.setOnClickListener(v -> {
            String topic = binding.etScheduleTopic.getText() != null ? binding.etScheduleTopic.getText().toString().trim() : "Scheduled Conference";
            String password = binding.etSchedulePassword.getText() != null ? binding.etSchedulePassword.getText().toString().trim() : "";

            if (topic.isEmpty()) {
                topic = "Scheduled Conference";
            }

            if (scheduledCalendar.getTimeInMillis() <= System.currentTimeMillis()) {
                Toast.makeText(this, "Please select a future date and time", Toast.LENGTH_SHORT).show();
                return;
            }

            binding.btnConfirmSchedule.setEnabled(false);
            final String finalTopic = topic;
            final long scheduledTime = scheduledCalendar.getTimeInMillis();

            firebaseManager.createUpcomingMeeting(finalTopic, password, String.valueOf(scheduledTime))
                    .addOnSuccessListener(ref -> {
                        scheduleReminder(finalTopic, firebaseManager.getCurUser() != null
                                ? firebaseManager.getCurUser().getMeetingId() : "", scheduledTime);

                        Toast.makeText(this, "Meeting scheduled! You'll receive a reminder 5 minutes before.", Toast.LENGTH_LONG).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        binding.btnConfirmSchedule.setEnabled(true);
                        Toast.makeText(this, "Failed to schedule: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void scheduleReminder(String topic, String meetingId, long meetingTimeMs) {
        long reminderTimeMs = meetingTimeMs - (5 * 60 * 1000);
        if (reminderTimeMs <= System.currentTimeMillis()) {
            reminderTimeMs = System.currentTimeMillis() + 3000;
        }

        Intent intent = new Intent(this, MeetingReminderReceiver.class);
        intent.putExtra(MeetingReminderReceiver.EXTRA_TITLE, topic);
        intent.putExtra(MeetingReminderReceiver.EXTRA_MEETING_ID, meetingId);

        int requestCode = (int) (meetingTimeMs % Integer.MAX_VALUE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP, reminderTimeMs, pendingIntent);
                    } else {
                        alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTimeMs, pendingIntent);
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP, reminderTimeMs, pendingIntent);
                }
            } catch (SecurityException ignored) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTimeMs, pendingIntent);
            }
        }
    }

    private void updateDateTimeLabel() {
        String formatted = DateTimeUtils.getFormattedDate(scheduledCalendar.getTime()) + ", " +
                DateTimeUtils.getFormattedTime(scheduledCalendar.get(Calendar.HOUR_OF_DAY), scheduledCalendar.get(Calendar.MINUTE));
        binding.tvSelectedDateTime.setText("Selected: " + formatted);
    }
}
