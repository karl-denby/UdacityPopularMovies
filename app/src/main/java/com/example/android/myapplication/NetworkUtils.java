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
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;


/**
 * These utilities will be used to communicate with the network.
 */
class NetworkUtils {

    private final static String BASE_URL = "https://api.themoviedb.org/3/movie";
    private final static String GRID_BASE_URL = "https://api.themoviedb.org/3/discover/movie";
    private final static String GRID_PARAM_SORT = "sort_by";
    private final static String PARAM_KEY = "api_key";

    // ToDo: () ..Enter your API key below..
    private final static String myKey = "YOUR_API_KEY_HERE";

    /**
     * Builds the URL used to query the movie db .com for our list of movies
     *
     * @param searchQuery The keywords that will be queried for
     * @return The URL to use to query the server
     */
    static URL buildGridUrl(String searchQuery) {
        Uri builtUri = Uri.parse(GRID_BASE_URL).buildUpon()
                .appendQueryParameter(GRID_PARAM_SORT, searchQuery)
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
     * Builds the URL used to query the movie db.com for our reviews of the movie
     *
     * @param  movieId The id of the movie that we want reviews for
     * @return The URL to use to query the server
     */
    static URL buildReviewUrl(String movieId) {
        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(movieId)
                .appendPath("reviews")
                .appendQueryParameter(PARAM_KEY, myKey)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Log.v("Reviews URL: ", "" + url.toString());
        return url;
    }

    /**
     * Builds the URL used to query the moviedb.com for our movie trailers
     *
     * @param  movieId The id of the movie that we want reviews for
     * @return The URL to use to query the server
     */
    static URL buildTrailerUrl(String movieId) {
        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(movieId)
                .appendPath("videos")
                .appendQueryParameter(PARAM_KEY, myKey)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Log.v("Videos URL: ", "" + url.toString());
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