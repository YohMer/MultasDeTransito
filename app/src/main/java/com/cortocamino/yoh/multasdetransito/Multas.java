package com.cortocamino.yoh.multasdetransito;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;

/**
 * Created by yoh on 7/13/16.
 */
public class Multas {

    Context mContext;
    private static final String DEBUG_TAG = "DEBUG";
    private static final String ERROR = "error";
    private static final boolean DEBUG_FIRST_START = true;
    private static final boolean MY_DEBUG = true;
    private static Utils utils;
    private SharedPreferences sharedPref;
    private String key_cedula, defaultCedulaNb;
    private String key_id_persona, defaultIdPersona;
    private String key_total_multas, defaultMultas;
    private String key_update_time, default_update_time;
    private String key_full_link_to_xjson_multas_list;
    private boolean NetworkOn = false;
    private boolean ValidatingCedula = false;
    private boolean unvalidCedula = true;
    private boolean IdPersonaFound = false;
    private boolean MultasFound = false;
    private String cedulaNb, idPersona, totalMultas, lastUpdateTime;
    
    public Multas(Context mContext){
        this.mContext = mContext;
        utils = new Utils(mContext);

        //init:
        key_cedula = mContext.getString(R.string.key_cedula);
        key_id_persona = mContext.getString(R.string.key_id_persona);
        key_total_multas = mContext.getString(R.string.key_total_multas);
        key_update_time = mContext.getString(R.string.key_last_update_time);
        key_full_link_to_xjson_multas_list =
                mContext.getString(R.string.key_full_link_to_xjson_multas_list);

        defaultCedulaNb = mContext.getString(R.string.default_cedula_nb);
        defaultIdPersona = mContext.getString(R.string.default_id_persona);
        defaultMultas = mContext.getString(R.string.default_multas);
        default_update_time = mContext.getString(R.string.default_update_time);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);

        cedulaNb = sharedPref.getString(key_cedula,  defaultCedulaNb);
        idPersona = sharedPref.getString(key_id_persona,  defaultIdPersona);
        totalMultas = sharedPref.getString(key_total_multas,  defaultMultas);
        lastUpdateTime = sharedPref.getString(key_update_time, default_update_time);

        //sharedPref = this.getPreferences(Context.MODE_PRIVATE);


        if (DEBUG_FIRST_START)
            mContext.getSharedPreferences("YOUR_PREFS", 0).edit().clear().commit();

    }

    private void resetSharedString(){
        utils.saveShared(key_cedula, defaultCedulaNb);
        utils.saveShared(key_id_persona, defaultIdPersona);
        utils.saveShared(key_total_multas, defaultMultas);
        utils.saveShared(key_update_time, default_update_time);
        utils.saveShared(key_full_link_to_xjson_multas_list, "");
    }

    public boolean isNetworkOn() {
        return NetworkOn;
    }

    public boolean isValidatingCedula() {
        return ValidatingCedula;
    }

    public boolean isUnvalidCedula() {
        return unvalidCedula;
    }

    public boolean isIdPersonaFound() {
        return IdPersonaFound;
    }

    public boolean isMultasFound() {
        return MultasFound;
    }

    public void update(Context mContext){

    }


    public void saveCedula(View view){
        EditText cedulaEditText = (EditText) findViewById(R.id.cedulaNb);
        String cedulaNb = cedulaEditText.getText().toString();

        stateValidatingCedula = true;
        resetSharedString();
        utils.saveShared(key_cedula ,cedulaNb);

        //is cedula valid
        if((cedulaNb.length() == 10) && (Integer.parseInt(cedulaNb) > 0)){
            //find id persona:
            if (utils.isNetworkAvailable()){
                stateNetworkOn = true;
                getIdPersona(cedulaNb);
            }else{
                stateNetworkOn = false;
                //todo: try later
            }
        }
        else{
            stateNetworkOn = true;
            stateValidatingCedula = false;
            stateAccessIdPersona = false;
            updateInfos();
            updateViewValues();
        }
    }


    private void getIdPersona(String cedulaNb){
        String url = "" + getString(R.string.link_to_multas_page_list_begin) +
                cedulaNb +
                getString(R.string.link_to_multas_page_list_end);
        Log.d(DEBUG_TAG, "id persona url: " + url);

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

        //full json link:
        String cedula = sharedPref.getString(key_cedula, defaultCedulaNb);
        String fullLink = String.format(
                getString(R.string.link_to_xjson_multas_list), idPersona, cedula, "%1$s");

        Log.d(DEBUG_TAG, "id persona: " + idPersona);

        stateValidatingCedula = false;
        //save id persona:
        if (Integer.parseInt(idPersona) > 1000){
            stateAccessIdPersona = true;
            utils.saveShared(key_id_persona ,idPersona);
            utils.saveShared(key_full_link_to_xjson_multas_list,fullLink);
            (findViewById(R.id.btn_update)).setVisibility(View.VISIBLE);
        } else {
            stateAccessIdPersona = false;
            utils.saveShared(key_id_persona ,defaultIdPersona);
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
                return null;
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String html) {
            if (html != null){
                saveIdPersona(html);
            } else {
                stateNetworkOn = false;
            }
            updateViewValues();
            updateInfos();
            String idPersona = sharedPref.getString(key_id_persona, defaultIdPersona);
            if (Integer.parseInt(idPersona) > 1000){
                stateAccessIdPersona = true;
                getMultas();
            }
        }
    }

    private void getMultas(){
        String cedula = sharedPref.getString(key_cedula, defaultCedulaNb);
        String idPersona = sharedPref.getString(key_id_persona, defaultIdPersona);
        String time = "" + System.currentTimeMillis();
        String url = String.format(
                getString(R.string.link_to_xjson_multas_list), idPersona, cedula, time);

        stateAccessMultas = false;
        if(Integer.parseInt(idPersona)< 1000){
            Log.d(ERROR, "idPersona < 1000 can not get multas");
            utils.saveShared(getString(R.string.key_total_multas), defaultMultas);
            stateAccessIdPersona = false;
            return;
        }
        if (MY_DEBUG){
            Log.d(DEBUG_TAG, "--------getMultas----------");
            Log.d(DEBUG_TAG, "cedula: " + cedula);
            Log.d(DEBUG_TAG, "idPersona: " + idPersona);
            Log.d(DEBUG_TAG, "time: " + time);
            Log.d(DEBUG_TAG, "url: " + url);
        }
        if (utils.isNetworkAvailable()){ //todo: wifi stopped still say ok
            stateNetworkOn = true;
            new DownloadMultas().execute(url);
        }else{
            stateNetworkOn = false;
            //todo: try later
        }
    }
    public void getMultas(View view){
        (findViewById(R.id.btn_update)).setVisibility(View.INVISIBLE);
        getMultas();
    }
    private void saveMultas(String jsonTxt){
        stateAccessMultas = utils.saveMultas(jsonTxt);
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
            updateInfos();
            (findViewById(R.id.btn_update)).setVisibility(View.VISIBLE);
        }
    }

    public String getCedulaNb() {
        return cedulaNb;
    }

    public String getIdPersona() {
        return idPersona;
    }

    public String getTotalMultas() {
        return totalMultas;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }
}
