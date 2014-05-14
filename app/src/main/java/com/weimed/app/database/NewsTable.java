package com.weimed.app.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by Richard Lee (rlee) on 5/6/14.
 */
public class NewsTable {
    // Database table
    public static final String TABLE_NEWS = "news";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_UUID = "uuid";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_PUBLISHER = "publisher";
    public static final String COLUMN_PICURL = "picurl";
    public static final String COLUMN_ORIGINALURL = "originalurl";
    public static final String COLUMN_CREATEDAT = "createdat";
    public static final String COLUMN_UPDATEDAT = "updatedat";
    public static final String COLUMN_PUBLISHEDAT = "publishedat";

    // Database creation SQL statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_NEWS
            + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_UUID + " text not null, "
            + COLUMN_TITLE + " text not null, "
            + COLUMN_CONTENT + " text not null, "
            + COLUMN_PUBLISHER + " text, "
            + COLUMN_PICURL + " text, "
            + COLUMN_ORIGINALURL + " text, "
            + COLUMN_CREATEDAT + " DATETIME, "
            + COLUMN_UPDATEDAT + " DATETIME, "
            + COLUMN_PUBLISHEDAT + " DATETIME, "
            + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        Log.w(NewsTable.class.getName(), "Upgrading database from version "
                + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NEWS);
        onCreate(database);
    }
}
