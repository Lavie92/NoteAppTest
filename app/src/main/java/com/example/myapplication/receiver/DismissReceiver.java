package com.example.myapplication.receiver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationManagerCompat;

import com.example.myapplication.woker.ConstantsManager;

public class DismissReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Xử lý khi người dùng nhấn vào nút "Tắt"
        // Đóng thông báo
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(context);
        notificationManager.cancel(ConstantsManager.NOTIFICATION_ID);
    }
}