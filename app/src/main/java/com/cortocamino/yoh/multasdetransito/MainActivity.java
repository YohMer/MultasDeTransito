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
    private static final boolean DEBUG_FIRST_START = true;
    private static final boolean MY_DEBUG = true;
    private static Utils utils;
    SharedPreferences sharedPref;
    String key_cedula, defaultCedulaNb;
    String key_id_persona, defaultIdPersona;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        utils = new Utils(this, this);

        key_cedula = getString(R.string.key_cedula);
        key_id_persona = getString(R.string.key_id_persona);

        defaultCedulaNb = getString(R.string.default_cedula_nb);
        defaultIdPersona = getString(R.string.default_id_persona);
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);

        if (DEBUG_FIRST_START)
            this.getSharedPreferences("YOUR_PREFS", 0).edit().clear().commit();

        String cedulaNb = sharedPref.getString(key_cedula,  defaultCedulaNb);

        //Show cedula Nb:
        ((EditText) findViewById(R.id.cedulaNb)).setText(cedulaNb);
    }

    public void saveCedula(View view){
        EditText cedulaEditText = (EditText) findViewById(R.id.cedulaNb);
        String cedulaNb = cedulaEditText.getText().toString();
        String idPersona;

        //if new cedula
        if (!cedulaNb.equals(sharedPref.getString(key_cedula, defaultCedulaNb))){

            //save cedula and reset id persona
            utils.saveSharedSTring(key_cedula ,cedulaNb);
            utils.saveSharedSTring(key_id_persona ,defaultIdPersona);
        }

        //if id persona missing
        idPersona = sharedPref.getString(key_id_persona, defaultIdPersona);
        if (idPersona.equals(defaultIdPersona)){

            if (utils.isNetworkAvailable()){
                getIdPersona(cedulaNb);
            }
            else{
                //todo: try later
            }
        }
        if (MY_DEBUG) {
            //show idPersona value on screen
            ((TextView) findViewById(R.id.debug_id_persona)).setText(idPersona);
        }
    }

    private void getIdPersona(String cedulaNb){
        String url = "" + getString(R.string.link_to_multas_page_list_begin) +
                //String.format("%010d", cedulaNb) +
                cedulaNb +
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
        String idPersona = utils.extractFirstNbAsString(idPersonaStr);

        Log.d(DEBUG_TAG, "id persona: " + idPersona);

        //save id persona:
        if (idPersona != ""){
            utils.saveSharedSTring(key_id_persona ,idPersona);
        }
        else{
            //todo: try later
        }
        if (MY_DEBUG) {
            TextView debug_id_persona = (TextView) findViewById(R.id.debug_id_persona);
            debug_id_persona.setText("" + idPersona);
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
