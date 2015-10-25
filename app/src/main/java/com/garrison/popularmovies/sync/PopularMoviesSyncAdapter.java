package com.garrison.popularmovies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.garrison.popularmovies.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;
import java.util.prefs.PreferencesFactory;

import com.garrison.popularmovies.data.PopularMoviesContract.*;
import com.garrison.popularmovies.services.MoviesLoadedNotificationService;


public class PopularMoviesSyncAdapter extends AbstractThreadedSyncAdapter {


    private static final String LOG_TAG = PopularMoviesSyncAdapter.class.getSimpleName();

    // Interval at which to sync with the weather, in milliseconds.
    // 60 seconds (1 minute) * 180 = 3 hours


    public static final int SYNC_INTERVAL = 60 * 60 * 12;
    public static final int SYNC_FLEXTIME = 60 * 60;

    // For testing:
    //public static final int SYNC_INTERVAL = 60 * 2;
    //public static final int SYNC_FLEXTIME = 0;


    public static final int LOAD_TRAILERS = 0;
    public static final int LOAD_REVIEWS = 1;

    private static Context mContext = null;

    HttpURLConnection urlConnection = null;

    String onlyNonFavs = "";

    BufferedReader reader = null;

    public PopularMoviesSyncAdapter(Context ctxt, boolean autoInitialize) {
        super(ctxt, autoInitialize);
        mContext = ctxt;
    }

    /*  Android 3.0 specification (for parallel syncs).  Implement when version 10 is no longer supported
    public PopularMoviesSyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
    }
    */

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        String api_key = mContext.getString(R.string.api_key);
        String basePath = mContext.getString(R.string.base_http_path);

        sendBroadcastMessage(getContext().getString(R.string.broadcast_message_start));

        onlyNonFavs = MoviesTable.COLUMN_MOVIE_FAVORITE + " = 'N'";

        loadMovies(basePath, api_key, mContext.getString(R.string.pref_db_uri_value_popularity), true);
        loadMovies(basePath, api_key, mContext.getString(R.string.pref_db_uri_value_rating), false);

        sendBroadcastMessage(getContext().getString(R.string.broadcast_message_movies_loaded));

        Cursor cursor = mContext.getContentResolver().query(MoviesTable.CONTENT_URI_MOVIES, null, onlyNonFavs, null, null, null);
        loadSubMovieElements(basePath, api_key, cursor, LOAD_TRAILERS);
        loadSubMovieElements(basePath, api_key, cursor, LOAD_REVIEWS);

