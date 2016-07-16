package com.cortocamino.yoh.multasdetransito;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by yoh on 7/11/16.
 */
public class AlarmReceiver extends BroadcastReceiver {
    private static final String DEBUG_TAG = "DEBUG";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPref =
                PreferenceManager.getDefaultSharedPreferences(context);
        String key_activity_on = context.getString(R.string.key_activity_on);

        //if activity run do nothing
        if(sharedPref.getBoolean(key_activity_on, false)){
            Toast.makeText(context, "activity on, no service will run", Toast.LENGTH_SHORT).show();
            Log.d(Defaults.DEBUG_TAG, "activity on, no service will run");
            return;
        }

        //verify the internet connection
        Utils utils = new Utils(context);
        if (!utils.isNetworkAvailable()) {
            Log.d(Defaults.DEBUG_TAG, "no url or no network");
            Toast.makeText(context, "no url or no network", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(Defaults.DEBUG_TAG, "url and network present");

        //create the service
        Intent getXjsonMultasService = new Intent(context, GetXjsonMultasService.class);
        context.startService(getXjsonMultasService);
    }
}
