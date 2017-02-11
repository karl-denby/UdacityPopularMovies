package com.example.android.myapplication;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class MovieDetail extends AppCompatActivity implements TrailerAdapter.ListItemClickListener {

    String mMovieId = null;
    TextView mMovieTitle;
    ImageView mMoviePoster;
    TextView mMovieOverview;
    RatingBar mMovieProgress;
    TextView mMovieReleaseDate;
    CheckBox mMovieFavourite;
    SavedFavouriteContract.SavedFavouriteDbHelper mSavedFavouriteDbHelper;
    SQLiteDatabase mDatabase;
    ReviewAdapter mReviewAdapter;
    RecyclerView mReviewList;
    TrailerAdapter mTrailerAdapter;
    RecyclerView mTrailerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        Intent intent = getIntent();
        mMovieId = intent.getStringExtra(EXTRA_MESSAGE);
        String data = intent.getStringExtra(getString(R.string.extra_data));
        boolean fav = intent.getBooleanExtra(getString(R.string.extra_fav), false);

        mMovieTitle = (TextView) findViewById(R.id.tv_detail_movie_title);
        mMoviePoster = (ImageView) findViewById(R.id.iv_detail_movie_poster);
        mMovieOverview = (TextView) findViewById(R.id.tv_detail_movie_overview);
        mMovieProgress = (RatingBar) findViewById(R.id.rb_detail_movie_vote_average);
        mMovieReleaseDate = (TextView) findViewById(R.id.tv_detail_movie_release_date);
        mMovieFavourite = (CheckBox) findViewById(R.id.cb_is_favourite_movie);

        mSavedFavouriteDbHelper = new SavedFavouriteContract.SavedFavouriteDbHelper(this);
        mDatabase = mSavedFavouriteDbHelper.getWritableDatabase();

        final String[] movieData;
        if (fav) {
            movieData = setFavouriteDetails(mMovieId);
        } else {
            movieData = setMovieDetails(data);
        }

        mMovieFavourite.setChecked(checkMovieFavourite(mMovieId));
        mMovieFavourite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Toast toast;
                long rowCount;
                if (isChecked) {
                    addMovieFavourite(mMovieId, movieData);
                } else {
                    delMovieFavourite(mMovieId);
                }
            }
        });

        mReviewList = (RecyclerView) findViewById(R.id.rv_reviews);
        LinearLayoutManager reviewLayoutManager = new LinearLayoutManager(this);
        mReviewList.setLayoutManager(reviewLayoutManager);
        mReviewList.setHasFixedSize(true);

        mTrailerList = (RecyclerView) findViewById(R.id.rv_trailers);
        LinearLayoutManager trailerLayoutManager = new LinearLayoutManager(this);
        mTrailerList.setLayoutManager(trailerLayoutManager);
        mTrailerList.setHasFixedSize(true);

        URL url = NetworkUtils.buildReviewUrl(mMovieId);
        ReviewAsyncTask reviewResults = new ReviewAsyncTask();
        reviewResults.execute(url);

        url = NetworkUtils.buildTrailerUrl(mMovieId);
        MovieAsyncTask movieResults = new MovieAsyncTask();
        movieResults.execute(url);
    }

    private String[] setFavouriteDetails(String _id) {
        String[] movieDetails = {"", "", "", "", ""};

        String[] select_col = {
                SavedFavouriteContract.FavEntry._ID,
                SavedFavouriteContract.FavEntry.COLUMN_NAME_MOVIE_TITLE,
                SavedFavouriteContract.FavEntry.COLUMN_NAME_MOVIE_POSTER,
                SavedFavouriteContract.FavEntry.COLUMN_NAME_MOVIE_OVERVIEW,
                //SavedFavouriteContract.FavEntry.COLUMN_NAME_MOVIE_RATING,
                SavedFavouriteContract.FavEntry.COLUMN_NAME_MOVIE_RELEASE
        };
        String where_col = SavedFavouriteContract.FavEntry._ID + "=?";
        String[] where_val = {_id};

        Cursor c = mDatabase.query(
                SavedFavouriteContract.FavEntry.TABLE_NAME,    // The table to query
                select_col,                                     // The columns to return
                where_col,                                      // The columns for the WHERE clause
                where_val,                                      // The values for the WHERE clause
                null,                                           // don't group the rows
                null,                                           // don't filter by row groups
                null                                            // The sort order
        );

        c.moveToFirst();

        String title = c.getString(c.getColumnIndexOrThrow("title"));
        movieDetails[0] = title;
        mMovieTitle.setText(title);

        String poster = c.getString(c.getColumnIndexOrThrow("poster"));
        movieDetails[1] = poster;
        setPoster(poster);

        String overview = c.getString(c.getColumnIndexOrThrow("overview"));
        movieDetails[2] = overview;
        mMovieOverview.setText(overview);

        //String rating = c.getString(c.getColumnIndexOrThrow("rating"));
        //movieDetails[3] = rating;
        mMovieProgress.setRating(5/2);

        String release = c.getString(c.getColumnIndexOrThrow("release"));
        movieDetails[4] = release;
        mMovieReleaseDate.setText(release);

        c.close();

        return movieDetails;
    }

    private String[] setMovieDetails(String data) {
        String movieDetails[] = {"", "", "", "", ""};
        try {
            JSONObject reader = new JSONObject(data);
            JSONArray all_movies = reader.getJSONArray("results");
            JSONObject thisMovie;

            for (int i = 0; i < all_movies.length(); i++) {
                thisMovie = all_movies.getJSONObject(i);
                if (thisMovie.getString("id").equals(mMovieId)) {

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

                    movieDetails[0] = title;
                    movieDetails[1] = poster;
                    movieDetails[2] = overview;
                    movieDetails[3] = String.valueOf(vote_average);
                    movieDetails[4] = release;

                    break;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return movieDetails;
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
        //values.put(SavedFavouriteContract.FavEntry.COLUMN_NAME_MOVIE_RATING, details[3]);
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

                Cursor c = getContentResolver().query(
                        SavedFavouriteContract.MovieEntry.CONTENT_URI,
                        projection,
                        selection,
                        selectionArgs,
                        null
                );

        if (c.getCount() > 0) {
            c.close();
            return true;
        } else {
            c.close();
            return false;
        }
    }

    private class ReviewAsyncTask extends AsyncTask<URL, Void, String> {
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

            String[] authors = {"", "", ""};
            String[] reviews = {"", "", ""};

            // author/content for review author/body
            try {
                JSONObject reader = new JSONObject(response);
                JSONArray all_reviews = reader.getJSONArray("results");
                for (int i = 0; i < 3; i++) {
                    JSONObject review = all_reviews.getJSONObject(i);
                    authors[i] = review.getString("author");
                    reviews[i] = review.getString("content");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mReviewAdapter = new ReviewAdapter(authors.length, authors, reviews);
            mReviewList.setAdapter(mReviewAdapter);
        }
    }

    private class MovieAsyncTask extends AsyncTask<URL, Void, String> {
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

            String[] vid_id = {"", "", ""};
            String[] vid_name = {"", "", ""};

            // key/name for video id/title
            try {
                JSONObject reader = new JSONObject(response);
                JSONArray all_reviews = reader.getJSONArray("results");
                for (int i = 0; i < 3; i++) {
                    JSONObject review = all_reviews.getJSONObject(i);
                    vid_id[i] = review.getString("key");
                    vid_name[i] = review.getString("name");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mTrailerAdapter = new TrailerAdapter(vid_id.length, MovieDetail.this, vid_name, vid_id);
            mTrailerList.setAdapter(mTrailerAdapter);
        }
    }


    @Override
    public void onListItemClick(String youtubeKey) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + youtubeKey)));
    }

}
