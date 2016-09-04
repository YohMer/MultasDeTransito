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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    static private String TAG = "MainActivity";
    private PendingIntent pendingIntent;
    Utils utils;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        utils = new Utils(this);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String key_EULA_accepted = getString(R.string.key_EULA_accepted);

        createSpinner(R.id.spinner_look_for);

        if (Config.MY_DEBUG)
            findViewById(R.id.debug_id_persona).setVisibility(View.VISIBLE);

        MultasPorCedula.init(this);

        if (!(sharedPref.getBoolean(key_EULA_accepted, false))){
            showEULA();
        }

        new updateAll().execute("");

        //init alarm:
        Intent alarmIntent = new Intent(MainActivity.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, 0);
        startAlarm();

        startListenerKeyboard(this, (EditText) findViewById(R.id.cedulaNb));
    }

    @Override
    protected void onStop() {
        super.onStop();
        utils.saveShared(getString(R.string.key_activity_on), false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        utils.saveShared(getString(R.string.key_activity_on), true);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        if (id == 0){
            findViewById(R.id.placaNb1).setVisibility(View.GONE);
            findViewById(R.id.placaNb2).setVisibility(View.GONE);
            findViewById(R.id.cedulaNb).setVisibility(View.VISIBLE);

        } else if (id == 1){
            findViewById(R.id.cedulaNb).setVisibility(View.GONE);
            findViewById(R.id.placaNb1).setVisibility(View.VISIBLE);
            findViewById(R.id.placaNb2).setVisibility(View.VISIBLE);

        }
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    public void createSpinner(int id){
        Spinner spinner = (Spinner) findViewById(id);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.look_for_array, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }
    public void showSoftKeyboard(View view){
        if(view.requestFocus()){
            InputMethodManager imm =(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view,InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void hideSoftKeyboard(View view){
        InputMethodManager imm =(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void startListenerKeyboard(final Context context, final EditText editText){
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    boolean result =
                            MultasPorCedula.changeCedulaNb(context, editText.getText().toString());

                    if (result){
                        hideSoftKeyboard(view);
                    }
                    handled = true;

                    refresh(view);
                }
                return handled;
            }
        });
    }

    private void updateView(String faultMsg){
        updateViewValues();

        //show fault msg only if there is a fault:
        ((TextView)findViewById(R.id.info1)).setText(faultMsg);
        if (faultMsg.equals(getString(R.string.done))){
            findViewById(R.id.info1).setVisibility(View.INVISIBLE);
        } else {
            Crashlytics.log(Log.INFO, TAG, "msg on screen: " + faultMsg);
            findViewById(R.id.info1).setVisibility(View.VISIBLE);
        }

        //remove fault msg after 5s
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.info1).setVisibility(View.INVISIBLE);
            }
        }, 5000);

        //show actualizar only if cedula can be valid:
        findViewById(R.id.btn_refresh).setVisibility(
                MultasPorCedula.isCedulaNbConsistent()?View.VISIBLE:View.INVISIBLE);
    }
    private void updateViewValues(){
        //show cedula Nb:
        String cedulaNb = MultasPorCedula.getCedulaNb();
        if(cedulaNb.equals(getString(R.string.default_cedula_nb))){
            ((EditText) findViewById(R.id.cedulaNb)).setText("");
        } else {
            ((EditText) findViewById(R.id.cedulaNb)).setText(cedulaNb);
        }

        //show id persona:
        if (Config.MY_DEBUG){
            String idPersona = MultasPorCedula.getIdPersona();
            ((TextView)findViewById(R.id.debug_id_persona)).setText(idPersona);
        }

        //show total multas:
        String totalStr = MultasPorCedula.getTotalMultas();
        ((TextView)findViewById(R.id.total_multas_value)).setText(totalStr);

        //show last update time:
        String time = MultasPorCedula.getLastUpdateTime();
        ((TextView)findViewById(R.id.date_update)).setText(time);
    }

    public void refresh(View view){
        findViewById(R.id.info1).setVisibility(View.INVISIBLE);
        (findViewById(R.id.btn_refresh)).setVisibility(View.INVISIBLE);
        new updateAll().execute("");
    }
    public void goToGovWebSite(View view){
        Uri uri = Uri.parse(getString(R.string.link_to_gov));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private class updateAll extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String empty[]) {
            //connection to the server to retrieve multas value
            return MultasPorCedula.getMultasFromCedula(MainActivity.this);
        }

        @Override
        protected void onPostExecute(String faultMsg) {
            updateView(faultMsg);
        }
    }

    public void startAlarm() {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        String key_alarm_interval = getString(R.string.key_alarm_interval);
        long interval =
                sharedPref.getLong(key_alarm_interval, Config.DEFAULT_ALARM_INTERVAL);

        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                interval, pendingIntent);
        Utils.debugToast(this,"Alarm Set from startAlarm");

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

    public void showEULA(){
        Intent intent = new Intent(this, EulaActivity.class);
        startActivity(intent);
    }
}
