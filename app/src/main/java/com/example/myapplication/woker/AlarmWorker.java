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
        String noteId = inputData.getString("note_id");
        boolean noteIsReminder = inputData.getBoolean("note_is_reminder", false);
        long currentTime = System.currentTimeMillis();

        if (alarmTime > currentTime && noteIsReminder == true) {

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
//            createNotification();
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                ringtone.stop();
            }
//            // Tạo PendingIntent cho khi người dùng nhấn vào thông báo
//            Intent intent = new Intent(context, MainActivity.class);
//            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
//            stackBuilder.addNextIntentWithParentStack(intent);
//            PendingIntent pendingIntent =
//                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//
//            // Tạo action cho nút "Mở"
//            Intent openIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.example.com"));
//            PendingIntent openPendingIntent = PendingIntent.getActivity(context, 0, openIntent,
//                    PendingIntent.FLAG_IMMUTABLE);
//
//            // Tạo action cho nút "Tắt"
//            Intent dismissIntent = new Intent(context, DismissReceiver.class);
//            PendingIntent dismissPendingIntent =
//                    PendingIntent.getBroadcast(context, 0, dismissIntent, PendingIntent.FLAG_IMMUTABLE);
//
//            // Tạo notification
//            NotificationCompat.Builder builder =
//                    new NotificationCompat.Builder(context, ConstantsManager.CHANNEL_ID)
//                            .setSmallIcon(R.mipmap.ic_launcher)
//                            .setContentTitle("Thông báo báo thức")
//                            .setContentText(noteContent)
//                            .setPriority(NotificationCompat.PRIORITY_HIGH)
//                            .setContentIntent(pendingIntent)
//                            .addAction(R.mipmap.ic_launcher, "Mở", openPendingIntent)
//                            .addAction(R.drawable.circle, "Tắt", dismissPendingIntent)
//                            .setDefaults(Notification.DEFAULT_ALL)
//                            .setAutoCancel(true);
//
//            // Hiển thị notification
//            NotificationManagerCompat notificationManager =
//                    NotificationManagerCompat.from(context);
//            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
//                    PackageManager.PERMISSION_GRANTED) {
//                requestNotificationPolicyPermission();
//                return Result.failure();
//            }
//            notificationManager.notify(ConstantsManager.NOTIFICATION_ID, builder.build());
        }
        // Lấy dữ liệu từ InputData



        return Result.success();
    }

    private void showNotification(String content) {
        if (content != null) {
            int newlineIndex = content.indexOf("\n");
            if (newlineIndex != -1) {
                content = content.substring(0, newlineIndex); //
            }
            if (content.length() > 20) {
                content = content.substring(0, 20) + "...";
            }
        }
        // Tạo notification
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(), ConstantsManager.CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(content)
                        .setContentText(content)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

        // Hiển thị notification
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(getApplicationContext());
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {
            requestNotificationPolicyPermission();
            return;
        }
        notificationManager.notify(ConstantsManager.NOTIFICATION_ID, builder.build());
    }
    private void requestNotificationPolicyPermission() {
        // Xin cấp quyền ACCESS_NOTIFICATION_POLICY tại đây
        Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    private void createNotification() {
        // Tạo PendingIntent cho khi người dùng nhấn vào thông báo
        Intent intent = new Intent(context, MainActivity.class); // Thay YourActivity bằng Activity bạn muốn mở khi nhấn vào thông báo
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Xây dựng thông báo
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ConstantsManager.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Thông báo báo thức")
                .setContentText("Nhấn để tắt báo thức")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true); // Tự động đóng thông báo khi người dùng nhấn vào
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {
            requestNotificationPolicyPermission();
            return;
        }
        // Hiển thị thông báo
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(ConstantsManager.NOTIFICATION_ID, builder.build());
    }
}
