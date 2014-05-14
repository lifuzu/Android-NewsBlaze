package com.weimed.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;

/**
 * Created by Richard Lee (rlee) on 5/10/14.
 */
public class NewsAdapter extends SimpleCursorAdapter{

    public NewsAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }
}
