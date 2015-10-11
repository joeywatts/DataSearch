package com.hacknc.database;

import android.content.Context;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Created by Gunnar on 10/10/2015.
 */
public class DataDB extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "Data.db";
    private static final int DATABASE_VERSION = 1;

    public DataDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
}

