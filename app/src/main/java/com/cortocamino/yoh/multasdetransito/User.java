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
import android.database.Cursor;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class User {

    private final static String TAG = "MultasPorCedula";
    
    public static class Db{
        public static final String TABLE = "users";
        public static final String ID = "id";
        public static final String ID_CONTRATO = "id_contrato";
        public static final String ID_PERSONA = "id_persona";
        public static final String PLACA_1 = "placa_1";
        public static final String PLACA_2 = "placa_2";
        public static final String CEDULA = "cedula";
        public static final String UPDATE_DATE = "update_date";
        public static final String TOTAL_MULTAS = "total_multas";
        public static final String OBTAINED_BY = "obtained_by";

        static MySQLiteHelper Helper;
    }

    private static Context mContext;
    private static Utils utils;
    private static ArrayList<User> users;

    //User's database fields
    private int id;
    private String idContrato;
    private String idPersona;
    private String placa1;
    private String placa2;
    private String cedula;
    private int updateDate;
    private String totalMultas;
    private ObtainedBy obtainedBy;


    private static String link_to_multas_page_list;

    private static Boolean initDone = false;

    public static void init(Context mContext){
        if (initDone) return;

        users = new ArrayList<>();

        link_to_multas_page_list = mContext.getString(R.string.link_to_multas_page_list);
        User.mContext = mContext;

        utils = new Utils(mContext);
        Db.Helper = new MySQLiteHelper(mContext); //todo: be sure to avoid the creation of 2 differents helpers
        User.Db.Helper.createAllUsers();

        initDone = true;
    }

    public User(){
        users.add(this);
        this.idContrato = "";
        this.idPersona = "";
        this.placa1 = "";
        this.placa2 = "";
        this.cedula = "";
        this.updateDate = 0;
        this.obtainedBy = ObtainedBy.NONE;
    }

    public User(Context mContext, String idContrato, String idPersona, String placa1,
                String placa2, String cedula, int updateDate, String totalMultas,
                ObtainedBy obtainedBy) {
        users.add(this);
        User.init(mContext);
        this.idContrato = idContrato;
        this.idPersona = idPersona;
        this.placa1 = placa1;
        this.placa2 = placa2;
        this.cedula = cedula;
        this.updateDate = updateDate;
        this.totalMultas = totalMultas;
        this.obtainedBy = obtainedBy;
        Db.Helper.addUser(idContrato, idPersona ,placa1, placa2, cedula, updateDate,
                totalMultas, obtainedBy);
    }

    User(Cursor c) {
        users.add(this);
        if (!initDone) return; //todo: Exception
        id = c.getInt(c.getColumnIndex(Db.ID));
        idContrato = c.getString(c.getColumnIndex(Db.ID_CONTRATO));
        idPersona =c.getString(c.getColumnIndex(Db.ID_PERSONA));
        placa1 = c.getString(c.getColumnIndex(Db.PLACA_1));
        placa2 = c.getString(c.getColumnIndex(Db.PLACA_2));
        cedula = c.getString(c.getColumnIndex(Db.CEDULA));
        updateDate = c.getInt(c.getColumnIndex(Db.UPDATE_DATE));
        totalMultas = c.getString(c.getColumnIndex(Db.TOTAL_MULTAS));
        obtainedBy = ObtainedBy.valueOf(c.getString(c.getColumnIndex(Db.OBTAINED_BY)));
    }

/*    public User(ObtainedBy obtainedBy, String info){
        if (!initDone) return;
        User user;

        switch (obtainedBy){
            case CEDULA:
                //user = addUserFromCedula(info);
                break;
            case PLACA:
                break;
        }
        this.id = user.id;
        this.idContrato = user.idContrato;
        this.idPersona = user.idPersona;
        this.placa1 = user.placa1;
        this.placa2 = user.placa2;
        this.cedula = user.cedula;
        this.updateDate = user.updateDate;
        this.totalMultas = user.totalMultas;
        this.obtainedBy = user.obtainedBy;
    }*/

    public static boolean isCedulaNbConsistent(String cedula) {
        if ((cedula.length() == 10) && (Double.parseDouble(cedula) > 0)) {
            return true;
        } else {
            return false;
        }
    }

    private static String extractIdPersona(String html)
            throws WrongIdPersonaException {
        //extract id persona:
        String preIdPersona = mContext.getString(R.string.previous_id_persona);
        int startPosition = html.indexOf(preIdPersona);
        startPosition += preIdPersona.length();
        int endPosition = startPosition + 15;
        String idPersonaStr = html.substring(startPosition, endPosition);
        String idPersona = utils.extractFirstNbAsString(idPersonaStr);
        Utils.log(Log.INFO, TAG, "id persona: " + idPersona);

        if (Double.parseDouble(idPersona) < 1000) {
            throw new WrongIdPersonaException(
                    mContext.getString(R.string.msg_cedula_not_valid));
        }
        return idPersona;
    }

    static String addUserByCedula(String cedula){
        // based on MultasPorCedula.getMultasFromCedula(mContext)
        long userId = -1;
        String idPersona = "";

        if (!isCedulaNbConsistent(cedula)) {
            return mContext.getString(R.string.msg_cedula_not_valid);
        }

        userId = Db.Helper.getDbIdByCedula(cedula);
        //if cedula not in db
        if (userId == -1){
            userId = Db.Helper.addUser("","","","",cedula,0,"",ObtainedBy.CEDULA);
        }
        else{
            idPersona = Db.Helper.getIdPersonaByDbId(userId);
        }

        //if id persona not known:
        if (idPersona.equals("")) {

            //find id cedula
            if (!utils.isNetworkAvailable()) {
                return mContext.getString(R.string.no_internet_connection);
            }

            String url = String.format(link_to_multas_page_list, "CED", cedula, "");
            String html;
            try {
                html = utils.downloadUrl(url);
            } catch (IOException e) {
                return mContext.getString(R.string.no_server_connection);
            }

            try {
                idPersona = extractIdPersona(html);
                Db.Helper.updateCedulaUser(userId, "", idPersona, "", "", cedula, 0, "");
            } catch (WrongIdPersonaException e) {
                Db.Helper.removeUser(userId);
                return e.getMessage();
            }
        }

        //full json link:
        String multasUrl = String.format(
                mContext.getString(R.string.link_to_xjson_multas_list),
                "P", "", idPersona, "", cedula, "CED", System.currentTimeMillis());

        if (utils.isNetworkUnAvailable()) {
            return mContext.getString(R.string.no_internet_connection);
        }

        String json;
        try {
            json = utils.downloadUrl(multasUrl);
        } catch (Exception e) {
            return mContext.getString(R.string.no_internet_connection);
        }

        String totalMultas;
        try {
            totalMultas = getTotalMultas(mContext, json);
        } catch (JSONException e) {
            Utils.logException(TAG, e);
            return mContext.getString(R.string.msg_json_not_valid);
        }


        //SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", new Locale("es-EC"));
        long dateStr = System.currentTimeMillis();
        Db.Helper.updateCedulaUser(userId, "", idPersona, "", "", cedula, dateStr,
                totalMultas);

        return mContext.getString(R.string.done);

    }

    public static String getTotalMultas(Context mContext, String jsonTxt)
            throws JSONException {
        Utils utils = new Utils(mContext);
        float total = 0;
        String totalStr, buffer;
        int length;
        JSONObject json = new JSONObject(jsonTxt);
        JSONArray rows = json.getJSONArray("rows");
        JSONObject obj;
        length = rows.length();
        for (int i = 0; i < length; i++) {
            obj = (JSONObject) rows.get(i);
            buffer = ((JSONArray) obj.get("cell")).getString(16);
            total += Float.parseFloat(buffer);
        }
        return String.format("%s", total);
    }

}
