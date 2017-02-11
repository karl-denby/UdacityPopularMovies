package com.example.android.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;

final class SavedFavouriteContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public SavedFavouriteContract() {}

    /* Inner class that defines the table contents */
    static abstract class FavEntry implements BaseColumns {
        static final String TABLE_NAME = "movies";
        static final String _ID = "_id";
        static final String COLUMN_NAME_MOVIE_TITLE = "title";
        static final String COLUMN_NAME_MOVIE_POSTER = "poster";
        static final String COLUMN_NAME_MOVIE_OVERVIEW = "overview";
        static final String COLUMN_NAME_MOVIE_RATING = "rating";
        static final String COLUMN_NAME_MOVIE_RELEASE = "release";
        public static final String COLUMN_NAME_NULLABLE = "NULL";
        // more columns
    }

    // helper definitions
    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INT";
    private static final String FLOAT_TYPE = " FLOAT";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FavEntry.TABLE_NAME + " (" +
                    FavEntry._ID + " INTEGER PRIMARY KEY," +
                    FavEntry.COLUMN_NAME_MOVIE_TITLE + TEXT_TYPE + COMMA_SEP +
                    FavEntry.COLUMN_NAME_MOVIE_POSTER + TEXT_TYPE + COMMA_SEP +
                    FavEntry.COLUMN_NAME_MOVIE_OVERVIEW + INTEGER_TYPE +
                    FavEntry.COLUMN_NAME_MOVIE_RATING + FLOAT_TYPE + COMMA_SEP +
                    FavEntry.COLUMN_NAME_MOVIE_RELEASE + TEXT_TYPE +
                    " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FavEntry.TABLE_NAME;


    private static final String AUTHORITY = "com.example.android.myapplication.provider";
    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    private static final String PATH_MOVIES = "movies";

    /* MovieEntry is an inner class that defines the contents of the task table */
    static final class MovieEntry implements BaseColumns {

        // MovieEntry content URI = base content URI + path
        static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        // Task table and column names
        public static final String TABLE_NAME = "movies";

        // Since MovieEntry implements the interface "BaseColumns", it has an automatically produced
        // "_ID" column in addition to the two below
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_PRIORITY = "priority";
    }

    /**
     *  Maintenance operations Create/upgrade/drop etc.
     */
    static class SavedFavouriteDbHelper extends SQLiteOpenHelper {
        static final int DATABASE_VERSION = 6;
        static final String DATABASE_NAME = "Movies.db";

        SavedFavouriteDbHelper(Context context) {
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