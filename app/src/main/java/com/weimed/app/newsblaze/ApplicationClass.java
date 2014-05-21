package com.weimed.app.newsblaze;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.util.LruCache;
import android.widget.SimpleCursorAdapter;

import com.jakewharton.disklrucache.DiskLruCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.weimed.app.utils.DiskLruImageCache;
import com.weimed.app.utils.ImageCache;
import com.weimed.app.utils.ImageWorker;
import com.weimed.app.utils.RecyclingBitmapDrawable;
import com.weimed.app.utils.Utils;

import java.io.File;

/**
 * Created by Richard Lee (rlee) on 5/10/14.
 */
public class ApplicationClass extends Application{
    private static Context context;

    // memory cache
    private static LruCache<String, Drawable> mMemoryCache;

    // disk cache
    private static ImageCache mImageCache;
    private ImageCache.ImageCacheParams mImageCacheParams;
    private static DiskLruCache mDiskLruCache;
    private final Object mDiskCacheLock = new Object();
    private boolean mDiskCacheStarting = true;
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
    private static final String DISK_CACHE_SUBDIR = "thumbnails";

    private static final int MESSAGE_CLEAR = 0;
    private static final int MESSAGE_INIT_DISK_CACHE = 1;
    private static final int MESSAGE_FLUSH = 2;
    private static final int MESSAGE_CLOSE = 3;

    /**
     * Cursor adapter for entire application.
     */
    private SimpleCursorAdapter adapter;

    @Override
    public void onCreate() {
        super.onCreate();
//        ApplicationClass.context = getApplicationContext();
//        // Create global configuration and initialize ImageLoader with this configuration
//        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).build();
//        ImageLoader.getInstance().init(config);

        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
//        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
//
//        // Use 1/4th of the available memory for this memory cache.
//        final int cacheSize = maxMemory / 4;
//
//        mMemoryCache = new LruCache<String, Drawable>(cacheSize) {
//            @Override
//            protected int sizeOf(String key, Drawable drawable) {
//                // The cache size will be measured in kilobytes rather than
//                // number of items.
//                Log.d(this.getClass().getSimpleName(), "cache size:" + cacheSize);
//                Log.d(this.getClass().getSimpleName(), "image size:" + ((BitmapDrawable)drawable).getBitmap().getByteCount() / 1024);
//                return ((BitmapDrawable)drawable).getBitmap().getByteCount() / 1024;
//            }
//        };

        // TODO: Initialize disk cache on background thread
        // http://developer.android.com/training/displaying-bitmaps/cache-bitmap.html
//        mDiskLruCache = new DiskLruImageCache(context, DISK_CACHE_SUBDIR, DISK_CACHE_SIZE,
//                Bitmap.CompressFormat.JPEG, 70);
        // Initialize disk cache on background thread
//        File cacheDir = getDiskCacheDir(this, DISK_CACHE_SUBDIR);
//        new InitDiskCacheTask().execute(cacheDir);
        ImageCache.ImageCacheParams cacheParams =
                new ImageCache.ImageCacheParams(this, DISK_CACHE_SUBDIR);
        cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory
        addImageCache(cacheParams);
    }

    public static Context getAppContext() {
        return ApplicationClass.context;
    }

    public SimpleCursorAdapter getAdapter() {
        return adapter;
    }

    // set memory cache
//    public static void addBitmapToMemoryCache(String key, Drawable drawable) {
//        if (getBitmapFromMemCache(key) == null) {
//            mMemoryCache.put(key, drawable);
//        }
//    }
//
//    // get memory cache
//    public static Drawable getBitmapFromMemCache(String key) {
//        return mMemoryCache.get(key);
//    }

    /**
     * Adds an {@link ImageCache} to this {@link com.weimed.app.utils.ImageWorker} to handle disk and memory bitmap
     * caching.
     * @param cacheParams The cache parameters to use for the image cache.
     * {@link ImageCache.ImageCacheParams#ImageCacheParams(android.content.Context, String)}.
     */
    public void addImageCache(ImageCache.ImageCacheParams cacheParams) {
        mImageCache = ImageCache.getInstance(cacheParams);
        new CacheAsyncTask().execute(MESSAGE_INIT_DISK_CACHE);
    }

