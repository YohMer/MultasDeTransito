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
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private PendingIntent pendingIntent;
    Utils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        utils = new Utils(this);

        if (Defaults.MY_DEBUG)
            findViewById(R.id.debug_id_persona).setVisibility(View.VISIBLE);

//        if(Multas.isCedulaNbConsistent()){
//            showSoftKeyboard(findViewById(R.id.mainLayout));
//        }
        Multas.init(this);
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
                            Multas.changeCedulaNb(context, editText.getText().toString());

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

        ((TextView)findViewById(R.id.info1)).setText(faultMsg);

        if(faultMsg.equals("")){
            findViewById(R.id.info1).setVisibility(View.INVISIBLE);
        } else {
            findViewById(R.id.info1).setVisibility(View.VISIBLE);
        }
        Toast.makeText(this, faultMsg, Toast.LENGTH_SHORT).show();


        findViewById(R.id.btn_refresh).setVisibility(
                Multas.isCedulaNbConsistent()?View.VISIBLE:View.INVISIBLE);
    }
    private void updateViewValues(){
        //show cedula Nb:
        String cedulaNb = Multas.getCedulaNb();
        if(cedulaNb.equals(getString(R.string.default_cedula_nb))){
            ((EditText) findViewById(R.id.cedulaNb)).setText("");
        } else {
            ((EditText) findViewById(R.id.cedulaNb)).setText(cedulaNb);
        }

        //show id persona:
        if (Defaults.MY_DEBUG){
            String idPersona = Multas.getIdPersona();
            ((TextView)findViewById(R.id.debug_id_persona)).setText(idPersona);
        }

        //show total multas:
        String totalStr = Multas.getTotalMultas();
        ((TextView)findViewById(R.id.total_multas_value)).setText(totalStr);

        //show last update time:
        String time = Multas.getLastUpdateTime();
        ((TextView)findViewById(R.id.date_update)).setText(time);
    }

    public void saveCedula(View view){
        EditText cedulaEditText = (EditText) findViewById(R.id.cedulaNb);
        String cedulaNb = cedulaEditText.getText().toString();
        Multas.changeCedulaNb(this, cedulaNb);

        refresh(view);
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
            return Multas.update(MainActivity.this);
        }

        @Override
        protected void onPostExecute(String faultMsg) {
            updateView(faultMsg);
        }
    }

    public void startAlarm() {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        String key_alarm_interval = getString(R.string.key_alarm_interval);
        SharedPreferences sharedPref =
                PreferenceManager.getDefaultSharedPreferences(this);
        long interval =
                sharedPref.getLong(key_alarm_interval, Defaults.DEFAULT_ALARM_INTERVAL);

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
}
