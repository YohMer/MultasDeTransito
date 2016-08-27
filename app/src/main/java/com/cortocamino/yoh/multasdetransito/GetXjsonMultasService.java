/*
 * Copyright (c) 2016 Yohann MERIENNE
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE
 */

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

    static private String TAG = "GetXjsonMultasService";

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
        Utils.log(Log.INFO, TAG, "multas: " + totalMultasTxt);

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
