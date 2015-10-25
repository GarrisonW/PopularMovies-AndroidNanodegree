package com.garrison.popularmovies;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.garrison.popularmovies.sync.PopularMoviesSyncAdapter;

import com.garrison.popularmovies.data.PopularMoviesContract.MoviesTable;

/**
 * Created by Garrison on 7/25/2015.
 */
public class MoviesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>  {

    private final String LOG_TAG = MoviesFragment.class.getSimpleName();

    Context mContext;
    Fragment mMoviesFragment;

    final static String SAVED_POSITION = "savedPosition";
    final static String SAVED_MOVIE_ID = "movieDBid";

    int mPosition = -1;
    int _ID = -1;

    GridView mMoviesGridView;
    MoviesGridAdapter mMoviesGridAdapter;
    TextView mTextViewLoading;
    ProgressBar mProgressSpinner;

    boolean pauseForLoad = false;

    public static int MOVIES_LOADER = 0;

    public static String[] MOVIE_COLUMNS = {
            MoviesTable.TABLE_NAME + "." + MoviesTable._ID,
            MoviesTable.COLUMN_MOVIE_REF_ID,
            MoviesTable.COLUMN_MOVIE_TITLE,
            MoviesTable.COLUMN_MOVIE_OVERVIEW,
            MoviesTable.COLUMN_MOVIE_POSTER_PATH,
            MoviesTable.COLUMN_MOVIE_RATING,
            MoviesTable.COLUMN_MOVIE_POPULARITY,
            MoviesTable.COLUMN_MOVIE_RELEASE_DATE,
            MoviesTable.COLUMN_MOVIE_FAVORITE,
            MoviesTable.COLUMN_MOVIE_ORDER
    };

