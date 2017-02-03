package com.example.android.myapplication;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class MovieDetail extends AppCompatActivity {

    String mMovieId = null;
    TextView mMovieTitle;
    ImageView mMoviePoster;
    TextView mMovieOverview;
    RatingBar mMovieProgress;
    TextView mMovieReleaseDate;
    CheckBox mMovieFavourite;
    SavedFavouriteContract.SavedFavouriteDbHelper mSavedFavouriteDbHelper;
    SQLiteDatabase mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        Intent intent = getIntent();
        mMovieId = intent.getStringExtra(EXTRA_MESSAGE);
        String data = intent.getStringExtra("DATA");

        mMovieTitle = (TextView) findViewById(R.id.tv_detail_movie_title);
        mMoviePoster = (ImageView) findViewById(R.id.iv_detail_movie_poster);
        mMovieOverview = (TextView) findViewById(R.id.tv_detail_movie_overview);
        mMovieProgress = (RatingBar) findViewById(R.id.rb_detail_movie_vote_average);
        mMovieReleaseDate = (TextView) findViewById(R.id.tv_detail_movie_release_date);
        mMovieFavourite = (CheckBox) findViewById(R.id.cb_is_favourite_movie);

        mSavedFavouriteDbHelper = new SavedFavouriteContract.SavedFavouriteDbHelper(this);
        mDatabase = mSavedFavouriteDbHelper.getWritableDatabase();

        mMovieFavourite.setChecked(checkMovieFavourite(mMovieId));
        mMovieFavourite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Toast toast;
                long rowCount;
                if (isChecked) {
                    rowCount = addMovieFavourite(mMovieId);
                    toast = Toast.makeText(MovieDetail.this, "added " + rowCount, Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    rowCount = delMovieFavourite(mMovieId);
                    toast = Toast.makeText(MovieDetail.this, "removed " + rowCount, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        setMovieDetails(data);
    }

    private void setMovieDetails(String data) {
        try {
            JSONObject reader = new JSONObject(data);
            JSONArray all_movies = reader.getJSONArray("results");
            JSONObject thisMovie;

            for (int i = 0; i < all_movies.length(); i++) {
                thisMovie = all_movies.getJSONObject(i);
                if (thisMovie.getString("id").equals(mMovieId)) {
                    mMovieTitle.setText(thisMovie.getString("title"));
                    setPoster(thisMovie.getString("poster_path"));
                    mMovieOverview.setText((thisMovie.getString("overview")));
                    float vote_average = thisMovie.getLong("vote_average") / 2;
                    mMovieProgress.setRating(vote_average);
                    mMovieReleaseDate.setText(thisMovie.getString("release_date"));
                    break;
                }
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

    private long addMovieFavourite(String _id) {
        ContentValues values = new ContentValues();
        values.put(SavedFavouriteContract.FeedEntry._ID, _id);

        return mDatabase.insert(
                SavedFavouriteContract.FeedEntry.TABLE_NAME,
                SavedFavouriteContract.FeedEntry.COLUMN_NAME_NULLABLE,
                values
        );
    }

    private long delMovieFavourite(String _id) {
        String selection = SavedFavouriteContract.FeedEntry._ID + " LIKE ?";  // WHERE col_name LIKE ?
        String[] selectionArgs = new String[]{ String.valueOf(_id) };

        return mDatabase.delete(
                SavedFavouriteContract.FeedEntry.TABLE_NAME,
                selection,
                selectionArgs
        );
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

}
