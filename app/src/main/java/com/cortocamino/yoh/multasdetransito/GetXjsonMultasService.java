package com.cortocamino.yoh.multasdetransito;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

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
        String key_last_total = getString(R.string.key_last_total);
        Float lastTotal = new Float(sharedPref.getFloat(key_last_total, 0f));

        Multas.update(this);
        String totalMultasTxt = sharedPref.getString(key_total_multas, "0");
        Log.d(DEBUG_TAG, "multas: " + totalMultasTxt);

        Float totalMultas = Float.parseFloat(totalMultasTxt);

        if (!totalMultas.equals(lastTotal)) {
            Toast.makeText(this, "show new notification", Toast.LENGTH_SHORT).show();
            SimpleNotification.call(this, 0, "actualizacion de multas",
                    "nuevo total: " + totalMultasTxt);
        } else {
            Toast.makeText(this, "notification already shown", Toast.LENGTH_SHORT).show();
        }
        utils.saveShared(key_last_total, totalMultas);
    }
}
