package com.hacknc.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by Gunnar on 10/10/2015.
 */
public class CrimeData {

    private static SQLiteDatabase data;

    private CrimeData() {}

    /**
     * Loads the DB for use.
     * @param context
     */
    public static void loadDB (Context context) {
        DataDB dataHelper = new DataDB (context);
        data = dataHelper.getReadableDatabase();
    }

    /**
     * Gets a list of rows of data for a state. A row contains state name, county name, county population, violent crime
     * amount, and property crime amount.
     * @param stateName the name of the state
     * @return a list of rows
     */
    public static ArrayList<CrimeDataRow> getData(String stateName) {
        if (data != null) {
            String query = "SELECT * FROM CrimeData WHERE StateName = ?";
            Cursor cursor = data.rawQuery(query, new String[] {stateName.toUpperCase()});
            return parseCursor(cursor);
        }
        return null;
    }

    /**
     * Gets row of data for a state and county. A row contains state name, county name, county population, violent crime
     * amount, and property crime amount.
     * @param stateName the name of the state
     * @param countyName the name of the county
     * @return a row
     */
    public static CrimeDataRow getData(String stateName, String countyName) {
        if (data != null) {
            String query = "SELECT * FROM CrimeData WHERE StateName = ? AND CountyName = ?";
            Cursor cursor = data.rawQuery(query, new String[] {stateName.toUpperCase(), countyName});
            ArrayList<CrimeDataRow> rows = parseCursor(cursor);
            if (!rows.isEmpty()) {
                return rows.get(0);
            }
        }
        return null;
    }

    private static ArrayList<CrimeDataRow> parseCursor(Cursor cursor) {
        ArrayList<CrimeDataRow> rows = new ArrayList<CrimeDataRow>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String countyName = cursor.getString(cursor.getColumnIndex("CountyName"));
                    String stateName = cursor.getString(cursor.getColumnIndex("StateName"));
                    int population = cursor.getInt(cursor.getColumnIndex("Population"));
                    int violentCrime = cursor.getInt(cursor.getColumnIndex("ViolentCrime"));
                    int propertyCrime = cursor.getInt(cursor.getColumnIndex("PropertyCrime"));
                    CrimeDataRow row = new CrimeDataRow(countyName, stateName, population, violentCrime, propertyCrime);
                    rows.add(row);
                } while (cursor.moveToNext());
            }
        }
        else
            return null;
        return rows;
    }




}
