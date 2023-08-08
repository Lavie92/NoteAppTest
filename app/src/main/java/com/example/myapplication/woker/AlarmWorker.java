package com.example.myapplication.woker;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.receiver.DismissReceiver;


public class AlarmWorker extends Worker {
    private Context context;

    public AlarmWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params
    ) {
        super(context, params);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {

        Data inputData = getInputData();
        long alarmTime = inputData.getLong("alarm_time", 0);
        String noteContent = inputData.getString("note_content");
        long currentTime = System.currentTimeMillis();

        if (alarmTime > currentTime) {

            try {
                long delay = alarmTime - currentTime;
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                return Result.failure();
            }

            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), alarmUri);
            if (ringtone != null) {
                ringtone.play();
                showNotification(noteContent);
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                ringtone.stop();
            }
        }
        return Result.success();
    }

    private void showNotification(String content) {
        if (content != null) {
            int newlineIndex = content.indexOf("\n");
            if (newlineIndex != -1) {
                content = content.substring(0, newlineIndex);
            }
            if (content.length() > 20) {
                content = content.substring(0, 20) + "...";
            }
        }
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE);
        Intent dismissIntent = new Intent(context, DismissReceiver.class);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(context, 0, dismissIntent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ConstantsManager.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(content)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {
            requestNotificationPolicyPermission();
            return;
        }
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(ConstantsManager.NOTIFICATION_ID, builder.build());
    }

    private void requestNotificationPolicyPermission() {
        Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    private PendingIntent getCancelNotificationIntent() {
        Intent cancelIntent = new Intent(context, DismissReceiver.class);
        return PendingIntent.getBroadcast(context, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}