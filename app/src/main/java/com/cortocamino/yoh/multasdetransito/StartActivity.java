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

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class StartActivity extends Activity {
    SharedPreferences sharedPref;
    String key_EULA_accepted;
    private static Utils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String key_EULA_accepted = getString(R.string.key_EULA_accepted);
        utils = new Utils(this);

        if (Config.DEBUG_FIRST_START){
            PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply();
            utils.saveShared(key_EULA_accepted, false);
        }

        if (!(sharedPref.getBoolean(key_EULA_accepted, false))){
            showEULA();
        }

        //if no password registered:
        TextView askPw2View = (TextView) findViewById(R.id.ask_password_2);
        if ( true ){
            TextView askPwView = (TextView) findViewById(R.id.ask_password);
            askPwView.setText(R.string.ask_for_new_password);
            askPw2View.setVisibility(View.INVISIBLE);
        } else {
            askPw2View.setVisibility(View.VISIBLE);
        }
    }

    public void showEULA(){
        Intent intent = new Intent(this, EulaActivity.class);
        startActivity(intent);
    }

    public void checkPassword(View view){
        String p1 = ((EditText) findViewById(R.id.p1)).getText().toString();
        String p2 = ((EditText) findViewById(R.id.p2)).getText().toString();

        if (p1.equals(p2)){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);

            finish();
        } else {
            String msg = getString(R.string.passwords_no_match);
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }
    }
}
