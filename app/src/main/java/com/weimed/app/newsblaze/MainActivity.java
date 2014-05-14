package com.weimed.app.newsblaze;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.OperationApplicationException;
import android.content.SyncStatusObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.weimed.app.accounts.GenericAccountService;
import com.weimed.app.provider.NewsContract;
import com.weimed.app.sync.SyncUtils;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ListActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public final static String EXTRA_NEWS_CONTENT = "com.weimed.app.newblaze.CONTENT";
    /**
     * Projection for querying the content provider.
     */
    private static final String[] PROJECTION = new String[] {
            NewsContract.Entry._ID,
            NewsContract.Entry.COLUMN_ENTRY_ID,
            NewsContract.Entry.COLUMN_TITLE,
            NewsContract.Entry.COLUMN_CONTENT,
            NewsContract.Entry.COLUMN_PUBLISHER,
            NewsContract.Entry.COLUMN_PICURL,
            NewsContract.Entry.COLUMN_ORIGINALURL,
            NewsContract.Entry.COLUMN_CREATEDAT,
            NewsContract.Entry.COLUMN_UPDATEDAT,
            NewsContract.Entry.COLUMN_PUBLISHEDAT
    };

    // Constants representing column positions from PROJECTION.
    public static final int COLUMN_ID = 0;
    public static final int COLUMN_ENTRY_ID = 1;
    public static final int COLUMN_TITLE = 2;
    public static final int COLUMN_CONTENT = 3;
    public static final int COLUMN_PUBLISHER = 4;
    public static final int COLUMN_PICURL = 5;
    public static final int COLUMN_ORIGINALURL = 6;
    public static final int COLUMN_CREATEDAT = 7;
    public static final int COLUMN_UPDATEDAT = 8;
    public static final int COLUMN_PUBLISHEDAT = 9;

    /**
     * List of Cursor columns to read from when preparing an adapter to populate the ListView.
     */
    private static final String[] FROM_COLUMNS = new String[]{
            NewsContract.Entry.COLUMN_TITLE,
            NewsContract.Entry.COLUMN_PUBLISHER,
            NewsContract.Entry.COLUMN_PUBLISHEDAT
    };

    /**
     * List of Views which will be populated by Cursor data.
     */
    private static final int[] TO_FIELDS = new int[]{
            R.id.firstLine,
            R.id.secondLine,
            R.id.dateView
    };

    /**
     * Cursor adapter for controlling ListView results.
     */
    private SimpleCursorAdapter adapter;

    /**
     * Options menu used to populate ActionBar.
     */
    private Menu mOptionsMenu;

    /**
     * Handle to a SyncObserver. The ProgressBar element is visible until the SyncObserver reports
     * that the sync is complete.
     *
     * <p>This allows us to delete our SyncObserver once the application is no longer in the
     * foreground.
     */
    private Object mSyncObserverHandle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_listview);

        //try { insertSampleData();} catch (RemoteException e) {} catch (OperationApplicationException e) {}

        /**
         * Create SyncAccount at launch, if needed.
         *
         * <p>This will create a new account with the system for our application, register our
         * {@link com.weimed.app.sync.SyncService} with it, and establish a sync schedule.
         */
        // Create account, if needed
        SyncUtils.CreateSyncAccount(this);

        //final ListView listview = (ListView) findViewById(R.id.listview);

        adapter = new SimpleCursorAdapter(
                this,                // Current context
                R.layout.activity_listitem,
                null,                // Cursor
                FROM_COLUMNS,        // Cursor columns to use
                TO_FIELDS,           // Layout fields to use
                0                    // No flags
        );
        //adapter = new NewsAdapter();
        setListAdapter(adapter);

