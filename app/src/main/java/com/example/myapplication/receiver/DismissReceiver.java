package com.example.myapplication.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

import com.example.myapplication.woker.ConstantsManager;

public class DismissReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(
                context.getApplicationContext());
        notificationManager.cancel(ConstantsManager.NOTIFICATION_ID);
    }
}