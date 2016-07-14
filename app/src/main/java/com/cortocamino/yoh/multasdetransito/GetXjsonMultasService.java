package com.cortocamino.yoh.multasdetransito;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by yoh on 7/13/16.
 */
public class GetXjsonMultasService extends IntentService {
    private static final String DEBUG_TAG = "DEBUG";

    public GetXjsonMultasService() {
        super("GetXjsonMultasService");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        Utils utils = new Utils(this);
        SharedPreferences sharedPref =
                PreferenceManager.getDefaultSharedPreferences(this);
        String key_total_multas = getString(R.string.key_total_multas);

        Multas.update(this);
        Log.d(DEBUG_TAG, "multas: " + sharedPref.getString(key_total_multas, ""));

    }
}
