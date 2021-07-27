package edu.imtl.bluekare.Background;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Calendar;

import edu.imtl.bluekare.R;

public class BackgroundService extends Service {
    Calendar cur_cal = Calendar.getInstance();
    @Override
    public void onCreate() {
        super.onCreate();
        Intent intent = new Intent(this, BackgroundService.class);
        PendingIntent pintent = PendingIntent.getService(getApplicationContext(),
                0, intent, 0);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        cur_cal.setTimeInMillis(System.currentTimeMillis());
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cur_cal.getTimeInMillis(),
                60 * 1000 * 3, pintent);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "bluekare")
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setContentTitle("BLUE KARE CARES ABOUT YOU")
                .setContentText("For Test Purpose")
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Much longer text that cannot fit one line..."))
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(123, builder.build());
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}