package com.cortocamino.yoh.multasdetransito;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by yoh on 7/11/16.
 */
public class AlarmReceiver extends BroadcastReceiver {
    private static final String DEBUG_TAG = "DEBUG";

    @Override
    public void onReceive(Context context, Intent intent) {
        String key_full_link_to_xjson_multas_list = context.getString(
                R.string.key_full_link_to_xjson_multas_list);
        String key_total_multas = context.getString(R.string.key_total_multas);
        SharedPreferences sharedPref =
                PreferenceManager.getDefaultSharedPreferences(context);
        Utils utils = new Utils(context);

        String linkNoTime = sharedPref.getString(key_full_link_to_xjson_multas_list, "");
        String time = "" + System.currentTimeMillis();
        String url = String.format(linkNoTime, time);

        String xjsonTxt = "";

        if ((!utils.isNetworkAvailable()) || (url.equals(""))) {
            Toast.makeText(context, "no url or no network", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            xjsonTxt = utils.downloadUrl(url);
            if (utils.saveMultas(xjsonTxt)){

                Toast.makeText(context,
                        "multas: " + sharedPref.getString(key_total_multas, ""),
                        Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(context, "I failed", Toast.LENGTH_SHORT).show();
            }


        } catch (IOException e) {
            Log.d(DEBUG_TAG, "Unable to retrieve web page. URL may be invalid.");
            Toast.makeText(context, "I failed (Exception)", Toast.LENGTH_SHORT).show();
        }




    }
}
