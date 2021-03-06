package com.weimed.app.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Richard Lee (rlee) on 5/6/14.
 */
public class NewsHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "newsblaze.db";
    private static final int DATABASE_VERSION = 1;

    public NewsHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Method is called during creation of the database
    @Override
    public void onCreate(SQLiteDatabase database) {
        NewsTable.onCreate(database);
    }

    // Method is called during an upgrade of the database,
    // e.g. if you increase the database version
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion,
                          int newVersion) {
        NewsTable.onUpgrade(database, oldVersion, newVersion);
    }
}
