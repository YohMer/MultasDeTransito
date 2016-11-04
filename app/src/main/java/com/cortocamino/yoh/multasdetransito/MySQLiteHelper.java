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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by yoh on 10/30/16.
 * inspired from:
 * http://hmkcode.com/android-simple-sqlite-database-tutorial/
 * http://www.androidhive.info/2013/09/android-sqlite-database-with-multiple-tables/
 */

public class MySQLiteHelper extends SQLiteOpenHelper {

    private final static String TAG = "MySQLiteHelper";
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "multasDB";

//users table   statement
    private static final String CREATE_USERS_TABLE = "CREATE TABLE " + User.Db.TABLE + " ( " +
            User.Db.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            User.Db.ID_CONTRATO + " TEXT, "+
            User.Db.ID_PERSONA + " TEXT, "+
            User.Db.PLACA_1 + " TEXT, "+
            User.Db.PLACA_2 + " TEXT, "+
            User.Db.CEDULA + " TEXT, "+
            User.Db.UPDATE_DATE + " INTEGER, "+
            User.Db.TOTAL_MULTAS + " TEXT, "+
            User.Db.OBTAINED_BY + " TEXT )";

    //multas table:
    //Table Name
    private static final String TABLE_MULTAS = "multas";
    //Fields Names
    private static final String ID_MULTA_SERVER = "id_multa";
    private static final String ID_USER = "id_user";
    private static final String SOURCE = "source";
    private static final String AMOUNT = "amount";
    private static final String PAID = "paid";
    //statement
    private static final String CREATE_MULTAS_TABLE = "CREATE TABLE " + TABLE_MULTAS + " ( " +
            User.Db.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ID_MULTA_SERVER + " TEXT, "+
            ID_USER + " INTEGER, "+
            SOURCE + " TEXT, "+
            AMOUNT + " REAL, "+
            PAID + " INTEGER )";

    public MySQLiteHelper(Context mContext) {
        super(mContext, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the tables
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_MULTAS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Save old table
        db.execSQL("ALTER TABLE {" + User.Db.TABLE + "} RENAME TO oldUsers");
        db.execSQL("ALTER TABLE {" + TABLE_MULTAS + "} RENAME TO oldMultas");
        db.execSQL("DROP TABLE IF EXISTS " + User.Db.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MULTAS);
        Utils.log(Log.DEBUG, TAG, "drop old tables" );

        // create fresh multas table
        this.onCreate(db);

        //then retrieve the data:
        db.execSQL("INSERT INTO " + User.Db.TABLE +
                "SELECT * FROM oldUsers");
        db.execSQL("INSERT INTO " + TABLE_MULTAS +
                "SELECT * FROM oldMultas");

        //then destroy the old table:
        db.execSQL("DROP TABLE IF EXISTS oldUsers");
        db.execSQL("DROP TABLE IF EXISTS oldMultas");
        Utils.log(Log.DEBUG, TAG, "new tables created" );
    }

    public long addUser(String idContrato, String idPersona, String placa1,
                        String placa2, String cedulaNb, long date, String totalMultas,
                         ObtainedBy obtainedBy){
        Utils.log(Log.DEBUG, TAG, "add user " + "user to string" );//todo: write .to_string for cedula object

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(User.Db.ID_CONTRATO, idContrato);
        values.put(User.Db.ID_PERSONA, idPersona);
        values.put(User.Db.PLACA_1, placa1);
        values.put(User.Db.PLACA_2, placa2);
        values.put(User.Db.CEDULA, cedulaNb);
        values.put(User.Db.UPDATE_DATE, date);
        values.put(User.Db.TOTAL_MULTAS, totalMultas);
        values.put(User.Db.OBTAINED_BY, obtainedBy.name());

        long userId = db.insert(User.Db.TABLE, null, values);
        db.close();
        return userId;
    }

    public void updateCedulaUser(long id, String idContrato, String idPersona,
                                 String placa1, String placa2, String cedulaNb,
                                 long updateDate, String totalMultas){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(User.Db.ID_CONTRATO,idContrato);
        cv.put(User.Db.ID_PERSONA,idPersona);
        cv.put(User.Db.PLACA_1,placa1);
        cv.put(User.Db.PLACA_2,placa2);
        cv.put(User.Db.UPDATE_DATE,updateDate);
        cv.put(User.Db.TOTAL_MULTAS,totalMultas);

        String query = User.Db.ID + "=? " + User.Db.ID_PERSONA + "=?" +
                User.Db.OBTAINED_BY + "=?";
        String[] args = { String.valueOf(id), cedulaNb, ObtainedBy.CEDULA.name() };
        db.update(User.Db.TABLE, cv, query, args);
        db.close();
    }

    public boolean removeUser(long userId){
        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(
                User.Db.TABLE, "id = ?", new String[] { String.valueOf(userId) }) > 0;
    }

    public  long getDbIdByCedula(String cedula){
        long answer = -1;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = User.Db.CEDULA + "=?";
        String[] args = {cedula};
        Cursor c = db.query(User.Db.TABLE, null, query, args, null, null, null);

        if (c.moveToFirst()){
            answer = c.getInt((c.getColumnIndex(User.Db.ID)));
        }
        c.close();
        return answer;
    }

    public String getIdPersonaByDbId(long userId){
        String answer = "-1";
        SQLiteDatabase db = this.getReadableDatabase();
        String query = User.Db.ID + "=?";
        String[] args = {String.valueOf(userId)};
        Cursor c = db.query(User.Db.TABLE, null, query, args, null, null, null);

        if (c.moveToFirst()){
            answer = c.getString((c.getColumnIndex(User.Db.ID_PERSONA)));
        }
        c.close();
        return answer;
    }
/*
    public long [] getAllUsersId(){ //todo replaced by public List<User> getAllUsers()
        long [] usersId;
        String selectQuery = "SELECT  * FROM " + TABLE_USERS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                User user = new User();
                c.getInt((c.getColumnIndex("id")))
                  c.getString(c.getColumnIndex(ID_PERSONA)))

                // adding to todo list
                users.add(user);
            } while (c.moveToNext());
        }

        return users;
    }*/

 /*   public User getFirstUserObtainedBy(ObtainedBy obtainedBy){
        String selectQuery = "SELECT  * FROM " + User.Db.TABLE + " WHERE " + User.Db.OBTAINED_BY +
                "=" + ObtainedBy.CEDULA.name();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            return new User(c);
        }
        else{
            return new User();
        }
    }*/

    public long addMultas(String id_multa_from_server, String id_user, String source,
                          float amount, int paid){
        Utils.log(Log.DEBUG, TAG, "add una multa " + "multa to string" );//todo: write .to_string for cedula object

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ID_MULTA_SERVER, id_multa_from_server);
        values.put(ID_USER, id_user);
        values.put(SOURCE, source);
        values.put(AMOUNT, amount);
        values.put(PAID, paid);

        long multaId = db.insert(TABLE_MULTAS, null, values);
        db.close();
        return multaId;
    }

    public boolean removeMulta(long multaId){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(
                User.Db.TABLE, "id = ?", new String[] { String.valueOf(multaId) }) > 0;
    }

}
