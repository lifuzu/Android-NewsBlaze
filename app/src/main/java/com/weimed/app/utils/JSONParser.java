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

package com.weimed.app.utils;

import android.text.format.Time;
import android.util.Xml;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class parses JSON items.
 *
 * <p>Given an InputStream representation of a json array, it returns a List of entries,
 * where each list element represents a single entry in the JSON item.
 *
 */
public class JSONParser {

    /** Parse an http response, returning a collection of Entry objects.
     *
     * @param in Atom feed, as a stream.
     * @return JSONArray of objects.
     * @throws org.json.JSONException on error parsing item.
     * @throws java.io.IOException on I/O error.
     */
    public JSONArray parseJSONArray(InputStream in)
            throws JSONException, IOException, ParseException {
        try {
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            StringBuilder builder = new StringBuilder();

            String inputStr;
            while ((inputStr = streamReader.readLine()) != null)
                builder.append(inputStr);
            return new JSONArray(builder.toString());
        } finally {
            in.close();
        }
    }

    /** Parse an http response, returning a JSON Entry object.
     *
     * @param in A feed, as a stream.
     * @return A JSONObject.
     * @throws org.json.JSONException on error parsing item.
     * @throws java.io.IOException on I/O error.
     */
    public JSONObject parseJSONObject(InputStream in)
            throws JSONException, IOException, ParseException {
        try {
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            StringBuilder builder = new StringBuilder();

            String inputStr;
            while ((inputStr = streamReader.readLine()) != null)
                builder.append(inputStr);
            return new JSONObject(builder.toString());
        } finally {
            in.close();
        }
    }
}
