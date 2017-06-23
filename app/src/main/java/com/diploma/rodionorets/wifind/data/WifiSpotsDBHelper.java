package com.diploma.rodionorets.wifind.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.diploma.rodionorets.wifind.MainActivity;
import com.diploma.rodionorets.wifind.utils.WifiUtils;

/**
 * Created by rodionorets on 16.05.17.
 */

public class WifiSpotsDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "spots.db";
    public static final int DATABASE_VERSION = 1;


    public WifiSpotsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_TABLE_QUERY =
                "CREATE TABLE IF NOT EXISTS " + WifiSpotsContract.WifiSpotsEntry.TABLE_NAME + "( " +
                        WifiSpotsContract.WifiSpotsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        WifiSpotsContract.WifiSpotsEntry.COLUMN_LAT + " REAL NOT NULL, " +
                        WifiSpotsContract.WifiSpotsEntry.COLUMN_LNG + " REAL NOT NULL, " +
                        WifiSpotsContract.WifiSpotsEntry.COLUMN_NAME + " STRING NOT NULL, " +
                        WifiSpotsContract.WifiSpotsEntry.COLUMN_LINK_SPEED + " REAL NOT NULL, " +
                        WifiSpotsContract.WifiSpotsEntry.COLUMN_USER_NUMBER + " STRING NOT NULL, " +
                        WifiSpotsContract.WifiSpotsEntry.COLUMN_PASSWORD + " STRING)";


        db.execSQL(CREATE_TABLE_QUERY);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS" + WifiSpotsContract.WifiSpotsEntry.TABLE_NAME);
        onCreate(db);
    }
}
