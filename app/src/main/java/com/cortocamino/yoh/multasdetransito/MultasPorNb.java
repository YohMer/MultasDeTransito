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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Locale;

public abstract class MultasPorNb {

    public static void saveMultas(Context mContext, String jsonTxt,
                                  String key_total_multas, String key_update_time)
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
        totalStr = String.format("%s", total);
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", new Locale("es-EC"));
        String dateStr = df.format(System.currentTimeMillis());
        utils.saveShared(key_total_multas, totalStr);
        utils.saveShared(key_update_time, dateStr);
    }
}
