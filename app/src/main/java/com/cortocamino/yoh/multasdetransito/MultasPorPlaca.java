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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;

class MultasPorPlaca extends MultasPorNb{

    private final static String TAG = "MultasPorPlaca";

    private static Utils utils;
    private static SharedPreferences sharedPref;

    private static String defaultPlacaNb1;
    private static String defaultPlacaNb2;
    private static String default_total_multas;
    private static String default_update_time;

    private static String key_placa_nb1;
    private static String key_placa_nb2;
    private static String key_placa_nb_consistent;
    private static String key_total_multas;
    private static String key_last_total;
    private static String key_update_time;
    private static String link_to_multas_page_list;

    private static boolean initDone = false;

    public static void init(Context mContext) {
        utils = new Utils(mContext);

        //init:
        sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        defaultPlacaNb1 = mContext.getString(R.string.default_placa_nb1);
        defaultPlacaNb2 = mContext.getString(R.string.default_placa_nb2);
        default_total_multas = mContext.getString(R.string.default_total_multas);
        default_update_time = mContext.getString(R.string.default_update_time);

        key_placa_nb1 = mContext.getString(R.string.key_placa_nb1_saved);
        key_placa_nb2 = mContext.getString(R.string.key_placa_nb2_saved);
        key_placa_nb_consistent = mContext.getString(R.string.key_placa_valid);
        key_total_multas = mContext.getString(R.string.key_total_multas);
        key_last_total = mContext.getString(R.string.key_last_total);
        key_update_time = mContext.getString(R.string.key_last_update_time);
        link_to_multas_page_list = mContext.getString(R.string.link_to_multas_page_list);
        @SuppressWarnings("UnusedAssignment") String key_EULA_accepted = mContext.getString(R.string.key_EULA_accepted);

        initDone = true;

        if (Config.DEBUG_FIRST_START) {
            PreferenceManager.getDefaultSharedPreferences(mContext).edit().clear().apply();
            utils.saveShared(key_EULA_accepted, false);
        }
    }

    @NonNull
    public static Boolean changePlacaNb(Context mContext, String placaNb1,
                                        String placaNb2) {
        Utils utils = new Utils(mContext);

        if ((placaNb1.matches("[a-zA-Z]+")) && (placaNb2.matches("[0-9]+"))) {
            utils.saveShared(key_placa_nb_consistent, true);

            String placaNbSaved = getPlacaNb();
            String newPlacaNb = placaNb1 + placaNb2;
            //is placa value new
            if (!newPlacaNb.equals(placaNbSaved)) {
                utils.saveShared(key_placa_nb1, placaNb1);
                utils.saveShared(key_placa_nb2, placaNb2);
                resetAllSharedButPlacaNb();
            }
            return true;

        } else {
            resetAllShared();
            return false;
        }
    }

    @Nullable
    public static String getMultasFromPlaca(Context mContext) {
        if (!isInitDone()) {
            return null; //todo specific msg error (must also be done for la cedula)
        }

        if (!isPlacaNbConsistent()) {
            return mContext.getString(R.string.msg_placa_not_valid);
        }

        String placaNb = getPlacaNb();

        //full json link:
        String multasUrl = String.format(
                mContext.getString(R.string.link_to_xjson_multas_list),
                "P", "", "", "", placaNb, "PLACA", System.currentTimeMillis());

        if (utils.isNetworkUnAvailable()) {
            return mContext.getString(R.string.no_internet_connection);
        }

        String json;
        try {
            json = utils.downloadUrl(multasUrl);
        } catch (Exception e) {
            return mContext.getString(R.string.no_internet_connection);
        }

        try {
            saveMultas(mContext, json);
        } catch (JSONException e) {
            Utils.logException(TAG, e);
            return mContext.getString(R.string.msg_json_not_valid);
        }

        return mContext.getString(R.string.done);
    }

    private static boolean isInitDone() {
        return initDone;
    }

    public static boolean isPlacaNbConsistent() {
        return sharedPref.getBoolean(key_placa_nb_consistent, false);
    }

    public static String getPlacaNb() {
        return sharedPref.getString(key_placa_nb1, defaultPlacaNb1) +
                sharedPref.getString(key_placa_nb2, defaultPlacaNb1);
    }

    public static String getTotalMultas() {
        return sharedPref.getString(key_total_multas, default_total_multas);
    }

    public static String getLastUpdateTime() {
        return sharedPref.getString(key_update_time, default_update_time);
    }

    private static void resetAllSharedButPlacaNb() {
        utils.saveShared(key_total_multas, default_total_multas);
        utils.saveShared(key_update_time, default_update_time);
        utils.saveShared(key_last_total, 0.0f);
    }

    private static void resetAllShared() {
        utils.saveShared(key_placa_nb1, defaultPlacaNb1);
        utils.saveShared(key_placa_nb1, defaultPlacaNb2);
        utils.saveShared(key_placa_nb_consistent, false);
        resetAllSharedButPlacaNb();
    }
}