        sendBroadcastMessage(getContext().getString(R.string.broadcast_message_all_loaded));

    }

    public void loadMovies(String basePath, String api_key, String subPath, boolean reloadDatabase) {

        Uri builder = Uri.parse(basePath).buildUpon()
                .appendPath(subPath)
                .appendQueryParameter("api_key", api_key)
                .build();

        try {
            URL moviesURL = new URL(builder.toString());

            urlConnection = (HttpURLConnection) moviesURL.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream1 = urlConnection.getInputStream();

            StringBuffer buffer = new StringBuffer();
            if (inputStream1 == null) {
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream1));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }
            if (buffer.length() == 0) {
                return;
            }
            parseMovieJSONintoDB(buffer.toString(), subPath, reloadDatabase);

        } catch (IOException ioe) {
             sendBroadcastMessage(mContext.getString(R.string.broadcast_message_html_error));
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    sendBroadcastMessage("Error closing stream");
                }
            }
        }

        Intent mServiceIntent = new Intent(getContext(), MoviesLoadedNotificationService.class);
        getContext().startService(mServiceIntent);
    }

    public void loadSubMovieElements(String basePath, String api_key, Cursor moviesCursor, int mode) {

        if (moviesCursor == null)
            return;

        String subPath = "";
        if (mode == LOAD_TRAILERS)
            subPath = "videos";
        else
            subPath = "reviews";

        moviesCursor.moveToFirst();
        while (!moviesCursor.isAfterLast()) {
            Uri builder = Uri.parse(basePath).buildUpon()
                    .appendPath(moviesCursor.getString(moviesCursor.getColumnIndex(MoviesTable.COLUMN_MOVIE_REF_ID)))
                    .appendPath(subPath)
                    .appendQueryParameter("api_key", api_key)
                    .build();

            try {
                URL moviesURL = new URL(builder.toString());
                urlConnection = (HttpURLConnection) moviesURL.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();

                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    return;
                }

                int movieID = moviesCursor.getInt(moviesCursor.getColumnIndex(MoviesTable._ID));
                if (mode == LOAD_TRAILERS)
                    parseTrailerJSONintoDB(buffer.toString(), movieID);
                else
                    parseReviewsJSONintoDB(buffer.toString(), movieID);
                moviesCursor.moveToNext();

            } catch (IOException ioe) {
                sendBroadcastMessage(mContext.getString(R.string.broadcast_message_html_error));
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        sendBroadcastMessage("Error closing stream");
                    }
                }
            }
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     */
    public static void syncImmediately(Context cntxt) {

        mContext = cntxt;
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(mContext), mContext.getString(R.string.content_authority), bundle);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {
            /*
             * Add the account and account type, no password or user data
             * If successful, return the Account object, otherwise report an error.
             */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            onAccountCreated(newAccount, context);
        }

        return newAccount;
    }

    public void parseMovieJSONintoDB(String movieData, String movieOrder, boolean reloadDatabase){

        JSONArray movieArray;
        JSONObject movieObject;

        final String BASE_OBJECT = "results";

        final String MOVIE_REF_ID = "id";
        final String MOVIE_TITLE = "title";
        final String MOVIE_OVERVIEW = "overview";
        final String MOVIE_POSTER_PATH = "poster_path";
        final String MOVIE_RATING = "vote_average";
        final String MOVIE_POPULARITY = "popularity";
        final String MOVIE_RELEASE_DATE = "release_date";

        String movieRefID;
        String movieTitle;
        String movieOverview;
        String moviePosterPath;
        String movieRating;
        String moviePopularity;
        String movieReleaseDate;

        try {


            JSONObject movieJSON = new JSONObject(movieData);
            movieArray = movieJSON.getJSONArray(BASE_OBJECT);

            if (reloadDatabase)
                mContext.getContentResolver().delete(MoviesTable.CONTENT_URI_MOVIES, onlyNonFavs, null);

            Vector<ContentValues> loaderVector = new Vector<ContentValues>(movieArray.length());

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

            for (int i = 0; i < movieArray.length(); i++) {

                boolean addMovie = true;

                ContentValues movieDataValues = new ContentValues();
                movieObject = movieArray.getJSONObject(i);
                movieRefID = movieObject.getString(MOVIE_REF_ID);
                if (sharedPreferences.contains(movieRefID)) {

                    // Movie already in DB (as a favorite, only add it if it has been added to the list of ohter "order"
                    String selectStatement = MoviesTable.COLUMN_MOVIE_REF_ID + " = " + movieRefID;
                    ContentResolver contentResolver = mContext.getContentResolver();
                    Cursor cursor = contentResolver.query(MoviesTable.CONTENT_URI_MOVIES, null, selectStatement, null, null);
                    if (cursor.getCount() == 1) {
                        cursor.moveToFirst();
                        int columnInt = cursor.getColumnIndex(MoviesTable.COLUMN_MOVIE_ORDER);
                        String currentOrderInDB = cursor.getString(columnInt);
                        if (currentOrderInDB.equals(movieOrder))
                            addMovie = false;
                    }
                }

                if (addMovie) {
                    movieTitle = movieObject.getString(MOVIE_TITLE);
                    movieOverview = movieObject.getString(MOVIE_OVERVIEW);
                    moviePosterPath = movieObject.getString(MOVIE_POSTER_PATH);
                    movieRating = movieObject.getString(MOVIE_RATING);
                    moviePopularity = movieObject.getString(MOVIE_POPULARITY);
                    movieReleaseDate = movieObject.getString(MOVIE_RELEASE_DATE);

                    movieDataValues.put(MoviesTable.COLUMN_MOVIE_REF_ID, movieRefID);
                    movieDataValues.put(MoviesTable.COLUMN_MOVIE_TITLE, movieTitle);
                    movieDataValues.put(MoviesTable.COLUMN_MOVIE_OVERVIEW, movieOverview);
                    movieDataValues.put(MoviesTable.COLUMN_MOVIE_POSTER_PATH, moviePosterPath);
                    movieDataValues.put(MoviesTable.COLUMN_MOVIE_RATING, movieRating);
                    movieDataValues.put(MoviesTable.COLUMN_MOVIE_POPULARITY, moviePopularity);
                    movieDataValues.put(MoviesTable.COLUMN_MOVIE_RELEASE_DATE, movieReleaseDate);
                    movieDataValues.put(MoviesTable.COLUMN_MOVIE_FAVORITE, "N");
                    movieDataValues.put(MoviesTable.COLUMN_MOVIE_ORDER, movieOrder);

                    loaderVector.add(movieDataValues);
                }
            }

            if (loaderVector.size() > 0) {
                ContentValues[] loaderArray = new ContentValues[loaderVector.size()];
                loaderVector.toArray(loaderArray);
                mContext.getContentResolver().bulkInsert(MoviesTable.CONTENT_URI_MOVIES, loaderArray);
            }
        } catch (JSONException je) {
            Log.v(LOG_TAG, "JSON EXCEPTION: " + je.getMessage());
        }
    }

    public void parseTrailerJSONintoDB(String trailerData, int movieID){

        JSONArray trailerArray;
        JSONObject trailerObject;

        final String BASE_OBJECT = "results";

        final String TRAILER_REF_ID = "id";
        final String TRAILER_NAME = "name";
        final String TRAILER_SOURCE = "site";
        final String TRAILER_SOURCE_ID = "key";
        final String TRAILER_SIZE = "size";
        final String TRAILER_TYPE = "type";

        String trailerRefID;
        String trailerName;
        String trailerSource;
        String trailerSourceID;
        String trailerSize;
        String trailerType;

        try {
            JSONObject trailerJSON = new JSONObject(trailerData);
            trailerArray = trailerJSON.getJSONArray(BASE_OBJECT);

            Vector<ContentValues> loaderVector = new Vector<ContentValues>(trailerArray.length());

            for (int i = 0; i < trailerArray.length(); i++) {
                ContentValues trailerDataValues = new ContentValues();
                trailerObject = trailerArray.getJSONObject(i);
                trailerRefID = trailerObject.getString(TRAILER_REF_ID);
                trailerName = trailerObject.getString(TRAILER_NAME);
                trailerSource= trailerObject.getString(TRAILER_SOURCE);
                trailerSourceID = trailerObject.getString(TRAILER_SOURCE_ID);
                trailerSize = trailerObject.getString(TRAILER_SIZE);
                trailerType = trailerObject.getString(TRAILER_TYPE);

                trailerDataValues.put(TrailersTable.COLUMN_MOVIE_ID, movieID);
                trailerDataValues.put(TrailersTable.COLUMN_TRAILER_REF_ID, trailerRefID);
                trailerDataValues.put(TrailersTable.COLUMN_TRAILER_NAME, trailerName);
                trailerDataValues.put(TrailersTable.COLUMN_TRAILER_SOURCE, trailerSource);
                trailerDataValues.put(TrailersTable.COLUMN_TRAILER_SOURCE_ID, trailerSourceID);
                trailerDataValues.put(TrailersTable.COLUMN_TRAILER_SIZE, trailerSize);
                trailerDataValues.put(TrailersTable.COLUMN_TRAILER_TYPE, trailerType);

                loaderVector.add(trailerDataValues);
            }

            if (loaderVector.size() > 0) {
                ContentValues[] loaderArray = new ContentValues[loaderVector.size()];
                loaderVector.toArray(loaderArray);
                mContext.getContentResolver().bulkInsert(TrailersTable.CONTENT_URI_TRAILERS, loaderArray);
            }
        } catch (JSONException je) {
            Log.v(LOG_TAG, "JSON EXCEPTION: " + je.getMessage());
        }
    }

    public void parseReviewsJSONintoDB(String reviewData, int movieID){

        JSONArray reviewArray;
        JSONObject reviewObject;

        final String BASE_OBJECT = "results";

        final String REVIEW_REF_ID = "id";
        final String REVIEW_AUTHOR = "author";
        final String REVIEW_CONTENT = "content";
        final String REVIEW_URL = "url";

        String reviewRefID;
        String reviewAuthor;
        String reviewContent;
        String reviewURL;

        try {
            JSONObject trailerJSON = new JSONObject(reviewData);
            reviewArray = trailerJSON.getJSONArray(BASE_OBJECT);

            Vector<ContentValues> loaderVector = new Vector<ContentValues>(reviewArray.length());

            for (int i = 0; i < reviewArray.length(); i++) {
                ContentValues reviewDataValues = new ContentValues();
                reviewObject = reviewArray.getJSONObject(i);
                reviewRefID = reviewObject.getString(REVIEW_REF_ID);
                reviewAuthor = reviewObject.getString(REVIEW_AUTHOR);
                reviewContent = reviewObject.getString(REVIEW_CONTENT);
                reviewURL = reviewObject.getString(REVIEW_URL);

                reviewDataValues.put(ReviewsTable.COLUMN_MOVIE_ID, movieID);
                reviewDataValues.put(ReviewsTable.COLUMN_REVIEW_REF_ID, reviewRefID);
                reviewDataValues.put(ReviewsTable.COLUMN_REVIEW_AUTHOR, reviewAuthor);
                reviewDataValues.put(ReviewsTable.COLUMN_REVIEW_CONTENT, reviewContent);
                reviewDataValues.put(ReviewsTable.COLUMN_REVIEW_URL, reviewURL);

                loaderVector.add(reviewDataValues);
            }

            if (loaderVector.size() > 0) {
                ContentValues[] loaderArray = new ContentValues[loaderVector.size()];
                loaderVector.toArray(loaderArray);
                mContext.getContentResolver().bulkInsert(ReviewsTable.CONTENT_URI_REVIEWS, loaderArray);
            }
        } catch (JSONException je) {
            Log.v(LOG_TAG, "JSON EXCEPTION: " + je.getMessage());
        }
    }

    /**
      * Helper method to schedule the sync adapter periodic execution
      */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {

         Account account = getSyncAccount(context);
         String authority = context.getString(R.string.content_authority);
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
             SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
             ContentResolver.requestSync(request);
        } else {
             ContentResolver.addPeriodicSync(account, authority, new Bundle(), syncInterval);
        }
        syncImmediately(context);
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        PopularMoviesSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
    }

    private void sendBroadcastMessage(String message) {
        Intent intent = new Intent(mContext.getString(R.string.broadcast_movie_data));
        intent.putExtra(mContext.getString(R.string.broadcast_movie_message), message);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }
}
