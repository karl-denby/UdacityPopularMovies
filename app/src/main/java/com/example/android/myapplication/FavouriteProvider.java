package com.example.android.myapplication;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;


public class FavouriteProvider extends ContentProvider {

    // DB that this ContentProvider uses
    Context context;
    SavedFavouriteContract.SavedFavouriteDbHelper mSavedFavouriteDbHelper;

    // All URIs share these parts
    public static final String CONTENT = "content://";
    public static final String AUTHORITY = "com.example.android.myapplication.provider";
    public static final String PATH = "/movies";

    // URI Matcher
    public static final int FAVOURITES = 100;
    public static final int FAVOURITE_WITH_ID = 101;
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, PATH, FAVOURITES);
        uriMatcher.addURI(AUTHORITY, PATH + "/#", FAVOURITE_WITH_ID);
        return uriMatcher;
    }

    // Initialize
    public FavouriteProvider() {}

    @Override
    public boolean onCreate() {
        context = getContext();
        mSavedFavouriteDbHelper = new SavedFavouriteContract.SavedFavouriteDbHelper(context);
        return true;
    }

    // Create
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {

        final SQLiteDatabase db = mSavedFavouriteDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case (FAVOURITES):
                long id = db.insert(SavedFavouriteContract.FavEntry.TABLE_NAME, null, values);
                db.close();

                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(SavedFavouriteContract.BASE_CONTENT_URI, id);
                } else {
                    throw new UnsupportedOperationException("Failed to insert row: " + uri.toString());
                }

                break;

            default:
                throw new UnsupportedOperationException("Unknown URI");
        }
        return returnUri;
    }

    // Read
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        final SQLiteDatabase db = mSavedFavouriteDbHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor returnCursor;

        switch (match) {
            case (FAVOURITES):
                returnCursor = db.query(
                        SavedFavouriteContract.FavEntry.TABLE_NAME, // The table to query
                        projection,                                 // The columns to return
                        selection,                                  // The columns for the WHERE clause
                        selectionArgs,                              // The values for the WHERE clause
                        null,                                       // don't group the rows
                        null,                                       // don't filter by row groups
                        sortOrder                                   // The sort order
                );
                break;

            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }
        return returnCursor;
    }

    // Update
    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // Delete
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mSavedFavouriteDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);

        switch (match) {
            case (FAVOURITES):
                long deletedRowCount = db.delete(SavedFavouriteContract.FavEntry.TABLE_NAME, selection, selectionArgs);
                db.close();
                if (deletedRowCount > 0) {
                    return 0;
                } else {
                    //throw new UnsupportedOperationException("Delete Error: " + uri.toString());
                    return 1;
                }
            default:
                throw new UnsupportedOperationException("Unknown URI");
        }
    }

    // MIME
    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
