package com.example.android.myapplication;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class MovieGrid extends AppCompatActivity {

    GridView mMovieGrid;
    ProgressBar mProgressBar;
    TextView mErrorText;

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
        // show grid
        // or error
        if (networkOnline()) {
            mMovieGrid.setAdapter(new ImageAdapter(this));
            mMovieGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    Intent intent = new Intent(MovieGrid.this, MovieDetail.class);
                    intent.putExtra(EXTRA_MESSAGE, String.valueOf(position));
                    startActivity(intent);
                }
            });

            showGrid();

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
}
