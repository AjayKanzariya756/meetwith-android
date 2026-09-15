package com.example.zoom_clone.ui.meetings;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zoom_clone.data.FirebaseManager;
import com.example.zoom_clone.databinding.ActivityScheduleMeetingBinding;
import com.example.zoom_clone.utils.DateTimeUtils;

import java.util.Calendar;
import java.util.Date;

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
            dialog.show();
        });

        binding.btnSelectTime.setOnClickListener(v -> {
            TimePickerDialog dialog = new TimePickerDialog(
                    this,
                    (view, hourOfDay, minute) -> {
                        scheduledCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        scheduledCalendar.set(Calendar.MINUTE, minute);
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

            binding.btnConfirmSchedule.setEnabled(false);
            firebaseManager.createUpcomingMeeting(topic, password, String.valueOf(scheduledCalendar.getTimeInMillis()))
                    .addOnSuccessListener(ref -> {
                        Toast.makeText(this, "Meeting scheduled successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        binding.btnConfirmSchedule.setEnabled(true);
                        Toast.makeText(this, "Failed to schedule: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void updateDateTimeLabel() {
        String formatted = DateTimeUtils.getFormattedDate(scheduledCalendar.getTime()) + ", " +
                DateTimeUtils.getFormattedTime(scheduledCalendar.get(Calendar.HOUR_OF_DAY), scheduledCalendar.get(Calendar.MINUTE));
        binding.tvSelectedDateTime.setText("Selected: " + formatted);
    }
}
