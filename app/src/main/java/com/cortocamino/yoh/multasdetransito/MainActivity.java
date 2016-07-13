package com.cortocamino.yoh.multasdetransito;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

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
    String key_update_time, default_update_time;
    String key_link_to_xjson_multas_list;
    boolean stateNetworkOn = false;
    boolean stateValidatingCedula = false;
    boolean stateAccessIdPersona = false;
    boolean stateAccessMultas = false;
    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        utils = new Utils(this, this);

        //init:
        key_cedula = getString(R.string.key_cedula);
        key_id_persona = getString(R.string.key_id_persona);
        key_total_multas = getString(R.string.key_total_multas);
        key_update_time = getString(R.string.key_last_update_time);
        key_link_to_xjson_multas_list = getString(R.string.key_link_to_xjson_multas_list);

        defaultCedulaNb = getString(R.string.default_cedula_nb);
        defaultIdPersona = getString(R.string.default_id_persona);
        defaultMultas = getString(R.string.default_multas);
        default_update_time = getString(R.string.default_update_time);

        //sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        //init alarm:
        Intent alarmIntent = new Intent(MainActivity.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, 0);
        startAlarm();

        if (DEBUG_FIRST_START)
            this.getSharedPreferences("YOUR_PREFS", 0).edit().clear().commit();

        resetSharedString();
        updateViewValues();
    }

    private void updateViewValues(){
        //show cedula Nb:
        String cedulaNb = sharedPref.getString(key_cedula,  defaultCedulaNb);
        if(cedulaNb.equals(defaultCedulaNb)){
            ((EditText) findViewById(R.id.cedulaNb)).setText("");
        } else {
            ((EditText) findViewById(R.id.cedulaNb)).setText(cedulaNb);
        }


        //show id persona:
        if (MY_DEBUG){
            String idPersona = sharedPref.getString(key_id_persona,  defaultIdPersona);
            ((TextView)findViewById(R.id.debug_id_persona)).setText(idPersona);
        }

        //show total multas:
        String totalStr = sharedPref.getString(key_total_multas,  defaultMultas);
        ((TextView)findViewById(R.id.total_multas_value)).setText(totalStr);

        //show last update time:
        String time = sharedPref.getString(key_update_time, default_update_time);
        ((TextView)findViewById(R.id.date_update)).setText(time);
    }

    private void updateInfo1(){
        //reset message:
        ((TextView)findViewById(R.id.info1)).setText("");

        //get info:
        String idPersona = sharedPref.getString(key_id_persona,  defaultIdPersona);
        if (!idPersona.equals(defaultIdPersona))
            stateAccessIdPersona = true;

        if (!stateNetworkOn){
            ((TextView)findViewById(R.id.info1)).setText(R.string.no_connection);
            return;
        }
        if (stateValidatingCedula){
            ((TextView)findViewById(R.id.info1)).setText(R.string.msg_validating_cedula);
            return;
        }
        if (!stateAccessIdPersona){
            ((TextView)findViewById(R.id.info1)).setText(R.string.msg_cedula_not_valid);
            return;
        }
        if (!stateAccessMultas){
            ((TextView)findViewById(R.id.info1)).setText(R.string.msg_multas_not_accessible);
            return;
        }
    }

    public void saveCedula(View view){
        EditText cedulaEditText = (EditText) findViewById(R.id.cedulaNb);
        String cedulaNb = cedulaEditText.getText().toString();

        stateValidatingCedula = true;
        resetSharedString();
        utils.saveSharedSTring(key_cedula ,cedulaNb);

        //is id persona valid
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
            updateInfo1();
            updateViewValues();
        }
    }

    private void resetSharedString(){
        utils.saveSharedSTring(key_cedula, defaultCedulaNb);
        utils.saveSharedSTring(key_id_persona, defaultIdPersona);
        utils.saveSharedSTring(key_total_multas, defaultMultas);
        utils.saveSharedSTring(key_update_time, default_update_time);

        (findViewById(R.id.btn_update)).setVisibility(View.INVISIBLE);
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

        Log.d(DEBUG_TAG, "id persona: " + idPersona);

        stateValidatingCedula = false;
        //save id persona:
        if (Integer.parseInt(idPersona) > 1000){
            stateAccessIdPersona = true;
            utils.saveSharedSTring(key_id_persona ,idPersona);
            utils.saveSharedSTring(key_link_to_xjson_multas_list ,idPersona);
            (findViewById(R.id.btn_update)).setVisibility(View.VISIBLE);
        } else {
            stateAccessIdPersona = false;
            utils.saveSharedSTring(key_id_persona ,defaultIdPersona);
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
            updateInfo1();
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
            utils.saveSharedSTring(getString(R.string.key_total_multas), defaultMultas);
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

            utils.saveSharedSTring(getString(R.string.key_total_multas), totalStr);
            utils.saveSharedSTring(getString(R.string.key_last_update_time), dateStr);
            stateAccessMultas = true;
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
            updateInfo1();
            (findViewById(R.id.btn_update)).setVisibility(View.VISIBLE);
        }
    }

    public void startAlarm() {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int interval = 10000;

        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                interval, pendingIntent);
        Toast.makeText(this, "Alarm Set from startAlarm", Toast.LENGTH_SHORT).show();

        //enable the schedule alarm
        ComponentName alarmReceiver = new ComponentName(MainActivity.this,
                AlarmReceiver.class);
        PackageManager pmAR = MainActivity.this.getPackageManager();

        pmAR.setComponentEnabledSetting(alarmReceiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        //enable the boot receiver
        ComponentName bootReceiver = new ComponentName(MainActivity.this,
                AlarmReceiver.class);
        PackageManager pmBR = MainActivity.this.getPackageManager();

        pmBR.setComponentEnabledSetting(bootReceiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }
}
