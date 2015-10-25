package com.garrison.popularmovies.data;

/**
 * Created by Garrison on 10/15/2014.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.garrison.popularmovies.data.PopularMoviesContract.*;

/**
 * Created by Garrison on 10/1/2014.
 */
public class PopularMoviesDBHelper extends SQLiteOpenHelper {

    private final String LOG_TAG = PopularMoviesDBHelper.class.getSimpleName();

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "movies.db";

    public PopularMoviesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onConfigure(SQLiteDatabase sqLiteDatabase) {
        super.onConfigure(sqLiteDatabase);
        sqLiteDatabase.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_MOVIES_TABLE =
            "CREATE TABLE " + MoviesTable.TABLE_NAME + " (" +
                MoviesTable._ID         + " INTEGER PRIMARY KEY, " +
                MoviesTable.COLUMN_MOVIE_REF_ID           + " TEXT, " +
                MoviesTable.COLUMN_MOVIE_TITLE            + " TEXT, " +
                MoviesTable.COLUMN_MOVIE_OVERVIEW         + " TEXT, " +
                MoviesTable.COLUMN_MOVIE_POSTER_PATH      + " TEXT, " +
                MoviesTable.COLUMN_MOVIE_RATING           + " TEXT, " +
                MoviesTable.COLUMN_MOVIE_POPULARITY       + " TEXT, " +
                MoviesTable.COLUMN_MOVIE_RELEASE_DATE     + " TEXT, " +
                MoviesTable.COLUMN_MOVIE_FAVORITE         + " TEXT DEFAULT 0, " +
                MoviesTable.COLUMN_MOVIE_ORDER            + " TEXT);";


        final String SQL_CREATE_TRAILERS_TABLE = "CREATE TABLE " + TrailersTable.TABLE_NAME + " (" +
                TrailersTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                TrailersTable.COLUMN_TRAILER_REF_ID + " TEXT, " +
                TrailersTable.COLUMN_TRAILER_NAME + " TEXT, " +
                TrailersTable.COLUMN_TRAILER_SOURCE + " TEXT, " +
                TrailersTable.COLUMN_TRAILER_SOURCE_ID + " TEXT, " +
                TrailersTable.COLUMN_TRAILER_SIZE + " TEXT, " +
                TrailersTable.COLUMN_TRAILER_TYPE + " TEXT, " +

                // Set up the location column as a foreign key to location table.
                TrailersTable.COLUMN_MOVIE_ID + " INTEGER REFERENCES " +
                        MoviesTable.TABLE_NAME + " (" + MoviesTable._ID + ") ON DELETE CASCADE); ";

        final String SQL_CREATE_REVIEWS_TABLE = "CREATE TABLE " + ReviewsTable.TABLE_NAME + " (" +
                ReviewsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ReviewsTable.COLUMN_REVIEW_REF_ID + " TEXT, " +
                ReviewsTable.COLUMN_REVIEW_AUTHOR + " TEXT, " +
                ReviewsTable.COLUMN_REVIEW_CONTENT + " TEXT, " +
                ReviewsTable.COLUMN_REVIEW_URL + " TEXT, " +

                // Set up the location column as a foreign key to location table.
                ReviewsTable.COLUMN_MOVIE_ID + " INTEGER REFERENCES " +
                MoviesTable.TABLE_NAME + " (" + MoviesTable._ID + ") ON DELETE CASCADE); ";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TRAILERS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_REVIEWS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

    }
}