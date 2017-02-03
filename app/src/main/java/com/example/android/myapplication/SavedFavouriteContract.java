package com.example.android.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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
        public static final String COLUMN_NAME_MOVIE_PROGRESS = "progress";
        public static final String COLUMN_NAME_MOVIE_RELEASE = "release";
        public static final String COLUMN_NAME_NULLABLE = "NULL";
        // more columns
    }

    // helper definitions
    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                    FeedEntry._ID + " INTEGER PRIMARY KEY," +
                    FeedEntry.COLUMN_NAME_MOVIE_TITLE + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_MOVIE_POSTER + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_MOVIE_OVERVIEW + INTEGER_TYPE +
                    FeedEntry.COLUMN_NAME_MOVIE_PROGRESS + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_MOVIE_RELEASE + TEXT_TYPE +
                    " )";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;

    /**
     *  Maintenance operations Create/upgrade/drop etc.
     */
    public static class SavedFavouriteDbHelper extends SQLiteOpenHelper {
        public static final int DATABASE_VERSION = 1;
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

