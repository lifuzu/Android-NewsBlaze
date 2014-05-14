package com.weimed.app.newsblaze;

import android.app.Application;
import android.content.Context;
import android.widget.SimpleCursorAdapter;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Created by Richard Lee (rlee) on 5/10/14.
 */
public class ApplicationClass extends Application{
    private static Context context;
    /**
     * Cursor adapter for entire application.
     */
    private SimpleCursorAdapter adapter;

    @Override
    public void onCreate() {
        super.onCreate();
        ApplicationClass.context = getApplicationContext();
        // Create global configuration and initialize ImageLoader with this configuration
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).build();
        ImageLoader.getInstance().init(config);
    }

    public static Context getAppContext() {
        return ApplicationClass.context;
    }

    public SimpleCursorAdapter getAdapter() {
        return adapter;
    }
}
