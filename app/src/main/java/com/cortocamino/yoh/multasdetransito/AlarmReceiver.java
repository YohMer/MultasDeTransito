package com.cortocamino.yoh.multasdetransito;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.Toast;

/**
 * Created by yoh on 7/11/16.
 */
public class AlarmReceiver extends BroadcastReceiver {
    private static final String DEBUG_TAG = "DEBUG";

    @Override
    public void onReceive(Context context, Intent intent) {
        //create the url to the xjson multas file
        String key_full_link_to_xjson_multas_list = context.getString(
                R.string.key_full_link_to_xjson_multas_list);
        SharedPreferences sharedPref =
                PreferenceManager.getDefaultSharedPreferences(context);
        String linkNoTime = sharedPref.getString(key_full_link_to_xjson_multas_list, "");
        String time = "" + System.currentTimeMillis();
        String url = String.format(linkNoTime, time);

        //verify the internet connection
        Utils utils = new Utils(context);
        if ((!utils.isNetworkAvailable()) || (url.equals(""))) {
            Toast.makeText(context, "no url or no network", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(context, "url and network present", Toast.LENGTH_SHORT).show();

        //create the service
        Intent getXjsonMultasService = new Intent(context, GetXjsonMultasService.class);
        getXjsonMultasService.setData(Uri.parse(url));
        context.startService(getXjsonMultasService);
    }
}
