package com.cortocamino.yoh.multasdetransito;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

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
        String xjsonTxt;
        Uri url = workIntent.getData();
        String urlTxt = url.toString();

        try {
            xjsonTxt = utils.downloadUrl(urlTxt);
            if (utils.saveMultas(xjsonTxt)){
                Log.d(DEBUG_TAG, "multas: " + sharedPref.getString(key_total_multas, ""));
            }else{
                Log.d(DEBUG_TAG, "multas could not be saved");
            }

        } catch (IOException e) {
            Log.d(DEBUG_TAG, "Unable to retrieve web page. URL may be invalid.");
            Toast.makeText(this, "I failed (Exception)", Toast.LENGTH_SHORT).show();
        }
    }
}
