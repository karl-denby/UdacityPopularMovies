package com.example.android.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;

public final class SavedFavouriteContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public SavedFavouriteContract() {}

    /* Inner class that defines the table contents */
    public static abstract class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "movies";
        public static final String _ID = "_id";
        public static final String COLUMN_NAME_MOVIE_TITLE = "title";
        public static final String COLUMN_NAME_MOVIE_POSTER = "poster";
        public static final String COLUMN_NAME_MOVIE_OVERVIEW = "overview";
        public static final String COLUMN_NAME_MOVIE_RATING = "rating";
        public static final String COLUMN_NAME_MOVIE_RELEASE = "release";
        public static final String COLUMN_NAME_NULLABLE = "NULL";
        // more columns
    }

    // helper definitions
    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INT";
    private static final String FLOAT_TYPE = " FLOAT";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                    FeedEntry._ID + " INTEGER PRIMARY KEY," +
                    FeedEntry.COLUMN_NAME_MOVIE_TITLE + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_MOVIE_POSTER + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_MOVIE_OVERVIEW + INTEGER_TYPE +
                    FeedEntry.COLUMN_NAME_MOVIE_RATING + FLOAT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_MOVIE_RELEASE + TEXT_TYPE +
                    " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;

    // The authority, which is how your code knows which Content Provider to access
    public static final String AUTHORITY = "com.example.android.myapplication.provider";

    // The base content URI = "content://" + <authority>
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    // Define the possible paths for accessing data in this contract
    // This is the path for the "tasks" directory
    public static final String PATH_MOVIES = "movies";

    /* TaskEntry is an inner class that defines the contents of the task table */
    public static final class TaskEntry implements BaseColumns {

        // TaskEntry content URI = base content URI + path
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        // Task table and column names
        public static final String TABLE_NAME = "movies";

        // Since TaskEntry implements the interface "BaseColumns", it has an automatically produced
        // "_ID" column in addition to the two below
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_PRIORITY = "priority";
    }

    /**
     *  Maintenance operations Create/upgrade/drop etc.
     */
    public static class SavedFavouriteDbHelper extends SQLiteOpenHelper {
        public static final int DATABASE_VERSION = 6;
        public static final String DATABASE_NAME = "Movies.db";

        public SavedFavouriteDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }

        public void deleteDatabase(SQLiteDatabase db) {
            db.execSQL(SQL_DELETE_ENTRIES);
        }
    }

}