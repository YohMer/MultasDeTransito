package com.cortocamino.yoh.multasdetransito;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int defaultCedulaNb = Integer.parseInt(getString(R.string.default_cedula_nb));
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        int cedulaNb = sharedPref.getInt("cedula_nb", defaultCedulaNb);

        EditText cedulaEditText = (EditText) findViewById(R.id.cedulaNb);
        cedulaEditText.setText("" + cedulaNb);
    }

    public void saveCedula(View view){
        EditText cedulaEditText = (EditText) findViewById(R.id.cedulaNb);
        int cedulaNb = Integer.parseInt(cedulaEditText.getText().toString());

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putInt("cedula_nb" ,cedulaNb);
        editor.commit();
    }
}
