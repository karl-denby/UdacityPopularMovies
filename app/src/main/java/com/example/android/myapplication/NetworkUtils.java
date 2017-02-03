/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.example.android.myapplication;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/*
 * Stage 2: To Do List
 * Done: (0) ..Colors/Style/icon
 * Done: (1) ..screen rotation is not an issue
 * Done: (2) ..mark a movie as favourite (local movies collection)
 *              .. update detail layout to have checkbox (DONE)
 *              .. store that data when selected (DONE)
 *              .. check present and toggle checkbox (DONE)
 * Todo: (3) ..when favorites selected shows favourites collection instead of query results
 *              .. update menu to show favourites option (DONE)
 *              .. show stored data (In Progress)
 * Todo: (4) ..view and play trailers (via youtube or browser [Intent]) /movie/{id}/videos
 * Todo: (5) ..read reviews of a selected movie /movie/{id}/reviews
 * Todo: (6) ..titles and ids stored in a ContentProvider with SQL database, updated when toggled
 * Todo: (E) ....store the other fields in the content provider for offline access
 * Todo: (E) ....user can share the 1st trailers url
 */

/**
 * These utilities will be used to communicate with the network.
 */
class NetworkUtils {

    private final static String BASE_URL =
            "https://api.themoviedb.org/3/discover/movie";

    /*
     * The sort field. One of popularity.desc or vote_average.desc
     */
    private final static String PARAM_SORT = "sort_by";

    /*
     * The API key.  You need to register with themoviedb.com and create one
     * then place it in here
     */
    private final static String PARAM_KEY = "api_key";

    // ToDo: () ..Enter your API key below..
    private final static String myKey = "YOUR_KEY_GOES_HERE";

    /**
     * Builds the URL used to query out website.
     *
     * @param searchQuery The keyword that will be queried for.
     * @return The URL to use to query the weather server.
     */
    static URL buildUrl(String searchQuery) {
        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter(PARAM_SORT, searchQuery)
                .appendQueryParameter(PARAM_KEY, myKey)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}