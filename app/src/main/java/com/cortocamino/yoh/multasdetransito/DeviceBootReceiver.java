package com.cortocamino.yoh.multasdetransito;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

/**
 * Created by yoh on 7/11/16.
 */
public class DeviceBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context mContext, Intent intent) {
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
            Intent alarmIntent = new Intent(mContext, AlarmReceiver.class);

            PendingIntent pendingIntent =
                    PendingIntent.getBroadcast(mContext, 0, alarmIntent, 0);
            AlarmManager manager =
                    (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

            SharedPreferences sharedPref =
                    PreferenceManager.getDefaultSharedPreferences(mContext);

            String key_alarm_interval = mContext.getString(R.string.key_alarm_interval);
            long interval =
                    sharedPref.getLong(key_alarm_interval, Defaults.defaultAlarmInterval);

            manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                    interval, pendingIntent);

            Toast.makeText(mContext, "Alarm Set From boot receiver", Toast.LENGTH_SHORT).show();
        }
    }
}