    protected class CacheAsyncTask extends com.weimed.app.utils.AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... params) {
            switch ((Integer)params[0]) {
                case MESSAGE_CLEAR:
                    clearCacheInternal();
                    break;
                case MESSAGE_INIT_DISK_CACHE:
                    initDiskCacheInternal();
                    break;
                case MESSAGE_FLUSH:
                    flushCacheInternal();
                    break;
                case MESSAGE_CLOSE:
                    closeCacheInternal();
                    break;
            }
            return null;
        }
    }

    protected void initDiskCacheInternal() {
        if (mImageCache != null) {
            mImageCache.initDiskCache();
        }
    }

    protected void clearCacheInternal() {
        if (mImageCache != null) {
            mImageCache.clearCache();
        }
    }

    protected static void flushCacheInternal() {
        if (mImageCache != null) {
            mImageCache.flush();
        }
    }

    protected static void closeCacheInternal() {
        if (mImageCache != null) {
            mImageCache.close();
            mImageCache = null;
        }
    }

    public void clearCache() {
        new CacheAsyncTask().execute(MESSAGE_CLEAR);
    }

    public void flushCache() {
        new CacheAsyncTask().execute(MESSAGE_FLUSH);
    }

    public void closeCache() {
        new CacheAsyncTask().execute(MESSAGE_CLOSE);
    }

    // set disk cache
//    public static void addBitmapToDiskCache(String key, Drawable drawable) {
//        if ( ! mDiskLruCache.containsKey(key)) {
//            mDiskLruCache.put(key, mDiskLruCache.drawableToBitmap(drawable));
//        }
//    }
    public static void addBitmapToCache(String key, BitmapDrawable drawable) {
        if (mImageCache != null) {
            mImageCache.addBitmapToCache(key, drawable);
        }
    }

    // get disk cache
//    public static Drawable getBitmapFromDiskCache(String key) {
//        if (mDiskLruCache.containsKey(key)) {
//            Bitmap bitmap = mDiskLruCache.getBitmap(key);
//            return new BitmapDrawable(context.getResources(), bitmap);
//        } else {
//            return null;
//        }
//    }
    public static BitmapDrawable getBitmapFromCache(Context context, String key) {
        if (mImageCache != null ) {
            BitmapDrawable drawable = mImageCache.getBitmapFromMemCache(key);
            if (drawable != null) {
                return drawable;
            }
            Bitmap bitmap = mImageCache.getBitmapFromDiskCache(key);
            if (bitmap != null) {
                if (Utils.hasHoneycomb()) {
                    // Running on Honeycomb or newer, so wrap in a standard BitmapDrawable
                    drawable = new BitmapDrawable(context.getResources(), bitmap);
                } else {
                    // Running on Gingerbread or older, so wrap in a RecyclingBitmapDrawable
                    // which will recycle automagically
                    drawable = new RecyclingBitmapDrawable(context.getResources(), bitmap);
                }

                if (mImageCache != null) {
                    mImageCache.addBitmapToCache(key, drawable);
                }
            }
            return drawable;
        }
        return null;
    }

//    class InitDiskCacheTask extends AsyncTask<File, Void, Void> {
//        @Override
//        protected Void doInBackground(File... params) {
//            synchronized (mDiskCacheLock) {
//                File cacheDir = params[0];
//                mDiskLruCache = DiskLruCache.open(cacheDir, DISK_CACHE_SIZE);
//                mDiskCacheStarting = false; // Finished initialization
//                mDiskCacheLock.notifyAll(); // Wake any waiting threads
//            }
//            return null;
//        }
//    }
//
//    public void addBitmapToCache(String key, Bitmap bitmap) {
//        // Add to memory cache as before
//        if (getBitmapFromMemCache(key) == null) {
//            mMemoryCache.put(key, bitmap);
//        }
//
//        // Also add to disk cache
//        synchronized (mDiskCacheLock) {
//            if (mDiskLruCache != null && mDiskLruCache.get(key) == null) {
//                mDiskLruCache.put(key, bitmap);
//            }
//        }
//    }
//
//    public Bitmap getBitmapFromDiskCache(String key) {
//        synchronized (mDiskCacheLock) {
//            // Wait while disk cache is started from background thread
//            while (mDiskCacheStarting) {
//                try {
//                    mDiskCacheLock.wait();
//                } catch (InterruptedException e) {}
//            }
//            if (mDiskLruCache != null) {
//                return mDiskLruCache.get(key);
//            }
//        }
//        return null;
//    }
//
//    // Creates a unique subdirectory of the designated app cache directory. Tries to use external
//    // but if not mounted, falls back on internal storage.
//    public static File getDiskCacheDir(Context context, String uniqueName) {
//        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
//        // otherwise use internal cache dir
//        final String cachePath =
//                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
//                        !isExternalStorageRemovable() ? getExternalCacheDir(context).getPath() :
//                        context.getCacheDir().getPath();
//
//        return new File(cachePath + File.separator + uniqueName);
//    }
}
