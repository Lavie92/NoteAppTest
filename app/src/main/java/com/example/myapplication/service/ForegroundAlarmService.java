//package com.example.myapplication.service;
//
//import android.app.AlarmManager;
//import android.app.Notification;
//import android.app.NotificationChannel;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.app.Service;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Build;
//import android.os.IBinder;
//
//import androidx.annotation.Nullable;
//import androidx.core.app.NotificationCompat;
//import androidx.work.Data;
//import androidx.work.OneTimeWorkRequest;
//import androidx.work.WorkManager;
//
//import com.example.myapplication.R;
//import com.example.myapplication.woker.AlarmWorker;
//
//import java.util.concurrent.TimeUnit;
//
//public class ForegroundAlarmService extends Service {
//
//    private static final int NOTIFICATION_ID = 123;
//    private static final String CHANNEL_ID = "ForegroundAlarmServiceChannel";
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        if (intent != null) {
//            long timeInMillis = intent.getLongExtra("alarm_time", 0);
//            setAlarm(timeInMillis);
//        }
//
//        // Create a notification for the foreground service
//        createNotificationChannel();
//        Notification notification = createNotification();
//        startForeground(NOTIFICATION_ID, notification);
//
//        return START_STICKY;
//    }
//
//    private void setAlarm(long timeInMillis) {
//        Data inputData = new Data.Builder()
//                .putLong("alarm_time", timeInMillis)
//                .build();
//
//        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(AlarmWorker.class)
//                .setInputData(inputData)
//                .setInitialDelay(timeInMillis - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
//                .build();
//
//        WorkManager.getInstance(getApplicationContext()).enqueue(workRequest);
//    }
//
//
//    private Notification createNotification() {
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setSmallIcon(R.drawable.circle)
//                .setContentTitle("Foreground Alarm Service")
//                .setContentText("Alarm Service is running in the foreground")
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
//
//        return builder.build();
//    }
//
//    private void createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel(
//                    CHANNEL_ID, "Foreground Alarm Service Channel", NotificationManager.IMPORTANCE_DEFAULT);
//            NotificationManager manager = getSystemService(NotificationManager.class);
//            manager.createNotificationChannel(channel);
//        }
//    }
//
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//    }
//}
