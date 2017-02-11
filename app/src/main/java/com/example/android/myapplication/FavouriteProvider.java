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


public class FavouriteProvider extends ContentProvider {

    // DB that this ContentProvider uses
    Context context;
    SavedFavouriteContract.SavedFavouriteDbHelper mSavedFavouriteDbHelper;

    // All URIs share these parts
    public static final String CONTENT = "content://";
    public static final String AUTHORITY = "com.example.android.myapplication.provider";
    public static final String PATH = "/movies";

    // URI Matcher constants
    public static final int FAVOURITES = 100;
    public static final int FAVOURITE_WITH_ID = 101;
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // directory and individual uri's
        uriMatcher.addURI(AUTHORITY, PATH, FAVOURITES);
        uriMatcher.addURI(AUTHORITY, PATH + "/#", FAVOURITE_WITH_ID);

        return uriMatcher;
    }

    // Initialize
    public FavouriteProvider() {
    }

    @Override
    public boolean onCreate() {
        context = getContext();
        mSavedFavouriteDbHelper = new SavedFavouriteContract.SavedFavouriteDbHelper(context);
        return true;
    }

    // Create
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        //final SQLiteDatabase db = .getWritableDatabase();
        //int match = builtUriMatcher(uri, values);
        final SQLiteDatabase db = mSavedFavouriteDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case (FAVOURITES):
                long id = db.insert(SavedFavouriteContract.FeedEntry.TABLE_NAME, null, values);
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
        // TODO: Implement this to handle query requests from clients.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // Update
    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // Delete
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // Other
    @Override
    public String getType(@NonNull Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
