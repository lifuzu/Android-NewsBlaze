package com.weimed.app.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Richard Lee on 4/3/14.
 *
 * Field and table name constants for
 * {@link NewsbakProvider}
 */
public class NewsContract {
    private NewsContract() {}

    /**
     * Content provider authority.
     */
    public static final String CONTENT_AUTHORITY = "com.weimed.app.newsblaze";

    /**
     * Base URI.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Path component for "entry"-type resources..
     */
    private static final String PATH_ENTRIES = "entries";

    /**
     * Columns supported by "entries" records.
     */
    public static class Entry implements BaseColumns {
        /**
         * MIME type for lists of entries.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.newsblaze.entries";

        /**
         * MIME type for individual entries.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.newsblaze.entry";

        /**
         * Fully qualified URI for "entry" resources.
         */
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ENTRIES).build();

        /**
         * Table name where records are stored for "entry" resources.
         */
        public static final String TABLE_NAME = "entry";

        /**
         * Atom ID. (NOTE: Not to be confused with the database primary key, which is _ID.
         */
        public static final String COLUMN_ENTRY_ID = "entry_id";

        /**
         * title
         */
        public static final String COLUMN_TITLE = "title";

        /**
         * content
         */
        public static final String COLUMN_CONTENT = "content";

        /**
         * publisher
         */
        public static final String COLUMN_PUBLISHER = "publisher";

        /**
         * picture URL
         */
        public static final String COLUMN_PICURL = "picurl";

        /**
         * original article URL
         */
        public static final String COLUMN_ORIGINALURL = "originalurl";

        /**
         * created at
         */
        public static final String COLUMN_CREATEDAT = "createdat";

        /**
         * updated at
         */
        public static final String COLUMN_UPDATEDAT = "updatedat";

        /**
         * published at
         */
        public static final String COLUMN_PUBLISHEDAT = "publishedat";
    }
}
