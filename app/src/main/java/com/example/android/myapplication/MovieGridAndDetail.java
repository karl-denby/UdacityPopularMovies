package com.example.android.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class MovieGridAndDetail extends AppCompatActivity {

    GridView mMovieGrid;
    int mMovieGridPosition;
    ProgressBar mProgressBar;
    TextView mErrorText;
    String mResponseFromJSON;
    String[] mMovieId = new String[10];
    String[] mPosterUrl = new String[10];
    String mSortType;
    SavedFavouriteContract.SavedFavouriteDbHelper mSavedFavouriteDbHelper;
    SQLiteDatabase mDatabase;

    TextView mMovieTitle;
    ImageView mMoviePoster;
    TextView mMovieOverview;
    RatingBar mMovieProgress;
    TextView mMovieReleaseDate;
    CheckBox mMovieFavourite;



    String mMovieDetails[] = {"", "", "", "", "", ""};
    String mSelectedMovieId = "";

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
        editor.putInt(getString(R.string.grid_position), mMovieGrid.getFirstVisiblePosition());
        editor.apply();
    }

    @Override
    protected void onStop() {
        super.onStop();

        showProgressIndicator();
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        Log.v("INSIDE: ", "onSaveInstanceState");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.v("INSIDE: ", "onRestoreInstanceState");
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
        mMovieGridPosition = sharedPreferences.getInt(getString(R.string.grid_position), 0);

        if (mSortType.equals(getString(R.string.sort_fav))) {
            showFavourites();
        } else {
            showGrid();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_grid_and_detail);

        mSavedFavouriteDbHelper = new SavedFavouriteContract.SavedFavouriteDbHelper(this);
        mDatabase = mSavedFavouriteDbHelper.getWritableDatabase();

        mProgressBar = (ProgressBar) findViewById(R.id.pb_grid_loading);
        mMovieGrid = (GridView) findViewById(R.id.gv_grid_posters);
        mErrorText = (TextView) findViewById(R.id.tv_grid_error);

        mMovieTitle = (TextView) findViewById(R.id.tv_detail_movie_title);
        mMoviePoster = (ImageView) findViewById(R.id.iv_detail_movie_poster);
        mMovieOverview = (TextView) findViewById(R.id.tv_detail_movie_overview);
        mMovieProgress = (RatingBar) findViewById(R.id.rb_detail_movie_vote_average);
        mMovieReleaseDate = (TextView) findViewById(R.id.tv_detail_movie_release_date);
        mMovieFavourite = (CheckBox) findViewById(R.id.cb_is_favourite_movie);

        mMovieFavourite.setChecked(checkMovieFavourite(mSelectedMovieId));
        mMovieFavourite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Toast toast;
                long rowCount;
                if (isChecked) {
                    addMovieFavourite(mSelectedMovieId, mMovieDetails);
                } else {
                    delMovieFavourite(mSelectedMovieId);
                }
            }
        });


        Context context = this.getBaseContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                getString(R.string.app_name),
                Context.MODE_PRIVATE
        );

        mSortType = sharedPreferences.getString(getString(R.string.pref_sort_order), getString(R.string.sort_pop_desc));
        mMovieGridPosition = sharedPreferences.getInt(getString(R.string.grid_position), 0);

        // onResume will show if fav already, so
        // not fav means check network, run query, show it
        if (!mSortType.equals(getString(R.string.sort_fav))) {
            if (networkOnline()) {
                queryAPI(mSortType);
                showGrid();
            } else {
                showNetworkError();
            }
        }
    }

    private void createFavouritesList() {

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

        // clear out the list
        mMovieId = new String[c.getCount()];
        mPosterUrl = new String[c.getCount()];

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
        mMovieGrid.setSelection(mMovieGridPosition);

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

        ImageAdapter adapter = new ImageAdapter(MovieGridAndDetail.this, mMovieId, mPosterUrl);
        mMovieGrid.setAdapter(null);
        if (mMovieGrid.getAdapter() == null) {
            mMovieGrid.setAdapter(adapter);
            mMovieGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    //showProgressIndicator();
                    //Intent intent = new Intent(MovieGridAndDetail.this, MovieDetail.class);
                    //intent.putExtra(EXTRA_MESSAGE, mMovieId[position]);
                    //intent.putExtra(getString(R.string.extra_fav), true);
                    //startActivity(intent);
                    Toast.makeText(MovieGridAndDetail.this, "Item: " + position, Toast.LENGTH_SHORT).show();
                }
            });
        }
        showGrid();
    }

    private void queryAPI(String sortOption) {
        URL url = NetworkUtils.buildGridUrl(sortOption);
        MovieGridAndDetail.QueryAsyncTask results = new MovieGridAndDetail.QueryAsyncTask();
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

            ImageAdapter adapter = new ImageAdapter(MovieGridAndDetail.this, mMovieId, mPosterUrl);
            mMovieGrid.setAdapter(null);
            if (mMovieGrid.getAdapter() == null) {
                mMovieGrid.setAdapter(adapter);
                mMovieGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                        try {
                            JSONObject reader = new JSONObject(mResponseFromJSON);
                            JSONArray all_movies = reader.getJSONArray("results");
                            JSONObject thisMovie;

                            for (int i = 0; i < all_movies.length(); i++) {
                                thisMovie = all_movies.getJSONObject(i);
                                if (thisMovie.getString("id").equals(mMovieId[position])) {

                                    String title = thisMovie.getString("title");
                                    mMovieTitle.setText(title);

                                    String poster = thisMovie.getString("poster_path");
                                    setPoster(poster);

                                    String overview = thisMovie.getString("overview");
                                    mMovieOverview.setText(overview);

                                    float vote_average = thisMovie.getLong("vote_average");
                                    mMovieProgress.setRating(vote_average / 2);

                                    String release = thisMovie.getString("release_date");
                                    mMovieReleaseDate.setText(release);

                                    mSelectedMovieId = mMovieId[position];
                                    mMovieDetails[0] = title;
                                    mMovieDetails[1] = poster;
                                    mMovieDetails[2] = overview;
                                    mMovieDetails[3] = String.valueOf(vote_average);
                                    mMovieDetails[4] = release;

                                    mMovieFavourite.setOnCheckedChangeListener(null);
                                    mMovieFavourite.setChecked(checkMovieFavourite(mMovieId[position]));
                                    mMovieFavourite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                        @Override
                                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                            Toast toast;
                                            long rowCount;
                                            if (isChecked) {
                                                addMovieFavourite(mSelectedMovieId, mMovieDetails);
                                            } else {
                                                delMovieFavourite(mSelectedMovieId);
                                            }
                                        }
                                    });


                                    break;
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            showGrid();
        }
    }

    private void createMovieList(String data) {
        try {
            JSONObject reader = new JSONObject(data);
            JSONArray all_movies = reader.getJSONArray("results");
            for (int i = 0; i < all_movies.length(); i++) {
                JSONObject movie = all_movies.getJSONObject(i);
                mMovieId[i] = movie.getString("id");
                mPosterUrl[i] = movie.getString("poster_path");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setPoster(String poster_path) {
        String base_url = "http://image.tmdb.org/t/p/w185";

        Picasso
                .with(this)
                .load(base_url + poster_path)
                .fit()
                .centerCrop()
                .into(mMoviePoster);
    }

    private void addMovieFavourite(String _id, String[] details) {
        ContentValues values = new ContentValues();
        values.put(SavedFavouriteContract.FavEntry._ID, _id);
        values.put(SavedFavouriteContract.FavEntry.COLUMN_NAME_MOVIE_TITLE, details[0]);
        values.put(SavedFavouriteContract.FavEntry.COLUMN_NAME_MOVIE_POSTER, details[1]);
        values.put(SavedFavouriteContract.FavEntry.COLUMN_NAME_MOVIE_OVERVIEW, details[2]);
        values.put(SavedFavouriteContract.FavEntry.COLUMN_NAME_MOVIE_RATING, details[3]);
        values.put(SavedFavouriteContract.FavEntry.COLUMN_NAME_MOVIE_RELEASE, details[4]);

        getContentResolver().insert(SavedFavouriteContract.MovieEntry.CONTENT_URI, values);
    }

    private void delMovieFavourite(String _id) {
        String selection = SavedFavouriteContract.FavEntry._ID + " LIKE ?";  // WHERE col_name LIKE ?
        String[] selectionArgs = new String[]{ String.valueOf(_id) };
        getContentResolver().delete(SavedFavouriteContract.MovieEntry.CONTENT_URI, selection, selectionArgs);
    }

    private boolean checkMovieFavourite(String _id) {

        String[] projection = {SavedFavouriteContract.FavEntry._ID};
        String selection = SavedFavouriteContract.FavEntry._ID + "=?";
        String[] selectionArgs = {_id};

        Cursor fav = getContentResolver().query(
                SavedFavouriteContract.MovieEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
        );

        if (fav.getCount() > 0) {
            fav.close();
            return true;
        } else {
            fav.close();
            return false;
        }

    }


}
