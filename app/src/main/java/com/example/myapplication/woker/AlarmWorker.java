package com.example.myapplication.woker;

import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.myapplication.models.Note;

public class AlarmWorker extends Worker {

    public AlarmWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params
    ) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {

        // Lấy thời gian báo thức
        long alarmTime = getInputData().getLong("alarm_time", 0);

        // Lấy thời gian hiện tại
        long currentTime = System.currentTimeMillis();

        if (alarmTime > currentTime) {

            // Đợi đến thời điểm báo thức
            try {
                long delay = alarmTime - currentTime;
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                return Result.failure();
            }

            // Phát nhạc trong 10 giây
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), alarmUri);
            if (ringtone != null) {
                ringtone.play();

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


}
