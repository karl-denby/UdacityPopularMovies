package com.example.android.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
    SavedFavouriteContract.SavedFavouriteDbHelper mSavedFavouriteDbHelper;
    SQLiteDatabase mDatabase;

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
            mSortType = getString(R.string.sort_pop_desc);
            queryAPI(mSortType);
        }

        if (clickedItem == R.id.rated) {
            mSortType = getString(R.string.sort_avg_desc);
            queryAPI(mSortType);
        }

        if (clickedItem == R.id.favourite) {
            mSortType = getString(R.string.sort_fav);
            showFavourites();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();

        Context context = this.getBaseContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                getString(R.string.app_name),
                Context.MODE_PRIVATE
        );
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.pref_sort_order), mSortType);
        editor.apply();
    }

    @Override
    protected void onStop() {
        super.onStop();

        showProgressIndicator();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Context context = this.getBaseContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                getString(R.string.app_name),
                Context.MODE_PRIVATE
        );

        mSortType = sharedPreferences.getString(getString(R.string.pref_sort_order), getString(R.string.sort_pop_desc));

        if (mSortType.equals(getString(R.string.sort_fav))) {
            showGrid();
        } else {
            if (networkOnline()) {
                queryAPI(mSortType);
                showGrid();
            } else {
                showNetworkError();
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_grid);

        mSavedFavouriteDbHelper = new SavedFavouriteContract.SavedFavouriteDbHelper(this);
        mDatabase = mSavedFavouriteDbHelper.getWritableDatabase();

        mProgressBar = (ProgressBar) findViewById(R.id.pb_grid_loading);
        mMovieGrid = (GridView) findViewById(R.id.gv_grid_posters);
        mErrorText = (TextView) findViewById(R.id.tv_grid_error);

        Context context = this.getBaseContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                getString(R.string.app_name),
                Context.MODE_PRIVATE
        );

        mSortType = sharedPreferences.getString(getString(R.string.pref_sort_order), getString(R.string.sort_pop_desc));

        // onResume will show if fav already, so
        // not fav means check network, run query, show it
        if (!mSortType.equals(getString(R.string.sort_fav))) {
            if (networkOnline()) {
                queryAPI(mSortType);
            } else {
                showNetworkError();
            }
        }
    }

    private void createFavouritesList() {
        // clear out the list
        for (int i = 0; i < 10; i++) {
            mMovieId[i] = null;
            mPosterUrl[i] = null;
        }

        String[] select_col = {
                SavedFavouriteContract.FavEntry._ID,
                SavedFavouriteContract.FavEntry.COLUMN_NAME_MOVIE_POSTER
        };


        Cursor c = mDatabase.query(
                SavedFavouriteContract.FavEntry.TABLE_NAME,    // The table to query
                select_col,                                     // The columns to return
                null,                                           // The columns for the WHERE clause
                null,                                           // The values for the WHERE clause
                null,                                           // don't group the rows
                null,                                           // don't filter by row groups
                null                                            // The sort order
        );

        c.moveToFirst();
        for (int i = 0; i < c.getCount(); i++) {
            mMovieId[i] = c.getString(c.getColumnIndexOrThrow("_id"));
            mPosterUrl[i] = c.getString(c.getColumnIndexOrThrow("poster"));
            c.moveToNext();
        }
        c.close();
    }

    private boolean networkOnline() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showGrid() {
        mMovieGrid.setVisibility(View.VISIBLE);

        mErrorText.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    private void showNetworkError() {
        mErrorText.setVisibility(View.VISIBLE);

        mProgressBar.setVisibility(View.INVISIBLE);
        mMovieGrid.setVisibility(View.INVISIBLE);
    }

    private void showProgressIndicator() {
        mProgressBar.setVisibility(View.VISIBLE);

        mMovieGrid.setVisibility(View.INVISIBLE);
        mErrorText.setVisibility(View.INVISIBLE);
    }

    private void showFavourites() {
        createFavouritesList();

        ImageAdapter adapter = new ImageAdapter(MovieGrid.this, mMovieId, mPosterUrl);
        mMovieGrid.setAdapter(null);
        mMovieGrid.setAdapter(adapter);
        mMovieGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                showProgressIndicator();
                Intent intent = new Intent(MovieGrid.this, MovieDetail.class);
                intent.putExtra(EXTRA_MESSAGE, mMovieId[position]);
                intent.putExtra(getString(R.string.extra_fav), true);
                startActivity(intent);
            }
        });
        showGrid();
    }

    private void queryAPI(String sortOption) {
        URL url = NetworkUtils.buildGridUrl(sortOption);
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
                Log.v(getString(R.string.error_http_response), e.toString());
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
                    intent.putExtra(getString(R.string.extra_data), mResponseFromJSON);
                    startActivity(intent);
                }
            });
            showGrid();
        }
    }

    private void createMovieList(String data) {
        try {
            JSONObject reader = new JSONObject(data);
            JSONArray all_movies = reader.getJSONArray("results");
            for (int i = 0; i < 10; i++) {
                JSONObject movie = all_movies.getJSONObject(i);
                mMovieId[i] = movie.getString("id");
                mPosterUrl[i] = movie.getString("poster_path");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
