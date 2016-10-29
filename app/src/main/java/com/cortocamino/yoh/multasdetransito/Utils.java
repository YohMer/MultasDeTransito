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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Utils {
    private final Context mContext;
    private final SharedPreferences sharedPref;
    private final SharedPreferences.Editor editor;

    public Utils(Context mContext){
        this.mContext = mContext;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = sharedPref.edit();
    }

    public void saveShared(String key, Float value){
        editor.putFloat(key ,value);
        editor.apply();
    }
    @SuppressWarnings("unused")
    public void saveShared(String key, Long value){
        editor.putLong(key ,value);
        editor.apply();
    }
    public void saveShared(String key, String value){
        editor.putString(key ,value);
        editor.apply();
    }
    public void saveShared(String key, Boolean value){
        editor.putBoolean(key ,value);
        editor.apply();
    }

    public String extractFirstNbAsString(String s){
        Pattern p = Pattern.compile("(\\d+)(.*)");
        Matcher m = p.matcher(s);
        if (m.find()){
            return m.group(1);
        } else {
            return "";
        }
    }

    //most of the code here after is from or adapted from:
    //https://developer.android.com/training/basics/network-ops/connecting.html#connection
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isNetworkAvailable(){
        ConnectivityManager connMgr = (ConnectivityManager)
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public boolean isNetworkUnAvailable(){
        return !isNetworkAvailable();
    }

    // Given a URL, establishes an HttpUrlConnection and retrieves
    // the web page content as a InputStream, which it returns as
    // a string.
    public String downloadUrl(String myUrl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.

        try {
            URL url = new URL(myUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000); // milliseconds
            conn.setConnectTimeout(15000); // milliseconds
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            conn.getResponseCode();
            is = conn.getInputStream();

            // Convert the InputStream into a string
            return convertStreamToString(is);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    static void debugToast(Context mContext, String msg){
        if (Config.MY_DEBUG){
            Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
        }
    }

    private static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    static void log(@SuppressWarnings("SameParameterValue") int priority, String tag, String msg){
        if(!Config.MY_DEBUG) {
            Crashlytics.log(priority, tag, msg);
        } else if (priority == Log.ASSERT) {
            Log.w(tag, msg);
        } else if (priority == Log.INFO) {
            Log.i(tag, msg);
        } else if (priority == Log.ERROR) {
            Log.e(tag, msg);
        } else if (priority == Log.DEBUG) {
            Log.d(tag, msg);
        } else if (priority == Log.VERBOSE) {
            Log.v(tag, msg);
        } else {
            Log.v(tag, msg + "-- INVALID PRIORITY");
        }
    }

    static void logException(@SuppressWarnings("SameParameterValue") String tag, Exception e){
        if(!Config.MY_DEBUG) {
            Crashlytics.logException(e);
        } else {
            Log.wtf(tag, e);
        }
    }
}
