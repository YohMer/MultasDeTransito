package com.cortocamino.yoh.multasdetransito;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by yoh on 7/11/16.
 */
public class DeviceBootReceiver extends BroadcastReceiver {
    int interval = 8000; //(milliseconds)

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
            Intent alarmIntent = new Intent(context, AlarmReceiver.class);
            PendingIntent pendingIntent =
                    PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
            AlarmManager manager =
                    (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                    interval, pendingIntent);
            Toast.makeText(context, "Alarm Set From boot receiver", Toast.LENGTH_SHORT).show();
        }
    }
}
