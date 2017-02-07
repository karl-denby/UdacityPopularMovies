package com.example.android.myapplication;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
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

public class MovieDetail extends AppCompatActivity implements ReviewAdapter.ListItemClickListener {

    String mMovieId = null;
    TextView mMovieTitle;
    ImageView mMoviePoster;
    TextView mMovieOverview;
    RatingBar mMovieProgress;
    TextView mMovieReleaseDate;
    CheckBox mMovieFavourite;
    SavedFavouriteContract.SavedFavouriteDbHelper mSavedFavouriteDbHelper;
    SQLiteDatabase mDatabase;
    private static final int NUM_REVIEW_ITEMS = 10;
    private ReviewAdapter mReviewAdapter;
    private RecyclerView mReviewList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        Intent intent = getIntent();
        mMovieId = intent.getStringExtra(EXTRA_MESSAGE);
        String data = intent.getStringExtra("DATA");
        boolean fav = intent.getBooleanExtra("FAV", false);

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



        URL url = NetworkUtils.buildReviewUrl(mMovieId);
        QueryAsyncTask results = new QueryAsyncTask();
        results.execute(url);

//        url = NetworkUtils.buildTrailerUrl(mMovieId);
//        results = new QueryAsyncTask();
//        results.execute(url);

    }

    private String[] setFavouriteDetails(String _id) {
        String[] movieDetails = {"", "", "", "", ""};

        String[] select_col = {
                SavedFavouriteContract.FeedEntry._ID,
                SavedFavouriteContract.FeedEntry.COLUMN_NAME_MOVIE_TITLE,
                SavedFavouriteContract.FeedEntry.COLUMN_NAME_MOVIE_POSTER,
                SavedFavouriteContract.FeedEntry.COLUMN_NAME_MOVIE_OVERVIEW,
                //SavedFavouriteContract.FeedEntry.COLUMN_NAME_MOVIE_RATING,
                SavedFavouriteContract.FeedEntry.COLUMN_NAME_MOVIE_RELEASE
        };
        String where_col = SavedFavouriteContract.FeedEntry._ID + "=?";
        String[] where_val = {_id};

        Cursor c = mDatabase.query(
                SavedFavouriteContract.FeedEntry.TABLE_NAME,    // The table to query
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

    private long addMovieFavourite(String _id, String[] details) {
        ContentValues values = new ContentValues();
        values.put(SavedFavouriteContract.FeedEntry._ID, _id);
        values.put(SavedFavouriteContract.FeedEntry.COLUMN_NAME_MOVIE_TITLE, details[0]);
        values.put(SavedFavouriteContract.FeedEntry.COLUMN_NAME_MOVIE_POSTER, details[1]);
        values.put(SavedFavouriteContract.FeedEntry.COLUMN_NAME_MOVIE_OVERVIEW, details[2]);
        //values.put(SavedFavouriteContract.FeedEntry.COLUMN_NAME_MOVIE_RATING, details[3]);
        values.put(SavedFavouriteContract.FeedEntry.COLUMN_NAME_MOVIE_RELEASE, details[4]);

        long dbResult = mDatabase.insert(
                SavedFavouriteContract.FeedEntry.TABLE_NAME,
                SavedFavouriteContract.FeedEntry.COLUMN_NAME_NULLABLE,
                values
        );
        mDatabase.close();
        return dbResult;
    }

    private long delMovieFavourite(String _id) {
        String selection = SavedFavouriteContract.FeedEntry._ID + " LIKE ?";  // WHERE col_name LIKE ?
        String[] selectionArgs = new String[]{ String.valueOf(_id) };

        long dbResult =  mDatabase.delete(
                SavedFavouriteContract.FeedEntry.TABLE_NAME,
                selection,
                selectionArgs
        );
        mDatabase.close();
        return dbResult;
    }

    private boolean checkMovieFavourite(String _id) {

        String[] select_col = {SavedFavouriteContract.FeedEntry._ID};
        String where_col = SavedFavouriteContract.FeedEntry._ID + "=?";
        String[] where_val = {_id};

        Cursor c = mDatabase.query(
                SavedFavouriteContract.FeedEntry.TABLE_NAME,    // The table to query
                select_col,                                     // The columns to return
                where_col,                                      // The columns for the WHERE clause
                where_val,                                      // The values for the WHERE clause
                null,                                           // don't group the rows
                null,                                           // don't filter by row groups
                null                                            // The sort order
        );

        if (c.getCount() > 0) {
            c.close();
            return true;
        } else {
            c.close();
            return false;
        }
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

            String[] authors = {"", "", ""};
            String[] reviews = {"", "", ""};

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

            mReviewAdapter = new ReviewAdapter(authors.length, MovieDetail.this, authors, reviews);
            mReviewList.setAdapter(mReviewAdapter);
        }
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        Toast.makeText(MovieDetail.this, "Item #" + clickedItemIndex + " clicked", Toast.LENGTH_SHORT).show();
    }
}
