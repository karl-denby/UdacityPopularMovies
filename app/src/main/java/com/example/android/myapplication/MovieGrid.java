package com.example.android.myapplication;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class MovieGrid extends AppCompatActivity {

    GridView mMovieGrid;
    ProgressBar mProgressBar;
    TextView mErrorText;
    String mResponseFromJSON;

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

        HashMap<String, String> mMovieData = new HashMap<String, String>();
        mMovieData.put("328111", "/WLQN5aiQG8wc9SeKwixW7pAR8K.jpg");
        mMovieData.put("297761", "/z4x0Bp48ar3Mda8KiPD1vwSY3D8.jpg");

        String[] mMovieId = {
                "328111",
                "297761"
        };

        String[] mPosterUrl = {
                "WLQN5aiQG8wc9SeKwixW7pAR8K.jpg",
                "z4x0Bp48ar3Mda8KiPD1vwSY3D8.jpg"
        };

        // Check for a valid network connection
        // run query
        // or error
        if (networkOnline()) {
            queryAPI();
            mMovieGrid.setAdapter(new ImageAdapter(this, mMovieId, mPosterUrl));
            mMovieGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    Intent intent = new Intent(MovieGrid.this, MovieDetail.class);
                    intent.putExtra(EXTRA_MESSAGE, String.valueOf(position));
                    startActivity(intent);
                }
            });
        } else {
            showNetworkError();
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

    private void queryAPI() {
        URL url = NetworkUtils.buildUrl("popularity.desc");
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
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            mResponseFromJSON = response;
            showGrid();
        }
    }

}
