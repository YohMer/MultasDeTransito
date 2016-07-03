package com.cortocamino.yoh.multasdetransito;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String DEBUG_TAG = "HttpExample";
    private static final boolean MY_DEBUG = true;
    private static Utils utils;
    SharedPreferences sharedPref;
    int defaultCedulaNb;
    String key_cedula;
    String key_id_persona;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        utils = new Utils(this, this);

        key_cedula = getString(R.string.key_cedula);
        key_id_persona = getString(R.string.key_id_persona);

        defaultCedulaNb = Integer.parseInt(getString(R.string.default_cedula_nb));
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);

        int cedulaNb = sharedPref.getInt(key_cedula, defaultCedulaNb);

        EditText cedulaEditText = (EditText) findViewById(R.id.cedulaNb);
        String buffer = String.format("%010d", cedulaNb);
        cedulaEditText.setText(buffer);
    }

    public void saveCedula(View view){
        EditText cedulaEditText = (EditText) findViewById(R.id.cedulaNb);
        int cedulaNb = Integer.parseInt(cedulaEditText.getText().toString());

        if ((cedulaNb != sharedPref.getInt(key_cedula, defaultCedulaNb)) ||
                (sharedPref.getInt(key_id_persona, defaultCedulaNb) == -1)){

            utils.saveSharedValue(key_cedula ,cedulaNb);
            utils.saveSharedValue(key_id_persona ,-1);

            if (MY_DEBUG) {
                TextView debug_id_persona = (TextView) findViewById(R.id.debug_id_persona);
                debug_id_persona.setText("" + "??");
            }
            if (utils.isNetworkAvailable()){
                getIdPersona(cedulaNb);
            }
            else{
                //todo: try later
            }
        }
    }

    private void getIdPersona(int cedulaNb){
        String url = "" + getString(R.string.link_to_multas_page_list_begin) +
                String.format("%010d", cedulaNb) +
                getString(R.string.link_to_multas_page_list_end);

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
        int idPersona = utils.extractFirstNb(idPersonaStr);

        Log.d(DEBUG_TAG, "id persona: " + idPersona);

        //save id persona:
        if (idPersona > 0){
            utils.saveSharedValue(key_id_persona ,idPersona);

            if (MY_DEBUG) {
                TextView debug_id_persona = (TextView) findViewById(R.id.debug_id_persona);
                debug_id_persona.setText("" + idPersona);
            }
        }
        else{
            //todo: try later
        }

    }

    public class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return utils.downloadUrl(urls[0]);
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



}
