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
        Float lastTotal = sharedPref.getFloat(key_last_total, 0f);

        Multas.update(this);
        String totalMultasTxt = sharedPref.getString(key_total_multas, "0");
        Log.d(Defaults.DEBUG_TAG, "multas: " + totalMultasTxt);

        Float totalMultas = Float.parseFloat(totalMultasTxt);

        if (!totalMultas.equals(lastTotal)) {
            Utils.debugToast(this, "show new notification");
            new SimpleNotificationBuilder(this, 0, "actualizacion de multas",
                    "nuevo total: " + totalMultasTxt);
        } else {
            Utils.debugToast(this, "notification already shown");
        }
        utils.saveShared(key_last_total, totalMultas);
    }
}
