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
    static private final String TAG = "MainActivity";
    private PendingIntent pendingIntent;
    private Utils utils;
    private SharedPreferences sharedPref;
    private long currentView;

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
        MultasPorPlaca.init(this);

        if (!(sharedPref.getBoolean(key_EULA_accepted, false))) {
            showEULA();
        }

        updateAll();

        //init alarm:
        Intent alarmIntent = new Intent(MainActivity.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, 0);
        startAlarm();

        startListenerCedulaKeyboard(this, (EditText) findViewById(R.id.cedulaNb));
        startListenerPlacaKeyboard2(this,(EditText) findViewById(R.id.placaNb2));
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
        currentView = id;
        if (id == 0) {
            showCedula();
        } else if (id == 1) {
            showPlaca();
        }
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    private void showCedula(){
        updateViewCedulaValues();
        findViewById(R.id.placaNb1).setVisibility(View.GONE);
        findViewById(R.id.placaNb2).setVisibility(View.GONE);
        findViewById(R.id.cedulaNb).setVisibility(View.VISIBLE);
    }

    private void showPlaca(){
        updateViewPlacaValues();
        findViewById(R.id.cedulaNb).setVisibility(View.GONE);
        findViewById(R.id.placaNb1).setVisibility(View.VISIBLE);
        findViewById(R.id.placaNb2).setVisibility(View.VISIBLE);
    }

    private void createSpinner(@SuppressWarnings("SameParameterValue") int id) {
        Spinner spinner = (Spinner) findViewById(id);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.look_for_array, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    public void showSoftKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideSoftKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void startListenerCedulaKeyboard(final Context context, final EditText editText) {
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    String cedula= editText.getText().toString();

                    if (User.isCedulaNbConsistent(cedula)) {
                        hideSoftKeyboard(view);
                    }
                    handled = true;

                    new updateCedula().execute(cedula);
                }
                return handled;
            }
        });
    }

    private void startListenerPlacaKeyboard2(final Context context, final EditText editText2) {
        final EditText editText1 = (EditText) findViewById(R.id.placaNb1);
        editText2.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    boolean result =
                            MultasPorPlaca.changePlacaNb(context,
                                    editText1.getText().toString(),
                                    editText2.getText().toString());

                    if (result) {
                        hideSoftKeyboard(view);
                    }
                    handled = true;

                    refresh(view);
                }
                return handled;
            }
        });
    }

    private void updateView(String faultMsg) {
        //show fault msg only if there is a fault:
        ((TextView) findViewById(R.id.info1)).setText(faultMsg);
        if (faultMsg.equals(getString(R.string.done))) {
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

        //show actualizar only if cédula can be valid:
        findViewById(R.id.btn_refresh).setVisibility(
                MultasPorCedula.isCedulaNbConsistent() ? View.VISIBLE : View.INVISIBLE);
    }

    private void updateViewCedulaValues() {
        //show cédula Nb:
        String cedulaNb = MultasPorCedula.getCedulaNb();
        if (cedulaNb.equals(getString(R.string.default_cedula_nb))) {
            ((EditText) findViewById(R.id.cedulaNb)).setText("");
        } else { //todo: keep only the else part
            ((EditText) findViewById(R.id.cedulaNb)).setText(cedulaNb);
        }

        //show id persona:
        if (Config.MY_DEBUG) {
            String idPersona = MultasPorCedula.getIdPersona();
            ((TextView) findViewById(R.id.debug_id_persona)).setText(idPersona);
        }

        //show total multas:
        String totalStr = MultasPorCedula.getTotalMultas();
        ((TextView) findViewById(R.id.total_multas_value)).setText(totalStr);

        //show last update time:
        String time = MultasPorCedula.getLastUpdateTime();
        ((TextView) findViewById(R.id.date_update)).setText(time);
    }

    private void updateViewPlacaValues() {
        //show cédula Nb:
        String placaNb1 = MultasPorPlaca.getPlacaNb1();
        String placaNb2 = MultasPorPlaca.getPlacaNb2();
        if (!(placaNb1.equals(getString(R.string.default_placa_nb1))) ||
                (!(placaNb2.equals(getString(R.string.default_placa_nb2))))){
            ((EditText) findViewById(R.id.placaNb1)).setText(placaNb1);
            ((EditText) findViewById(R.id.placaNb2)).setText(placaNb2);
        }

        //show total multas:
        String totalStr = MultasPorPlaca.getTotalMultas();
        ((TextView) findViewById(R.id.total_multas_value)).setText(totalStr);

        //show last update time:
        String time = MultasPorPlaca.getLastUpdateTime();
        ((TextView) findViewById(R.id.date_update)).setText(time);
    }

    public void refresh(@SuppressWarnings("UnusedParameters") View view) {
        findViewById(R.id.info1).setVisibility(View.INVISIBLE);
        (findViewById(R.id.btn_refresh)).setVisibility(View.INVISIBLE);
        if ( currentView == 0){
            updateCedulaF();
        }else if ( currentView == 1){
            //todo: get placa number
            new updatePlaca().execute("");
        }
    }

    public void goToGovWebSite(@SuppressWarnings("UnusedParameters") View view) {
        Uri uri = Uri.parse(getString(R.string.link_to_gov));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private void startAlarm() {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        String key_alarm_interval = getString(R.string.key_alarm_interval);
        long interval =
                sharedPref.getLong(key_alarm_interval, Config.DEFAULT_ALARM_INTERVAL);

        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                interval, pendingIntent);
        Utils.debugToast(this, "Alarm Set from startAlarm");

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

    private void showEULA() {
        Intent intent = new Intent(this, EulaActivity.class);
        startActivity(intent);
    }

    private class updateCedula extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String arg[]) {
            //connection to the server to retrieve multas value
            User.init(MainActivity.this);
            return User.addUserByCedula(arg[0]);
        }

        @Override
        protected void onPostExecute(String faultMsg) {
            updateViewCedulaValues();
            updateView(faultMsg);
        }
    }

    private class updatePlaca extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String empty[]) {
            //connection to the server to retrieve multas value
            return MultasPorPlaca.getMultasFromPlaca(MainActivity.this);
        }

        @Override
        protected void onPostExecute(String faultMsg) {
            updateViewPlacaValues();
            updateView(faultMsg);
        }
    }

    private void updateCedulaF(){
        EditText editTextCed = (EditText) findViewById(R.id.cedulaNb);
        String cedula = editTextCed.getText().toString();
        new updateCedula().execute(cedula);
    }
    private void updateAll(){
        //cedula must be second because it s the one to be shown at first
        new updatePlaca().execute("");
        updateCedulaF();
    }
}
