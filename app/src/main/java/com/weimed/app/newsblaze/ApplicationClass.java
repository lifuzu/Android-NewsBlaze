package com.weimed.app.newsblaze;

import android.app.Application;
import android.widget.SimpleCursorAdapter;

/**
 * Created by Richard Lee (rlee) on 5/10/14.
 */
public class ApplicationClass extends Application{
    /**
     * Cursor adapter for entire application.
     */
    private SimpleCursorAdapter adapter;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public SimpleCursorAdapter getAdapter() {
        return adapter;
    }
}
