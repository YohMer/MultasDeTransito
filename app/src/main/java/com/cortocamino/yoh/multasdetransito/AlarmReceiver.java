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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by yoh on 7/11/16.
 */
public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPref =
                PreferenceManager.getDefaultSharedPreferences(context);
        String key_activity_on = context.getString(R.string.key_activity_on);

        //if activity run do nothing
        if(sharedPref.getBoolean(key_activity_on, false)){
            Utils.debugToast(context, "activity on, no service will run");
            Log.d(Config.DEBUG_TAG, "activity on, no service will run");
            return;
        }

        //verify the internet connection
        Utils utils = new Utils(context);
        if (!utils.isNetworkAvailable()) {
            Log.d(Config.DEBUG_TAG, "no network");
            Utils.debugToast(context, "no network");
            return;
        }
        Log.d(Config.DEBUG_TAG, "url and network present");

        //create the service
        Intent getXjsonMultasService = new Intent(context, GetXjsonMultasService.class);
        context.startService(getXjsonMultasService);
    }
}
