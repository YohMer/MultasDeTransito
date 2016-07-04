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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String DEBUG_TAG = "DEBUG";
    private static final String ERROR = "error";
    private static final boolean DEBUG_FIRST_START = true;
    private static final boolean MY_DEBUG = true;
    private static Utils utils;
    SharedPreferences sharedPref;
    String key_cedula, defaultCedulaNb;
    String key_id_persona, defaultIdPersona;
    String key_total_multas, defaultMultas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        utils = new Utils(this, this);

        //init:
        key_cedula = getString(R.string.key_cedula);
        key_id_persona = getString(R.string.key_id_persona);
        key_total_multas = getString(R.string.key_total_multas);

        defaultCedulaNb = getString(R.string.default_cedula_nb);
        defaultIdPersona = getString(R.string.default_id_persona);
        defaultMultas = getString(R.string.default_multas);
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);

        if (DEBUG_FIRST_START)
            this.getSharedPreferences("YOUR_PREFS", 0).edit().clear().commit();

        updateViewValues();
    }

    private void updateViewValues(){
        //show cedula Nb:
        String cedulaNb = sharedPref.getString(key_cedula,  defaultCedulaNb);
        ((EditText) findViewById(R.id.cedulaNb)).setText(cedulaNb);

        //show id persona:
        if (MY_DEBUG){
            String idPersona = sharedPref.getString(key_id_persona,  defaultIdPersona);
            ((TextView)findViewById(R.id.debug_id_persona)).setText(idPersona);
        }

        //show total multas:
        String totalStr = sharedPref.getString(key_total_multas,  defaultMultas);
        ((TextView)findViewById(R.id.total_multas_value)).setText(totalStr);
    }

    public void saveCedula(View view){
        EditText cedulaEditText = (EditText) findViewById(R.id.cedulaNb);
        String cedulaNb = cedulaEditText.getText().toString();
        String idPersona;

        //if new cedula
        if (!cedulaNb.equals(sharedPref.getString(key_cedula, defaultCedulaNb))){

            //save cedula and reset id persona and total multas
            utils.saveSharedSTring(key_cedula ,cedulaNb);
            utils.saveSharedSTring(key_id_persona ,defaultIdPersona);
            utils.saveSharedSTring(key_total_multas, defaultMultas);
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
    }

    private void getIdPersona(String cedulaNb){
        String url = "" + getString(R.string.link_to_multas_page_list_begin) +
                //String.format("%010d", cedulaNb) +
                cedulaNb +
                getString(R.string.link_to_multas_page_list_end);

        //todo: try else call again later
        new DownloadIdPersona().execute(url);
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
            //getMultas();
        }
        else{
            //todo: try later
        }
    }
    private class DownloadIdPersona extends AsyncTask<String, Void, String> {
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
            updateViewValues();
            getMultas();
        }
    }

    private void getMultas(){
        String cedula = sharedPref.getString(key_cedula, defaultCedulaNb);
        String idPersona = sharedPref.getString(key_id_persona, defaultIdPersona);
        String time = "" + System.currentTimeMillis();
        String url = String.format(
                getString(R.string.link_to_xjson_multas_list), idPersona, cedula, time);

        if(idPersona.length() < 5){
            Log.d(ERROR, "idPersono.length < 5 can not get multas");
            utils.saveSharedSTring(getString(R.string.key_total_multas), "0");
            return;
        }
        if (MY_DEBUG){
            Log.d(DEBUG_TAG, "--------getMultas----------");
            Log.d(DEBUG_TAG, "cedula: " + cedula);
            Log.d(DEBUG_TAG, "idPersona: " + idPersona);
            Log.d(DEBUG_TAG, "time: " + time);
            Log.d(DEBUG_TAG, "url: " + url);
        }
        new DownloadMultas().execute(url);
    }
    public void getMultas(View view){
        getMultas();
    }
    private void saveMultas(String jsonTxt){
        float total = 0;
        String totalStr;
        int length = 0;
        String buffer = "";
        try{
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

            utils.saveSharedSTring(getString(R.string.key_total_multas), totalStr);
        }catch(JSONException e){

        }


    }
    private class DownloadMultas extends AsyncTask<String, Void, String> {
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
        protected void onPostExecute(String json) {
            saveMultas(json);
            updateViewValues();
        }
    }
}
