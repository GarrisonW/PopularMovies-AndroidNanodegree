package com.garrison.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.garrison.popularmovies.data.PopularMoviesContract.*;

/**
 * Created by Garrison on 8/1/2015.
 */
public class PopularMoviesProvider extends ContentProvider {

    private final String LOG_TAG = PopularMoviesProvider.class.getSimpleName();

    PopularMoviesDBHelper mPopularMoviesDBHelper;
    UriMatcher sUriMatcher = buildUriMatcher();

    private static final int MOVIES = 100;
    private static final int MOVIES_BY_ID = 101;

    private static final int TRAILERS = 200;
    private static final int TRAILERS_BY_ID = 201;
    private static final int TRAILERS_BY_MOVIE= 202;

    private static final int REVIEWS = 300;
    private static final int REVIEWS_BY_ID = 301;
    private static final int REVIEWS_BY_MOVIE = 302;


    @Override
    public boolean onCreate() {
        //CLEAR DATABASE FOR TESTING:
//getContext().deleteDatabase(mPopularMoviesDBHelper.DATABASE_NAME);
        mPopularMoviesDBHelper = new PopularMoviesDBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String queryString, String[] selectionArgs, String sortOrder) {

        Cursor retCursor;
        SQLiteDatabase db = mPopularMoviesDBHelper.getReadableDatabase();

        switch (sUriMatcher.match(uri)) {
            case MOVIES:
            {
                retCursor = db.query(
                        MoviesTable.TABLE_NAME,
                        projection,
                        queryString,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );

                break;
            }
            case MOVIES_BY_ID:
            {
                retCursor = db.query(
                        MoviesTable.TABLE_NAME,
                        projection,
                        MoviesTable._ID + " = " + ContentUris.parseId(uri),
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );

                break;
            }
            case TRAILERS:
            {
                retCursor = db.query(
                        TrailersTable.TABLE_NAME,
                        projection,
                        queryString,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );

                break;
            }
            case TRAILERS_BY_ID:
            {
                retCursor = db.query(
                        TrailersTable.TABLE_NAME,
                        projection,
                        TrailersTable._ID + " = " + ContentUris.parseId(uri),
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );

                break;
            }
            case TRAILERS_BY_MOVIE:
            {
                retCursor = db.query(
                        TrailersTable.TABLE_NAME,
                        projection,
                        TrailersTable.COLUMN_MOVIE_ID + " = " + ContentUris.parseId(uri),
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );

                break;
            }
            case REVIEWS:
            {
                retCursor = db.query(
                        ReviewsTable.TABLE_NAME,
                        projection,
                        queryString,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );

                break;
            }
            case REVIEWS_BY_ID:
            {
                retCursor = db.query(
                        ReviewsTable.TABLE_NAME,
                        projection,
                        ReviewsTable._ID + " = " + ContentUris.parseId(uri),
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );

                break;
            }
            case REVIEWS_BY_MOVIE:
            {
                retCursor = db.query(
                        ReviewsTable.TABLE_NAME,
                        projection,
                        ReviewsTable.COLUMN_MOVIE_ID + " = " + ContentUris.parseId(uri),
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );

                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {

        final int myMatch =  sUriMatcher.match(uri);

        switch (myMatch) {
            case MOVIES:
                return MoviesTable.CONTENT_TYPE;
            case MOVIES_BY_ID:
                return MoviesTable.CONTENT_ITEM_TYPE;
            case TRAILERS:
                return TrailersTable.CONTENT_TYPE;
            case TRAILERS_BY_ID:
                return TrailersTable.CONTENT_ITEM_TYPE;
            case TRAILERS_BY_MOVIE:
                return TrailersTable.CONTENT_ITEM_TYPE;
            case REVIEWS:
                return ReviewsTable.CONTENT_TYPE;
            case REVIEWS_BY_ID:
                return ReviewsTable.CONTENT_ITEM_TYPE;
            case REVIEWS_BY_MOVIE:
                return ReviewsTable.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        SQLiteDatabase db = mPopularMoviesDBHelper.getWritableDatabase();

        int myMatch = sUriMatcher.match(uri);
        Uri returnUri;
        long _id = 0;

        switch (myMatch) {
            case MOVIES: {
                try{
                    _id = db.insert(MoviesTable.TABLE_NAME, null, contentValues);
Log.v(LOG_TAG, "_ID: " + _id + " movie: " + contentValues.getAsString(MoviesTable.COLUMN_MOVIE_TITLE));
                }
                catch (Exception dbEx){
                    Log.e(LOG_TAG, "We have a db error on insert (MOVIES): " + dbEx.getMessage());
                }

                if ( _id > 0 )
                    returnUri = MoviesTable.buildMovieUri(_id);
                else {
                    throw new android.database.SQLException("Failed to insert row into (MOVIES)" + uri);
                }
                break;
            }
            case TRAILERS: {
                try{
                    _id = db.insert(TrailersTable.TABLE_NAME, null, contentValues);
                }
                catch (Exception dbEx){
                    Log.e(LOG_TAG, "We have a db error on insert (TRAILERS): " + dbEx.getMessage());
                }

                if ( _id > 0 )
                    returnUri = TrailersTable.buildTrailerUri(_id);
                else {
                    throw new android.database.SQLException("Failed to insert row into TRAILERS" + uri);
                }
                break;
            }
            case REVIEWS: {
                try{
                    _id = db.insert(ReviewsTable.TABLE_NAME, null, contentValues);
                }
                catch (Exception dbEx){
                    Log.e(LOG_TAG, "We have a db error on insert (REVIEWS): " + dbEx.getMessage());
                }

                if ( _id > 0 )
                    returnUri = ReviewsTable.buildReviewUri(_id);
                else {
                    throw new android.database.SQLException("Failed to insert row into (REVIEWS)" + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }

        //  This notifies any Content Observers (i.e. other apps) that the insert on the URI has changed
        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int numRows = 0;

        SQLiteDatabase db = mPopularMoviesDBHelper.getWritableDatabase();
Log.v(LOG_TAG, "DELETE STRING: " + selection);
        final int myMatch = sUriMatcher.match(uri);
        switch (myMatch) {
            case MOVIES: {
                try {

Log.v(LOG_TAG, "DELETE IN RIGHT PLACE");
                    numRows = db.delete(MoviesTable.TABLE_NAME,
                            selection,
                            null
                    );
                } catch (Exception dbEx) {
                    Log.e(LOG_TAG, "We have a db error on delete (MOVIES): " + dbEx.getMessage());
                }
                break;
            }
            case TRAILERS: {
                try {
                    numRows = db.delete(TrailersTable.TABLE_NAME,
                            selection,
                            null
                    );
                } catch (Exception dbEx) {
                    Log.e(LOG_TAG, "We have a db error on delete (TRAILERS): " + dbEx.getMessage());
                }
                break;
            }
            case REVIEWS: {
                try {
                    numRows = db.delete(ReviewsTable.TABLE_NAME,
                            selection,
                            null
                    );
                } catch (Exception dbEx) {
                    Log.e(LOG_TAG, "We have a db error on delete (REVIEWS): " + dbEx.getMessage());
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);

        }
        Log.v(LOG_TAG, " DELETEING DONE " + numRows + " ROWS");

        return numRows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int numRows = 0;

        SQLiteDatabase db = mPopularMoviesDBHelper.getWritableDatabase();

        final int myMatch = sUriMatcher.match(uri);
        switch (myMatch) {
            case MOVIES: {
                try {
                    Log.v(LOG_TAG, "Right spot: " + selection);
                    numRows = db.update(MoviesTable.TABLE_NAME,
                            values,
                            selection,
                            null
                    );
                } catch (Exception dbEx) {
                    Log.e(LOG_TAG, "We have a db error on UPDATE (MOVIES): " + dbEx.getMessage());
                }
                break;
            }
            case TRAILERS: {
                try {
                    Log.v(LOG_TAG, "RWRONG");
                    numRows = db.update(TrailersTable.TABLE_NAME,
                            values,
                            selection,
                            null
                    );
                } catch (Exception dbEx) {
                    Log.e(LOG_TAG, "We have a db error on UPDATE (TRAILERS): " + dbEx.getMessage());
                }
                break;
            }
            case REVIEWS: {
                try {
                    Log.v(LOG_TAG, "RWRONG");
                    numRows = db.update(ReviewsTable.TABLE_NAME,
                            values,
                            selection,
                            null
                    );
                } catch (Exception dbEx) {
                    Log.e(LOG_TAG, "We have a db error on UPDATE (REVIEWS): " + dbEx.getMessage());
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);

        }
        Log.v(LOG_TAG, " UPDATING DONE ");

        return numRows;
    }

    public static UriMatcher buildUriMatcher(){

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        String authority = PopularMoviesContract.CONTENT_AUTHORITY;

        uriMatcher.addURI(authority, PopularMoviesContract.PATH_MOVIES, MOVIES);
        uriMatcher.addURI(authority, PopularMoviesContract.PATH_MOVIES + "/#", MOVIES_BY_ID);

        uriMatcher.addURI(authority, PopularMoviesContract.PATH_TRAILERS, TRAILERS);
        uriMatcher.addURI(authority, PopularMoviesContract.PATH_TRAILERS + "/#", TRAILERS_BY_ID);
        uriMatcher.addURI(authority, PopularMoviesContract.PATH_TRAILERS + "/#", TRAILERS_BY_MOVIE);

        uriMatcher.addURI(authority, PopularMoviesContract.PATH_REVIEWS, REVIEWS);
        uriMatcher.addURI(authority, PopularMoviesContract.PATH_REVIEWS + "/#", REVIEWS_BY_ID);
        uriMatcher.addURI(authority, PopularMoviesContract.PATH_REVIEWS + "/#", REVIEWS_BY_MOVIE);

        return uriMatcher;
    }
}