    public static final int ADAPTER_BINDER_COL_MOVIE_ID = 0;
    public static final int ADAPTER_BINDER_COL_MOVIE_REF_ID = 1;
    public static final int ADAPTER_BINDER_COL_MOVIE_TITLE = 2;
    public static final int ADAPTER_BINDER_COL_MOVIE_OVERVIEW = 3;
    public static final int ADAPTER_BINDER_COL_POSTER_PATH = 4;
    public static final int ADAPTER_BINDER_COL_RATING = 5;
    public static final int ADAPTER_BINDER_COL_POPULARITY = 6;
    public static final int ADAPTER_BINDER_COL_RELEASE_DATE = 7;
    public static final int ADAPTER_BINDER_COL_FAVORITE = 8;
    public static final int ADAPTER_BINDER_COL_ORDER = 9;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMovieDataLoadReceiver,
                new IntentFilter(mContext.getString(R.string.broadcast_movie_data)));

        getLoaderManager().initLoader(MOVIES_LOADER, null, this);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        mMoviesFragment = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_movies, container, false);

        mMoviesGridAdapter = new MoviesGridAdapter(getActivity(), null, 0);
        mMoviesGridView = (GridView) rootView.findViewById(R.id.gridview_movies);
        mMoviesGridView.setAdapter(mMoviesGridAdapter);
        mTextViewLoading = (TextView) rootView.findViewById(R.id.info_loading);
        mProgressSpinner = (ProgressBar) rootView.findViewById(R.id.progress_movie_data);

        if (!networkAvailable()) {
            mTextViewLoading.setVisibility(View.GONE);
            mProgressSpinner.setVisibility(View.GONE);
            mMoviesGridView.setVisibility(View.VISIBLE);
            Toast.makeText(mContext, getString(R.string.warn_no_network), Toast.LENGTH_LONG).show();
            mMoviesFragment.onResume();
        }

        mMoviesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                Cursor cursor = mMoviesGridAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(pos)) {
                    _ID = cursor.getInt(ADAPTER_BINDER_COL_MOVIE_ID);
                    ((Callback) getActivity()).onItemSelected(_ID);
                }
                mPosition = pos;
            }
        });

        if (savedInstanceState!= null && savedInstanceState.containsKey(SAVED_POSITION)) {
            mPosition = savedInstanceState.getInt(SAVED_POSITION, -1);
            _ID = savedInstanceState.getInt(SAVED_MOVIE_ID, -1);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        if (mPosition > -1) {
            savedInstanceState.putInt(SAVED_POSITION, mPosition);
            savedInstanceState.putInt(SAVED_MOVIE_ID, _ID);
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(MOVIES_LOADER, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Uri moviesUri = MoviesTable.buildMovieUri();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String key = getString(R.string.pref_key_movie_order);
        String defaultValue = getString(R.string.pref_movieorder_default);
        String order = sharedPreferences.getString(key, defaultValue);

        String queryString = "";
        String orderSelection = "";
        String sortOrder = null;

        if (order.equals(getString(R.string.pref_value_movie_order_popularity))) {
            orderSelection = getString(R.string.pref_db_uri_value_popularity);
            sortOrder = MoviesTable.COLUMN_MOVIE_POPULARITY + " DESC";
        }
        else if (order.equals(getString(R.string.pref_value_movie_order_rating))) {
            orderSelection = getString(R.string.pref_db_uri_value_rating);
            sortOrder = MoviesTable.COLUMN_MOVIE_RATING + " DESC";
        }
        else {
            queryString = MoviesTable.COLUMN_MOVIE_FAVORITE + " = 'Y'";
            mTextViewLoading.setVisibility(View.GONE);
            mProgressSpinner.setVisibility(View.GONE);
        }

        if (queryString == "")
            queryString = MoviesTable.COLUMN_MOVIE_ORDER + " = '" + orderSelection + "'";

        return new CursorLoader(
                getActivity(),
                moviesUri,
                MOVIE_COLUMNS,
                queryString,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMoviesGridAdapter.swapCursor(data);
        if (mPosition > -1)
            mMoviesGridView.setSelection(mPosition);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMoviesGridAdapter.swapCursor(null);
    }


    /********   Broadcast intent for synch data loads   **********************************************/

    private BroadcastReceiver mMovieDataLoadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String indicateFailure = "false";

            String message = intent.getStringExtra(mContext.getString(R.string.broadcast_movie_message));
Log.v(LOG_TAG, "MESSAGE:  " + message);
            if (message.equals(mContext.getString(R.string.broadcast_message_start))) {
                mMoviesGridView.setVisibility(View.GONE);
                mTextViewLoading.setVisibility(View.VISIBLE);
                mProgressSpinner.setVisibility(View.VISIBLE);
            }
            if (message.equals(mContext.getString(R.string.broadcast_message_movies_loaded)) ||
                    message.equals(mContext.getString(R.string.broadcast_message_no_network)) ||
                    message.equals(mContext.getString(R.string.broadcast_message_html_error))) {

                mTextViewLoading.setVisibility(View.GONE);
                mProgressSpinner.setVisibility(View.GONE);
                mMoviesGridView.setVisibility(View.VISIBLE);
                if (message.equals(mContext.getString(R.string.broadcast_message_no_network))) {
                    Toast.makeText(mContext, getString(R.string.warn_no_network), Toast.LENGTH_SHORT).show();
                    indicateFailure = "true";
                }
                else if (message.equals(mContext.getString(R.string.broadcast_message_html_error))) {
                    Toast.makeText(mContext, getString(R.string.error_html), Toast.LENGTH_SHORT).show();
                    indicateFailure = "true";
                }

                mMoviesFragment.onResume();
            }

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(getString(R.string.pref_indicate_failure), indicateFailure);


        }
    };

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMovieDataLoadReceiver);
        super.onDestroy();
    }

    private boolean networkAvailable () {
        Log.v(LOG_TAG, "CHECKING NETWORK");
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /********   Callback Interface for menu selections   **********************************************/

    public interface Callback {
        public void onItemSelected(int _ID);
    }
}
