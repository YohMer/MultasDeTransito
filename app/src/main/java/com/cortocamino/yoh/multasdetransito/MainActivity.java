package com.cortocamino.yoh.multasdetransito;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String DEBUG_TAG = "DEBUG";
    private static final String ERROR = "error";
    private static final boolean DEBUG_FIRST_START = true;
    private static final boolean MY_DEBUG = true;
    private PendingIntent pendingIntent;
    Utils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        utils = new Utils(this);

        Multas.init(this);
        Multas.update(this);

        //init alarm:
        Intent alarmIntent = new Intent(MainActivity.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, 0);
        startAlarm();
        updateViewValues();
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

    private void updateView(String faultMsg){
        updateViewValues();

        ((TextView)findViewById(R.id.info1)).setText(faultMsg);

        if(faultMsg.equals("")){
            findViewById(R.id.info1).setVisibility(View.INVISIBLE);
        } else {
            findViewById(R.id.info1).setVisibility(View.VISIBLE);
        }


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
        if (MY_DEBUG){
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
        int interval = 20000;

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
