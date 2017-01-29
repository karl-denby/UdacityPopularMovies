package com.example.android.myapplication;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;


import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class MovieGrid extends AppCompatActivity {

    GridView mMovieGrid;
    ProgressBar mProgressBar;
    TextView mErrorText;
    String mResponseFromJSON;
    String[] mMovieId = new String[10];
    String[] mPosterUrl = new String[10];
    String mSortType;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sort, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        showProgressIndicator();
        int clickedItem = item.getItemId();

        if (clickedItem == R.id.popular) {
            mSortType = "popularity.desc"; queryAPI(mSortType);
        }

        if (clickedItem == R.id.rated) {
            mSortType = "vote_average.desc"; queryAPI(mSortType);
        }

        queryAPI(mSortType);
        Log.v("Query Param", clickedItem + " " + mSortType);
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();

        showProgressIndicator();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (networkOnline()) {
            showGrid();
        } else {
            showNetworkError();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_grid);

        mProgressBar = (ProgressBar) findViewById(R.id.pb_grid_loading);
        mMovieGrid = (GridView) findViewById(R.id.gv_grid_posters);
        mErrorText = (TextView) findViewById(R.id.tv_grid_error);

        // Check for a valid network connection
        // run query
        // or error
        if (mSortType == null) {
            mSortType = "popularity.desc";
        }

        if (networkOnline()) {
            queryAPI(mSortType);
        } else {
            showNetworkError();
        }
    }

    private void createMovieList(String data) {
        try {
            JSONObject reader = new JSONObject(data);
            JSONArray all_movies = reader.getJSONArray("results");
            int i;
            for (i = 0; i < 10; i++) {
                JSONObject movie = all_movies.getJSONObject(i);
                mMovieId[i] = movie.getString("id");
                mPosterUrl[i] = movie.getString("poster_path");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean networkOnline() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showGrid() {
        mProgressBar.setVisibility(View.INVISIBLE);
        mMovieGrid.setVisibility(View.VISIBLE);
    }

    private void showNetworkError() {
        mProgressBar.setVisibility(View.INVISIBLE);
        mErrorText.setVisibility(View.VISIBLE);
    }

    private void showProgressIndicator() {
        mMovieGrid.setVisibility(View.INVISIBLE);
        mErrorText.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void queryAPI(String sortOption) {
        URL url = NetworkUtils.buildUrl(sortOption);
        QueryAsyncTask results = new QueryAsyncTask();
        results.execute(url);
    }

    private class QueryAsyncTask extends AsyncTask<URL, Void, String> {
        @Override
        protected String doInBackground(URL... url) {

            String response = "";
            try {
                response =  NetworkUtils.getResponseFromHttpUrl(url[0]);
            } catch (IOException e) {
                Log.v("queryAPI", e.toString());
            }
            return response;
        }

        @Override
        protected void onPostExecute(final String response) {
            super.onPostExecute(response);
            mResponseFromJSON = response;
            createMovieList(mResponseFromJSON);

            ImageAdapter adapter = new ImageAdapter(MovieGrid.this, mMovieId, mPosterUrl);
            mMovieGrid.setAdapter(null);
            mMovieGrid.setAdapter(adapter);
            mMovieGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    showProgressIndicator();
                    Intent intent = new Intent(MovieGrid.this, MovieDetail.class);
                    intent.putExtra(EXTRA_MESSAGE, mMovieId[position]);
                    intent.putExtra("DATA", mResponseFromJSON);
                    startActivity(intent);
                }
            });
            showGrid();
        }
    }

}
