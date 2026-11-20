package com.example.zoom_clone.receivers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.zoom_clone.R;
import com.example.zoom_clone.ui.MainActivity;

public class MeetingReminderReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "meetwith_reminders";
    public static final String EXTRA_TITLE = "meeting_title";
    public static final String EXTRA_MEETING_ID = "meeting_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra(EXTRA_TITLE);
        String meetingId = intent.getStringExtra(EXTRA_MEETING_ID);

        if (title == null || title.isEmpty()) title = "Upcoming Meeting";
        if (meetingId == null) meetingId = "";

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (manager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Meeting Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Reminds you about upcoming scheduled meetings");
            channel.enableVibration(true);
            manager.createNotificationChannel(channel);
        }

        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_calendar)
                .setContentTitle("Meeting Starting Soon!")
                .setContentText(title + " starts in 5 minutes  •  ID: " + meetingId)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Your meeting \"" + title + "\" is starting in 5 minutes.\nMeeting ID: " + meetingId))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(new long[]{0, 500, 200, 500});

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
