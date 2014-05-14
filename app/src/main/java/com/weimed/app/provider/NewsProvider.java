package com.weimed.app.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.weimed.app.database.SelectionBuilder;

public class NewsProvider extends ContentProvider {

    TodoDatabase database;

    /**
     * Content authority for this provider.
     */
    private static final String AUTHORITY = NewsContract.CONTENT_AUTHORITY;

    // The constants below represent individual URI routes, as IDs. Every URI pattern recognized by
    // this ContentProvider is defined using sUriMatcher.addURI(), and associated with one of these
    // IDs.
    //
    // When a incoming URI is run through sUriMatcher, it will be tested against the defined
    // URI patterns, and the corresponding route ID will be returned.
    /**
     * URI ID for route: /entries
     */
    public static final int ROUTE_ENTRIES = 1;

    /**
     * URI ID for route: /entries/{ID}
     */
    public static final int ROUTE_ENTRIES_ID = 2;

    /**
     * UriMatcher, used to decode incoming URIs.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(AUTHORITY, "entries", ROUTE_ENTRIES);
        sUriMatcher.addURI(AUTHORITY, "entries/*", ROUTE_ENTRIES_ID);
    }

    public NewsProvider() {
    }

    /**
     * Delete an entry by database by URI.
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SelectionBuilder builder = new SelectionBuilder();
        final SQLiteDatabase sqlite = database.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int count;
        switch (match) {
            case ROUTE_ENTRIES:
                count = builder.table(NewsContract.Entry.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .delete(sqlite);
                break;
            case ROUTE_ENTRIES_ID:
                String id = uri.getLastPathSegment();
                count = builder.table(NewsContract.Entry.TABLE_NAME)
                        .where(NewsContract.Entry._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .delete(sqlite);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    /**
     * Determine the mime type for entries returned by a given URI.
     * @param uri
     * @return content type
     */
    @Override
    public String getType(Uri uri) {
        // at the given URI.
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ROUTE_ENTRIES:
                return NewsContract.Entry.CONTENT_TYPE;
            case ROUTE_ENTRIES_ID:
                return NewsContract.Entry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * Insert a new entry into the database.
     * @param uri
     * @param values
     * @return
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase sqlite = database.getWritableDatabase();
        assert sqlite != null;
        final int match = sUriMatcher.match(uri);
        Uri result;
        switch (match) {
            case ROUTE_ENTRIES:
                long id = sqlite.insertOrThrow(NewsContract.Entry.TABLE_NAME, null, values);
                result = Uri.parse(NewsContract.Entry.CONTENT_URI + "/" + id);
                break;
            case ROUTE_ENTRIES_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return result;
    }

    @Override
    public boolean onCreate() {
        database = new TodoDatabase(getContext());
        return true;
    }

    /**
     * Perform a database query by URI.
     * <p>Currently supports returning all entries (/entries) and individual entries by ID (/entries/{ID}).</p>
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        SQLiteDatabase sqlite = database.getReadableDatabase();
        SelectionBuilder builder = new SelectionBuilder();
        int urlMatch = sUriMatcher.match(uri);
        switch (urlMatch) {
            case ROUTE_ENTRIES_ID:
                // Return a single entry, by ID.
                String id = uri.getLastPathSegment();
                builder.where(NewsContract.Entry._ID + "=?", id);
            case ROUTE_ENTRIES:
                // Return all known entries.
                builder.table(NewsContract.Entry.TABLE_NAME)
                       .where(selection, selectionArgs);
                Cursor c = builder.query(sqlite, projection, sortOrder);
                // Note: Notification URI must be manually set here for loaders to correctly
                // register ContentObservers.
                Context ctx = getContext();
                assert ctx != null;
                c.setNotificationUri(ctx.getContentResolver(), uri);
                return c;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * Update an entry in the database by URI.
     * @param uri
     * @param values
     * @param selection
     * @param selectionArgs
     * @return
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        SelectionBuilder builder = new SelectionBuilder();
        final SQLiteDatabase sqlite = database.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int count;
        switch (match) {
            case ROUTE_ENTRIES:
                count = builder.table(NewsContract.Entry.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .update(sqlite, values);
                break;
            case ROUTE_ENTRIES_ID:
                String id = uri.getLastPathSegment();
                count = builder.table(NewsContract.Entry.TABLE_NAME)
                        .where(NewsContract.Entry._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .update(sqlite, values);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    /**
     * SQLite backend for @{link NewsProvider}.
     *
     * Provides access to an disk-backed, SQLite datastore which is utilized by TodoProvider. This
     * database should never be accessed by other parts of the application directly.
     */
    static class TodoDatabase extends SQLiteOpenHelper {
        /** Schema version. */
        public static final int DATABASE_VERSION = 1;
        /** Filename for SQLite file. */
        public static final String DATABASE_NAME = "newsblaze.db";

        private static final String TYPE_TEXT = " TEXT";
        private static final String TYPE_INTEGER = " INTEGER";
        private static final String TYPE_DATETIME = " DATETIME";
        private static final String COMMA_SEP = ",";
        /** SQL statement to create "entry" table. */
        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + NewsContract.Entry.TABLE_NAME + " (" +
                        NewsContract.Entry._ID + " INTEGER PRIMARY KEY," +
                        NewsContract.Entry.COLUMN_ENTRY_ID + TYPE_TEXT + COMMA_SEP +
                        NewsContract.Entry.COLUMN_TITLE    + TYPE_TEXT + COMMA_SEP +
                        NewsContract.Entry.COLUMN_CONTENT  + TYPE_TEXT + COMMA_SEP +
                        NewsContract.Entry.COLUMN_PUBLISHER + TYPE_TEXT + COMMA_SEP +
                        NewsContract.Entry.COLUMN_PICURL   + TYPE_TEXT + COMMA_SEP +
                        NewsContract.Entry.COLUMN_ORIGINALURL + TYPE_TEXT + COMMA_SEP +
                        NewsContract.Entry.COLUMN_CREATEDAT + TYPE_DATETIME + COMMA_SEP +
                        NewsContract.Entry.COLUMN_UPDATEDAT + TYPE_DATETIME + COMMA_SEP +
                        NewsContract.Entry.COLUMN_PUBLISHEDAT+ TYPE_DATETIME + ")";

        /** SQL statement to drop "entry" table. */
        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + NewsContract.Entry.TABLE_NAME;

        public TodoDatabase(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }
    }
}
