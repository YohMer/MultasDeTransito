package com.cortocamino.yoh.multasdetransito;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by yoh on 7/13/16.
 */
public class Multas {

    private static Context mContext;
    private static final String DEBUG_TAG = "DEBUG";
    private static final String ERROR = "error";
    private static final boolean DEBUG_FIRST_START = true;
    private static final boolean MY_DEBUG = true;
    private static Utils utils;
    private static SharedPreferences sharedPref;

    private static String defaultCedulaNb;
    private static boolean cedulaNbValid;
    private static String defaultIdPersona;
    private static boolean idPersonaValid;
    private static String defaultMultas;
    private static String default_update_time;

    private static String key_cedula;
    private static String key_cedula_nb_consistent;
    private static String key_id_persona;
    private static String key_id_persona_validated;
    private static String key_total_multas;
    private static String key_update_time;
    private static String link_to_multas_page_list;
    private static String link_to_xjson_multas_list;

    private static boolean initDone = false;
    private static boolean NetworkOn = false;
    private static boolean ValidatingCedula = false;
    private static boolean unvalidCedula = true;
    private static boolean multasFound = false;
    //private static String edulaNb, idPersona, totalMultas, lastUpdateTime;

    private static String onUseBy = "";
    
    public static void init(Context context){
        mContext = context;
        utils = new Utils(mContext);

        //init:
        sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        defaultCedulaNb = mContext.getString(R.string.default_cedula_nb);
        defaultIdPersona = mContext.getString(R.string.default_id_persona);
        defaultMultas = mContext.getString(R.string.default_multas);
        default_update_time = mContext.getString(R.string.default_update_time);

        key_cedula = mContext.getString(R.string.key_cedula);
        key_cedula_nb_consistent = mContext.getString(R.string.key_cedula_not_valid);
        key_id_persona = mContext.getString(R.string.key_id_persona);
        key_id_persona_validated = mContext.getString(R.string.key_id_persona_validated);
        key_total_multas = mContext.getString(R.string.key_total_multas);
        key_update_time = mContext.getString(R.string.key_last_update_time);
        link_to_multas_page_list = mContext.getString(R.string.link_to_multas_page_list);
        link_to_xjson_multas_list =
                mContext.getString(R.string.link_to_xjson_multas_list);

        initDone = true;

        if (DEBUG_FIRST_START)
            mContext.getSharedPreferences("YOUR_PREFS", 0).edit().clear().commit();
    }

    public static void changeCedulaNb(Context mcontext, String cedulaNb){
        Utils utils = new Utils(mcontext);
        
        if((cedulaNb.length() == 10) && (Integer.parseInt(cedulaNb) > 0)) {
            utils.saveShared(key_cedula_nb_consistent, true);

            String cedulaNbSaved = sharedPref.getString(key_cedula,  defaultIdPersona);
            //is cedula value new
            if(!cedulaNb.equals(cedulaNbSaved)){
                utils.saveShared(key_cedula, cedulaNb);
                resetAllSharedButCedulaNb();
            }
            
        }else{
            utils.saveShared(key_cedula_nb_consistent, false);
            utils.saveShared(key_id_persona, defaultCedulaNb);
            resetAllShared();
        }
    }
    public static String update(Context mContext){
        if (!isInitDone()){
            return null;//todo: throw an exception
        }

        String idPersona = "";
        String cedulaNb = sharedPref.getString(key_cedula,  defaultCedulaNb);

        if(!isCedulaNbConsistent()){
            return "cedula nbr not consistent"; //todo: Exception
        }

        //is cedula validated? (if id persona is validated so cedula nb)
        if(isIdPersonaValidated()) {
            idPersona = sharedPref.getString(key_id_persona, defaultIdPersona);
        }else{
            //find id cedula
            if (!utils.isNetworkAvailable()){
                return mContext.getString(R.string.no_connection);//todo? exception?
            }

            String url = String.format(link_to_multas_page_list, cedulaNb);

            String html;
            try{
                html = utils.downloadUrl(url);
            }
            catch (Exception e){
                return mContext.getString(R.string.no_connection);
                //todo: is it possible to have this exception handle by the method?
            }

            try{
                idPersona = extractIdPersona(mContext, html);
            }
            catch (Exception e){

            }
        }

        //full json link:
        String multasUrl = String.format(
                mContext.getString(R.string.link_to_xjson_multas_list),
                idPersona, cedulaNb, System.currentTimeMillis());

        if (!utils.isNetworkAvailable()){
            return mContext.getString(R.string.no_connection);//todo? exception?
        }

        String json;
        try{
            json = utils.downloadUrl(multasUrl);
        }
        catch (Exception e){
            return mContext.getString(R.string.no_connection);
            //todo: is it possible to have this exception handle by the method?
        }

        try{
            saveMultas(mContext, json);
        }
        catch (Exception e){
            //todo: is it possible to have this exception handle by the method?
        }
        return "Done";
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
    public static boolean isMultasFound() {
        return multasFound;
    }

    public static String getCedulaNb() {
        return sharedPref.getString(key_cedula,  defaultCedulaNb);
    }
    public static String getIdPersona() {
        return sharedPref.getString(key_id_persona,  defaultIdPersona);
    }
    public static String getTotalMultas() {
        return sharedPref.getString(key_total_multas,  defaultMultas);
    }
    public static String getLastUpdateTime() {
        return sharedPref.getString(key_update_time, default_update_time);
    }

    private static void resetAllSharedButCedulaNb(){
        utils.saveShared(key_id_persona, defaultIdPersona);
        utils.saveShared(key_total_multas, defaultMultas);
        utils.saveShared(key_update_time, default_update_time);
        utils.saveShared(key_cedula_nb_consistent, true);
        utils.saveShared(key_id_persona_validated, false);
    }
    private static void resetAllShared(){
        utils.saveShared(key_cedula, defaultCedulaNb);
        resetAllSharedButCedulaNb();
    }


    private static String extractIdPersona(Context mContext, String html) {
        //extract id persona:
        String preIdPersona = mContext.getString(R.string.previous_id_persona);
        int startPosition = html.indexOf(preIdPersona);
        startPosition += preIdPersona.length();
        int endPosition = startPosition + 15;
        String idPersonaStr = html.substring(startPosition, endPosition);
        String idPersona = utils.extractFirstNbAsString(idPersonaStr);
        Log.d(DEBUG_TAG, "id persona: " + idPersona);

        if (Integer.parseInt(idPersona) < 1000){
            return ""; //todo: throw an exception
        }
        utils.saveShared(key_id_persona ,idPersona);
        utils.saveShared(key_id_persona_validated, true);
        return idPersona;
    }
    private static void saveMultas(Context mContext, String jsonTxt){
        Utils utils = new Utils(mContext);
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

            utils.saveShared(
                    mContext.getString(R.string.key_total_multas), totalStr);
            utils.saveShared(
                    mContext.getString(R.string.key_last_update_time), dateStr);

        }catch(JSONException e){
            //throw exception
        }
    }
}
