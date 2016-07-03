package com.cortocamino.yoh.multasdetransito;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
    private static final String DEBUG_TAG = "HttpExample";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int defaultCedulaNb = Integer.parseInt(getString(R.string.default_cedula_nb));
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        int cedulaNb = sharedPref.getInt("cedula_nb", defaultCedulaNb);

        EditText cedulaEditText = (EditText) findViewById(R.id.cedulaNb);
        String buffer = "" + cedulaNb;
        cedulaEditText.setText(buffer);
    }

    private void saveCedula(View view){
        EditText cedulaEditText = (EditText) findViewById(R.id.cedulaNb);
        int cedulaNb = Integer.parseInt(cedulaEditText.getText().toString());

        saveSharedValue("cedula_nb" ,cedulaNb);

        if (isNetworkAvailable()){
            getIdPersona(cedulaNb);
        }
        else{
            //todo: try later
        }

    }

    private void getIdPersona(int cedulaNb){
        String url = "" + R.string.link_to_multas_page_list_begin + cedulaNb +
                R.string.link_to_multas_page_list_end;

        //todo: try else call again later
        new DownloadWebpageTask().execute(url);
    }

    private void saveIdPersona(String html){
        //extract id persona:
        String preIdPersona = getString(R.string.previous_id_persona);
        int startPosition = html.indexOf(preIdPersona);
        startPosition += preIdPersona.length();
        int endPosition = startPosition + 15;
        String idPersonaStr = html.substring(startPosition, endPosition);
        int idPersona = new Scanner(idPersonaStr).useDelimiter("\\D+").nextInt();

        Log.d(DEBUG_TAG, "id persona: " + idPersona);

        //save id persona:
        saveSharedValue("id_persona" ,idPersona);

    }


    //most of the code here after is from or adapted from:
    //https://developer.android.com/training/basics/network-ops/connecting.html#connection
    private boolean isNetworkAvailable(){
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }


    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String html) {
            saveIdPersona(html);
        }
    }

    // Given a URL, establishes an HttpUrlConnection and retrieves
    // the web page content as a InputStream, which it returns as
    // a string.
    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 50000;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(DEBUG_TAG, "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            return readIt(is, len);

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

    public void saveSharedValue(String key, int value){
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putInt(key ,value);
        editor.apply();
    }

}
