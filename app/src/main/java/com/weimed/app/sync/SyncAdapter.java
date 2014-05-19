/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.weimed.app.sync;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.weimed.app.provider.NewsContract;
import com.weimed.app.utils.JSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.common.io.CharStreams;

/**
 * Define a sync adapter for the app.
 *
 * <p>This class is instantiated in {@link SyncService}, which also binds SyncAdapter to the system.
 * SyncAdapter should only be initialized in SyncService, never anywhere else.
 *
 * <p>The system calls onPerformSync() via an RPC call through the IBinder object supplied by
 * SyncService.
 */
class SyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String TAG = "SyncAdapter";

    /**
     * URL to fetch content from during a sync.
     *
     */
    private static final String NEWS_URL_BASE = "http://192.168.1.138:5984/news_articles";
    private static final String NEWS_URL = NEWS_URL_BASE + "/_design/article/_view/index";

    /**
     * Network connection timeout, in milliseconds.
     */
    private static final int NET_CONNECT_TIMEOUT_MILLIS = 15000;  // 15 seconds

    /**
     * Network read timeout, in milliseconds.
     */
    private static final int NET_READ_TIMEOUT_MILLIS = 10000;  // 10 seconds

    /**
     * Content resolver, for performing database operations.
     */
    private final ContentResolver mContentResolver;

    /**
     * Project used when querying content provider. Returns all known fields.
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
     * Constructor. Obtains handle to content resolver for later use.
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
    }

    /**
     * Called by the Android system in response to a request to run the sync adapter. The work
     * required to read data from the network, parse it, and store it in the content provider is
     * done here. Extending AbstractThreadedSyncAdapter ensures that all methods within SyncAdapter
     * run on a background thread. For this reason, blocking I/O and other long-running tasks can be
     * run <em>in site</em>, and you don't have to set up a separate thread for them.
     .
     *
     * <p>This is where we actually perform any work required to perform a sync.
     * {@link android.content.AbstractThreadedSyncAdapter} guarantees that this will be called on a non-UI thread,
     * so it is safe to preform blocking I/O here.
     *
     * <p>The syncResult argument allows you to pass information back to the method that triggered
     * the sync.
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        Log.i(TAG, "Beginning network synchronization");
        try {
            final URL location = new URL(NEWS_URL);
            InputStream stream = null;

            try {
                Log.i(TAG, "Streaming data from network: " + location);
                stream = downloadUrl(location);
                updateLocalJSONData(stream, syncResult);
                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, "Feed URL is malformed", e);
            syncResult.stats.numParseExceptions++;
            return;
        } catch (IOException e) {
            Log.e(TAG, "Error reading from network: " + e.toString());
            syncResult.stats.numIoExceptions++;
            return;
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing feed: " + e.toString());
            syncResult.stats.numParseExceptions++;
            return;
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing feed: " + e.toString());
            syncResult.stats.numParseExceptions++;
            return;
        } catch (RemoteException e) {
            Log.e(TAG, "Error updating database: " + e.toString());
            syncResult.databaseError = true;
            return;
        } catch (OperationApplicationException e) {
            Log.e(TAG, "Error updating database: " + e.toString());
            syncResult.databaseError = true;
            return;
        }
        Log.i(TAG, "Network synchronization complete");
    }

    /**
     * Read JSON from an input stream, storing it into the content provider.
     *
     * <p>This is where incoming data is persisted, committing the results of a sync. In order to
     * minimize (expensive) disk operations, we compare incoming data with what's already in our
     * database, and compute a merge. Only changes (insert/update/delete) will result in a database
     * write.
     *
     * <p>As an additional optimization, we use a batch operation to perform all database writes at
     * once.
     *
     * <p>Merge strategy:
     * 1. Get cursor to all items in feed<br/>
     * 2. For each item, check if it's in the incoming data.<br/>
     *    a. YES: Remove from "incoming" list. Check if data has mutated, if so, perform
     *            database UPDATE.<br/>
     *    b. NO: Schedule DELETE from database.<br/>
     * (At this point, incoming database only contains missing items.)<br/>
     * 3. For any items remaining in incoming list, ADD to database.
     */
    public void updateLocalJSONData(final InputStream stream, final SyncResult syncResult)
            throws IOException, JSONException, RemoteException,
            OperationApplicationException, ParseException {
        final JSONParser JSONParser = new JSONParser();
        final ContentResolver contentResolver = getContext().getContentResolver();

        Log.i(TAG, "Parsing stream as JSON Array");
        final JSONObject json = JSONParser.parseJSONObject(stream);
        Log.i(TAG, "Parsing complete. Found " + json.getInt("total_rows") + " entries");


        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

        // Build hash table of incoming entries
        HashMap<String, JSONObject> entryMap = new HashMap<String, JSONObject>();
        final JSONArray entries = json.getJSONArray("rows");
        for (int i = 0; i < json.getInt("total_rows"); i++) {
            JSONObject e = entries.getJSONObject(i).getJSONObject("value");
            entryMap.put(e.getString("_id"), e);
        }

        // Get list of all items
        Log.i(TAG, "Fetching local entries for merge");
        Uri uri = NewsContract.Entry.CONTENT_URI; // Get all entries
        Cursor c = contentResolver.query(uri, PROJECTION, null, null, null);
        assert c != null;
        Log.i(TAG, "Found " + c.getCount() + " local entries. Computing merge solution...");

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

        while (c.moveToNext()) {
            syncResult.stats.numEntries++;
            id = c.getInt(COLUMN_ID);
            entryId = c.getString(COLUMN_ENTRY_ID);
            title = c.getString(COLUMN_TITLE);
            content = c.getString(COLUMN_CONTENT);
            publisher = c.getString(COLUMN_PUBLISHER);
            picurl = c.getString(COLUMN_PICURL);
            originalurl = c.getString(COLUMN_ORIGINALURL);
            createdat = c.getString(COLUMN_CREATEDAT);
            updatedat = c.getString(COLUMN_UPDATEDAT);
            publishedat = c.getString(COLUMN_PUBLISHEDAT);
            JSONObject match = entryMap.get(entryId);
//            if (match != null) {
                // Entry exists. Remove from entry map to prevent insert later.
//                entryMap.remove(entryId);
                // Check to see if the entry needs to be updated
                // How to know update local or remote? updatedAt! which is newer, update another.
//                Uri existingUri = NewsContract.Entry.CONTENT_URI.buildUpon()
//                                              .appendPath(Integer.toString(id)).build();
//                if ((match.getString("title") != null && !match.getString("title").equals(title)) ||
//                    (match.getString("content") != null && !match.getString("content").equals(content)) ||
//                    (match.getString("publisher") != null && !match.getString("publisher").equals(publisher)) ||
//                    (match.getString("picurl") != null && !match.getString("picurl").equals(picurl)) ||
//                    (match.getString("originalurl") != null && !match.getString("originalurl").equals(originalurl)) ||
//                    (match.getString("createdat") != null && !match.getString("createdat").equals(createdat)) ||
//                    (match.getString("updatedat") != null && !match.getString("updatedat").equals(updatedat)) ||
//                    (match.getString("publishedat") != null && !match.getString("publishedat").equals(publishedat))
//                   ) {
//                    // Update existing record
//                    Log.i(TAG, "Scheduling update: " + existingUri);
//                    batch.add(ContentProviderOperation.newUpdate(existingUri)
//                         .withValue(NewsContract.Entry.COLUMN_TITLE, title)
//                         .withValue(NewsContract.Entry.COLUMN_CONTENT, content)
//                         .withValue(NewsContract.Entry.COLUMN_PUBLISHER, publisher)
//                         .withValue(NewsContract.Entry.COLUMN_PICURL, picurl)
//                         .withValue(NewsContract.Entry.COLUMN_ORIGINALURL, originalurl)
//                         .withValue(NewsContract.Entry.COLUMN_CREATEDAT, createdat)
//                         .withValue(NewsContract.Entry.COLUMN_UPDATEDAT, updatedat)
//                         .withValue(NewsContract.Entry.COLUMN_PUBLISHEDAT, publishedat)
//                         .build());
//                    syncResult.stats.numUpdates++;
//                } else {
//                    Log.i(TAG, "No action: " + existingUri);
//                }
//            } else {
                // Entry doesn't exist. Remove it from the database.
                Uri deleteUri = NewsContract.Entry.CONTENT_URI.buildUpon()
                                            .appendPath(Integer.toString(id)).build();
                Log.i(TAG, "Scheduling delete: " + deleteUri);
                batch.add(ContentProviderOperation.newDelete(deleteUri).build());
                syncResult.stats.numDeletes++;
//            }
        }
        c.close();

        // Add new items
        for (JSONObject e : entryMap.values()) {
            Log.i(TAG, "Scheduling insert: entry_id=" + e.getString("_id"));
            batch.add(ContentProviderOperation.newInsert(NewsContract.Entry.CONTENT_URI)
                    .withValue(NewsContract.Entry.COLUMN_ENTRY_ID, e.getString("_id"))
                    .withValue(NewsContract.Entry.COLUMN_TITLE, e.getString("title"))
                    .withValue(NewsContract.Entry.COLUMN_CONTENT, fetchTextFileToString(NEWS_URL_BASE + '/' + e.getString("_id") + "/content.md"))
                    .withValue(NewsContract.Entry.COLUMN_PUBLISHER, e.getString("publisher"))
                    .withValue(NewsContract.Entry.COLUMN_PICURL, e.has("pic_link") ? e.getString("pic_link") : null)
                    .withValue(NewsContract.Entry.COLUMN_ORIGINALURL, e.getString("origin_link"))
                    .withValue(NewsContract.Entry.COLUMN_CREATEDAT, e.getString("created_at"))
                    .withValue(NewsContract.Entry.COLUMN_UPDATEDAT, e.getString("updated_at"))
                    .withValue(NewsContract.Entry.COLUMN_PUBLISHEDAT, e.getString("publish_at"))
                    .build());
            syncResult.stats.numInserts++;
        }
        Log.i(TAG, "Merge solution ready. Applying batch update");
        mContentResolver.applyBatch(NewsContract.CONTENT_AUTHORITY, batch);
        mContentResolver.notifyChange(
                NewsContract.Entry.CONTENT_URI, // URI where data was modified
                null,                           // No local observer
                false);                          // IMPORTANT: Do not sync to network
        // This sample doesn't support uploads, but if *your* code does, make sure you set
        // syncToNetwork=false in the line above to prevent duplicate syncs.
    }

    /**
     * Given a string representation of a URL, sets up a connection and gets an input stream.
     */
    private InputStream downloadUrl(final URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(NET_READ_TIMEOUT_MILLIS /* milliseconds */);
        conn.setConnectTimeout(NET_CONNECT_TIMEOUT_MILLIS /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        return conn.getInputStream();
    }

    /** Parse an http response, returning a String.
     *
     * @param in A feed, as a stream.
     * @return A String.
     * @throws java.io.IOException on I/O error.
     */
    public String parseString(InputStream in)
            throws IOException, ParseException {
        try {
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            StringBuilder builder = new StringBuilder();

            String inputStr;
            while ((inputStr = streamReader.readLine()) != null)
                builder.append(inputStr);
            return builder.toString();
        } finally {
            in.close();
        }
    }

    /** Download the text file from URL string, returning a String.
     *
     * @param strUrl, as a String.
     * @return A String.
     * @throws java.io.IOException on I/O error.
     */
    public String fetchTextFileToString(String strUrl) {
        URL url = null;
        try {
            url = new URL(strUrl);
        } catch (MalformedURLException e) {}
        String ret = "";
        try {
            ret = CharStreams.toString(new InputStreamReader(url.openStream(), "UTF-8"));
        } catch (IOException e) {}
        return ret;
    }
}
