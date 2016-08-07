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
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yoh on 7/3/16.
 */
public class Utils {
    Context mContext;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    public Utils(Context mContext){
        this.mContext = mContext;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = sharedPref.edit();
    }

    public void saveShared(String key, Float value){
        editor.putFloat(key ,value);
        editor.apply();
    }
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
    public boolean isNetworkAvailable(){
        ConnectivityManager connMgr = (ConnectivityManager)
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    // Given a URL, establishes an HttpUrlConnection and retrieves
    // the web page content as a InputStream, which it returns as
    // a string.
    public String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.

        try {
            URL url = new URL(myurl);
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
        if (Defaults.MY_DEBUG){
            Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
        }
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
