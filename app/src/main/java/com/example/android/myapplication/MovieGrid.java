package com.example.android.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class MovieGrid extends AppCompatActivity {

    Toast mToast = null;
    GridView mMovieGrid;
    ProgressBar mProgressBar;
    TextView mErrorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_grid);

        mProgressBar = (ProgressBar) findViewById(R.id.pb_loading);
        mMovieGrid = (GridView) findViewById(R.id.gv_movie_posters);

        // Get rid of the grid interface and show the spinner
        mProgressBar.setVisibility(View.VISIBLE);
        mMovieGrid.setVisibility(View.INVISIBLE);

        // Load the data and setup item click handler
        mMovieGrid.setAdapter(new ImageAdapter(this));
        mMovieGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                if (mToast!=null) {
                    mToast.cancel();
                }

                mToast = Toast.makeText(MovieGrid.this, "" + position,Toast.LENGTH_SHORT);
                mToast.show();

                Intent intent = new Intent(MovieGrid.this, MovieDetail.class);
                intent.putExtra(EXTRA_MESSAGE, String.valueOf(position));
                startActivity(intent);
            }
        });

        // Get rid of the spinner and show the grid interface
        mProgressBar.setVisibility(View.INVISIBLE);
        mMovieGrid.setVisibility(View.VISIBLE);
    }

}
