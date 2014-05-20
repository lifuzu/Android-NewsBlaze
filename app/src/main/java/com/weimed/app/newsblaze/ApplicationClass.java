package com.weimed.app.newsblaze;

import android.app.Application;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.LruCache;
import android.widget.SimpleCursorAdapter;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Created by Richard Lee (rlee) on 5/10/14.
 */
public class ApplicationClass extends Application{
    private static Context context;

    // memory cache
    private static LruCache<String, Drawable> mMemoryCache;

    // disk cache
    //private DiskLruCache mDiskLruCache;
    private final Object mDiskCacheLock = new Object();
    private boolean mDiskCacheStarting = true;
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
    private static final String DISK_CACHE_SUBDIR = "thumbnails";

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

        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/4th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 4;

        mMemoryCache = new LruCache<String, Drawable>(cacheSize) {
            @Override
            protected int sizeOf(String key, Drawable drawable) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                Log.d(this.getClass().getSimpleName(), "cache size:" + cacheSize);
                Log.d(this.getClass().getSimpleName(), "image size:" + ((BitmapDrawable)drawable).getBitmap().getByteCount() / 1024);
                return ((BitmapDrawable)drawable).getBitmap().getByteCount() / 1024;
            }
        };
    }

    public static Context getAppContext() {
        return ApplicationClass.context;
    }

    public SimpleCursorAdapter getAdapter() {
        return adapter;
    }

    // set memory cache
    public static void addBitmapToMemoryCache(String key, Drawable drawable) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, drawable);
        }
    }

    // get memory cache
    public static Drawable getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }
}