//        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                NewsItem item = adapter.getItem(i);
//                Toast.makeText(MainActivity.this, item.title, Toast.LENGTH_SHORT).show();
//            }
//        });

        //listview.setEmptyText(getText(R.string.loading));
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        // Get the item that was clicked
        Cursor c = (Cursor) this.getListAdapter().getItem(position);
        String content = c.getString(c.getColumnIndex(NewsContract.Entry.COLUMN_CONTENT));
        //Toast.makeText(this, "You selected: " + title, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, ArticleActivity.class);
        intent.putExtra(EXTRA_NEWS_CONTENT, content);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        mSyncStatusObserver.onStatusChanged(0);

        // Watch for sync state changes
        final int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING |
                ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
        mSyncObserverHandle = ContentResolver.addStatusChangeListener(mask, mSyncStatusObserver);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSyncObserverHandle != null) {
            ContentResolver.removeStatusChangeListener(mSyncObserverHandle);
            mSyncObserverHandle = null;
        }

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * Query the content provider for data.
     *
     * <p>Loaders do queries in a background thread. They also provide a ContentObserver that is
     * triggered when data in the content provider changes. When the sync adapter updates the
     * content provider, the ContentObserver responds by resetting the loader and then reloading
     * it.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // We only have one loader, so we can ignore the value of i.
        // (It'll be '0', as set in onCreate().)
        return new CursorLoader(MainActivity.this,  // Context
                NewsContract.Entry.CONTENT_URI, // URI
                PROJECTION,                     // Projection
                null,                           // Selection
                null,                           // Selection args
                null);                          // Sort
    }

    /**
     * Move the Cursor returned by the query into the ListView adapter. This refreshes the existing
     * UI with the data in the Cursor.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        adapter.swapCursor(cursor);
    }

    /**
     * Called when the ContentObserver defined for the content provider detects that data has
     * changed. The ContentObserver resets the loader, and then re-runs the loader. In the adapter,
     * set the Cursor value to null. This removes the reference to the Cursor, allowing it to be
     * garbage-collected.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        adapter.swapCursor(null);
    }

    public class NewsItem {
        String title;
        String publisher;
    }

    public void insertSampleData() throws RemoteException,
            OperationApplicationException {
        // Insert some sample data here
        ContentResolver mContentResolver = getContentResolver();
        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        batch.add(ContentProviderOperation.newInsert(NewsContract.Entry.CONTENT_URI)
                .withValue(NewsContract.Entry.COLUMN_ENTRY_ID, "111")
                .withValue(NewsContract.Entry.COLUMN_TITLE, "甄子丹小姨子嫁港富商之子 筵开160席超郭晶晶(图)")
                .withValue(NewsContract.Entry.COLUMN_CONTENT, "甄子丹小姨子嫁港富商之子 筵开160席超郭晶晶(图)+")
                .withValue(NewsContract.Entry.COLUMN_PUBLISHER, "新浪娱乐")
                .build());
        mContentResolver.applyBatch(NewsContract.CONTENT_AUTHORITY, batch);
    }

    public List<NewsItem> getSampleData() {
        // fetch sample data from database
        final String[] PROJECTION = new String[] {
                NewsContract.Entry._ID,
                NewsContract.Entry.COLUMN_ENTRY_ID,
                NewsContract.Entry.COLUMN_TITLE,
                NewsContract.Entry.COLUMN_CONTENT,
                NewsContract.Entry.COLUMN_PUBLISHER
        };

        // Constants representing column positions from PROJECTION.
        final int COLUMN_ID = 0;
        final int COLUMN_ENTRY_ID = 1;
        final int COLUMN_TITLE = 2;
        final int COLUMN_CONTENT = 3;
        final int COLUMN_PUBLISHER = 4;

        final ContentResolver contentResolver = getContentResolver();
        Uri uri = NewsContract.Entry.CONTENT_URI; // Get all entries
        Cursor c = contentResolver.query(uri, PROJECTION, null, null, null);
        assert c != null;

        // Find stale data
        int id;
        String entryId;
        String title;
        String content;
        String publisher;
        String picurl;
        String originalurl;
        String createdat;
        String updatedat;
        String publishedat;
        List<NewsItem> list = new ArrayList<NewsItem>();

        while (c.moveToNext()) {
            id = c.getInt(COLUMN_ID);
            entryId = c.getString(COLUMN_ENTRY_ID);
            title = c.getString(COLUMN_TITLE);
            content = c.getString(COLUMN_CONTENT);
            publisher = c.getString(COLUMN_PUBLISHER);

            NewsItem item = new NewsItem();
            item.title = title;
            item.publisher = publisher;
            list.add(item);
        }
        c.close();
        return list;
    }

    public List<NewsItem> DataProvider(){
        List<NewsItem> list = new ArrayList<NewsItem>();

        for(int i=0;i<10000;i++) {
            NewsItem item = new NewsItem();
            item.title = "Chapter "+i;
            item.publisher = "This is description for chapter "+i;
            list.add(item);
        }
        return list;
    }

    private class NewsAdapter extends BaseAdapter {

        List<NewsItem> list = getSampleData();

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public NewsItem getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.activity_listitem, parent, false);
            }

            TextView title = (TextView)convertView.findViewById(R.id.firstLine);
            TextView description = (TextView)convertView.findViewById(R.id.secondLine);

            NewsItem item = list.get(position);
            title.setText(item.title);
            description.setText(item.publisher);

            return convertView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        mOptionsMenu = menu;
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            // If the user clicks the "Refresh" button.
            case R.id.action_refresh:
                SyncUtils.TriggerRefresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Set the state of the Refresh button. If a sync is active, turn on the ProgressBar widget.
     * Otherwise, turn it off.
     *
     * @param refreshing True if an active sync is occurring, false otherwise
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setRefreshActionButtonState(boolean refreshing) {
        if (mOptionsMenu == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            return;
        }

        final MenuItem refreshItem = mOptionsMenu.findItem(R.id.action_refresh);
        if (refreshItem != null) {
            if (refreshing) {
                refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
            } else {
                refreshItem.setActionView(null);
            }
        }
    }

    /**
     * Create a new anonymous SyncStatusObserver. It's attached to the app's ContentResolver in
     * onResume(), and removed in onPause(). If status changes, it sets the state of the Refresh
     * button. If a sync is active or pending, the Refresh button is replaced by an indeterminate
     * ProgressBar; otherwise, the button itself is displayed.
     */
    private SyncStatusObserver mSyncStatusObserver = new SyncStatusObserver() {
        /** Callback invoked with the sync adapter status changes. */
        @Override
        public void onStatusChanged(int which) {
            MainActivity.this.runOnUiThread(new Runnable() {
                /**
                 * The SyncAdapter runs on a background thread. To update the UI, onStatusChanged()
                 * runs on the UI thread.
                 */
                @Override
                public void run() {
                    // Create a handle to the account that was created by
                    // SyncService.CreateSyncAccount(). This will be used to query the system to
                    // see how the sync status has changed.
                    Account account = GenericAccountService.GetAccount(SyncUtils.ACCOUNT_TYPE);
                    if (account == null) {
                        // GetAccount() returned an invalid value. This shouldn't happen, but
                        // we'll set the status to "not refreshing".
                        setRefreshActionButtonState(false);
                        return;
                    }

                    // Test the ContentResolver to see if the sync adapter is active or pending.
                    // Set the state of the refresh button accordingly.
                    boolean syncActive = ContentResolver.isSyncActive(
                            account, NewsContract.CONTENT_AUTHORITY);
                    boolean syncPending = ContentResolver.isSyncPending(
                            account, NewsContract.CONTENT_AUTHORITY);
                    setRefreshActionButtonState(syncActive || syncPending);
                }
            });
        }
    };
}
