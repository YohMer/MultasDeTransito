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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by yoh on 7/13/16.
 */
public class MultasPorCedula {

    final static String TAG = "MultasPorCedula";

    private static Utils utils;
    private static SharedPreferences sharedPref;

    private static String defaultCedulaNb;
    //private static boolean cedulaNbValid;
    private static String defaultIdPersona;
    //private static boolean idPersonaValid;
    private static String default_total_multas;
    private static String default_update_time;

    private static String key_cedula;
    private static String key_cedula_nb_consistent;
    private static String key_id_persona;
    private static String key_id_persona_validated;
    private static String key_total_multas;
    private static String key_last_total;
    private static String key_update_time;
    private static String link_to_multas_page_list;

    private static boolean initDone = false;
    
    public static void init(Context mContext){
        utils = new Utils(mContext);

        //init:
        sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        defaultCedulaNb = mContext.getString(R.string.default_cedula_nb);
        defaultIdPersona = mContext.getString(R.string.default_id_persona);
        default_total_multas = mContext.getString(R.string.default_total_multas);
        default_update_time = mContext.getString(R.string.default_update_time);

        key_cedula = mContext.getString(R.string.key_cedula);
        key_cedula_nb_consistent = mContext.getString(R.string.key_cedula_not_valid);
        key_id_persona = mContext.getString(R.string.key_id_persona);
        key_id_persona_validated = mContext.getString(R.string.key_id_persona_validated);
        key_total_multas = mContext.getString(R.string.key_total_multas);
        key_last_total = mContext.getString(R.string.key_last_total);
        key_update_time = mContext.getString(R.string.key_last_update_time);
        link_to_multas_page_list = mContext.getString(R.string.link_to_multas_page_list);
        String key_EULA_accepted = mContext.getString(R.string.key_EULA_accepted);

        initDone = true;

        if (Config.DEBUG_FIRST_START){
            PreferenceManager.getDefaultSharedPreferences(mContext).edit().clear().apply();
            utils.saveShared(key_EULA_accepted, false);
        }
    }

    public static Boolean changeCedulaNb(Context mcontext, String cedulaNb){
        Utils utils = new Utils(mcontext);
        
        if((cedulaNb.length() == 10) && (Double.parseDouble(cedulaNb) > 0)) {
            utils.saveShared(key_cedula_nb_consistent, true);

            String cedulaNbSaved = sharedPref.getString(key_cedula,  defaultIdPersona);
            //is cedula value new
            if(!cedulaNb.equals(cedulaNbSaved)){
                utils.saveShared(key_cedula, cedulaNb);
                resetAllSharedButCedulaNb();
            }
            return true;
            
        }else{
            resetAllShared();
            return false;
        }
    }
    public static String getMultasFromCedula(Context mContext){
        if (!isInitDone()){
            return null;
        }

        if(!isCedulaNbConsistent()){
            return mContext.getString(R.string.msg_cedula_not_valid);
        }

        String idPersona;
        String cedulaNb = sharedPref.getString(key_cedula,  defaultCedulaNb);

        //is cedula validated? (if id persona is validated so cedula nb)
        if(isIdPersonaValidated()) {
            idPersona = sharedPref.getString(key_id_persona, defaultIdPersona);
        }else{
            //find id cedula
            if (!utils.isNetworkAvailable()){
                return mContext.getString(R.string.no_internet_connection);
            }

            String url = String.format(link_to_multas_page_list, cedulaNb);
            String html;
            try{
                html = utils.downloadUrl(url);
            } catch (IOException e){
                return mContext.getString(R.string.no_server_connection);
            }

            try{
                idPersona = extractIdPersona(mContext, html);
            } catch (WrongIdPersonaException e) {
                return e.getMessage();
            }
        }

        //full json link:
        String multasUrl = String.format(
                mContext.getString(R.string.link_to_xjson_multas_list),
                idPersona, cedulaNb, System.currentTimeMillis());

        if (!utils.isNetworkAvailable()){
            return mContext.getString(R.string.no_internet_connection);
        }

        String json;
        try{
            json = utils.downloadUrl(multasUrl);
        }
        catch (Exception e){
            return mContext.getString(R.string.no_internet_connection);
        }

        try{
            saveMultas(mContext, json);
        }
        catch (JSONException e){
            Utils.logException(TAG, e);
            return mContext.getString(R.string.msg_json_not_valid);
        }

        return mContext.getString(R.string.done);
    }

    public static boolean isInitDone() {
        return initDone;
    }
    public static boolean isCedulaNbConsistent() {
        return sharedPref.getBoolean(key_cedula_nb_consistent,  false);
    }
    public static boolean isIdPersonaValidated() {
        return sharedPref.getBoolean(key_id_persona_validated,  false);
    }

    public static String getCedulaNb() {
        return sharedPref.getString(key_cedula,  defaultCedulaNb);
    }
    public static String getIdPersona() {
        return sharedPref.getString(key_id_persona,  defaultIdPersona);
    }
    public static String getTotalMultas() {
        return sharedPref.getString(key_total_multas, default_total_multas);
    }
    public static String getLastUpdateTime() {
        return sharedPref.getString(key_update_time, default_update_time);
    }

    private static void resetAllSharedButCedulaNb(){
        utils.saveShared(key_id_persona, defaultIdPersona);
        utils.saveShared(key_total_multas, default_total_multas);
        utils.saveShared(key_update_time, default_update_time);
        utils.saveShared(key_id_persona_validated, false);
        utils.saveShared(key_last_total, 0.0f);
    }
    private static void resetAllShared(){
        utils.saveShared(key_cedula, defaultCedulaNb);
        utils.saveShared(key_cedula_nb_consistent, false);
        resetAllSharedButCedulaNb();
    }

    private static String extractIdPersona(Context mContext, String html)
            throws WrongIdPersonaException{
        //extract id persona:
        String preIdPersona = mContext.getString(R.string.previous_id_persona);
        int startPosition = html.indexOf(preIdPersona);
        startPosition += preIdPersona.length();
        int endPosition = startPosition + 15;
        String idPersonaStr = html.substring(startPosition, endPosition);
        String idPersona = utils.extractFirstNbAsString(idPersonaStr);
        Utils.log(Log.INFO, TAG, "id persona: " + idPersona);

        if (Double.parseDouble(idPersona) < 1000){
            throw new WrongIdPersonaException(
                    mContext.getString(R.string.msg_cedula_not_valid));
        }
        utils.saveShared(key_id_persona ,idPersona);
        utils.saveShared(key_id_persona_validated, true);
        return idPersona;
    }
    private static void saveMultas(Context mContext, String jsonTxt)
            throws JSONException{
        Utils utils = new Utils(mContext);
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
        totalStr = String.format("%s", total);
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", new Locale("es-EC"));
        String dateStr = df.format(System.currentTimeMillis());
        utils.saveShared(
                mContext.getString(R.string.key_total_multas), totalStr);
        utils.saveShared(
                mContext.getString(R.string.key_last_update_time), dateStr);
    }
}

