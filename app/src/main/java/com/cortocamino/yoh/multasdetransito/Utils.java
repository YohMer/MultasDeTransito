package com.cortocamino.yoh.multasdetransito;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yoh on 7/3/16.
 */
public class Utils {
    Context mContext;
    Activity activity;

    public Utils(Context mContext){
        this.mContext = mContext;
    }

    public void saveSharedSTring(String key, String value){
        SharedPreferences sharedPref =
                PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString(key ,value);
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
        int len = 20000;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 ); // milliseconds
            conn.setConnectTimeout(15000); // milliseconds
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            is = conn.getInputStream();

            // Convert the InputStream into a string
            return convertStreamToString(is);

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream, int len) throws IOException {
        Reader reader;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    Boolean saveMultas(String jsonTxt){

        try{
            float total = 0;
            String totalStr, buffer;
            int length;

            JSONObject json = new JSONObject(jsonTxt);
            JSONArray rows = json.getJSONArray("rows");
            JSONObject obj;

            length = rows.length();

            for(int i = 0; i < length; i++){
                obj = (JSONObject) rows.get(i);
                buffer = ((JSONArray) obj.get("cell")).getString(16);
                total += Float.parseFloat(buffer);
            }

            totalStr = String.format("%1.2f", total);

            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", new Locale("es-EC"));
            String dateStr = df.format(System.currentTimeMillis());

            this.saveSharedSTring(
                    mContext.getString(R.string.key_total_multas), totalStr);
            this.saveSharedSTring(
                    mContext.getString(R.string.key_last_update_time), dateStr);
            return true;
        }catch(JSONException e){
            return false;
        }
    }
}
