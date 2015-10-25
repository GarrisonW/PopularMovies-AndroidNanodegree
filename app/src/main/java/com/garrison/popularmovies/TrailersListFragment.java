package com.garrison.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;


import com.garrison.popularmovies.data.PopularMoviesContract.*;

/**
 * Created by Garrison on 2/5/2015.
 */
public class TrailersListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
private final String LOG_TAG = TrailersListFragment.class.getSimpleName();
    private static final int TRAILERS_LOADER = 2;

    int _ID = -1;

    private ListView mTrailersListView = null;
    private TrailersListAdapter mTrailersListAdapter = null;

    public static String[] TRAILER_COLUMNS = {
            TrailersTable.TABLE_NAME + "." + TrailersTable._ID,
            TrailersTable.COLUMN_MOVIE_ID,
            TrailersTable.COLUMN_TRAILER_REF_ID,
            TrailersTable.COLUMN_TRAILER_NAME,
            TrailersTable.COLUMN_TRAILER_SOURCE,
            TrailersTable.COLUMN_TRAILER_SOURCE_ID,
            TrailersTable.COLUMN_TRAILER_SIZE,
            TrailersTable.COLUMN_TRAILER_TYPE
    };

    public static final int ADAPTER_BINDER_COL_TRAILER_ID = 0;
    public static final int ADAPTER_BINDER_COL_TRAILER_MOVIE_ID = 1;
    public static final int ADAPTER_BINDER_COL_TRAILER_REF_ID = 2;
    public static final int ADAPTER_BINDER_COL_TRAILER_NAME = 3;
    public static final int ADAPTER_BINDER_COL_TRAILER_SOURCE = 4;
    public static final int ADAPTER_BINDER_COL_TRAILER_SOURCE_ID = 5;
    public static final int ADAPTER_BINDER_COL_TRAILER_SIZE = 6;
    public static final int ADAPTER_BINDER_COL_TRAILER_TYPE = 7;

    public TrailersListFragment() {
        super();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(TRAILERS_LOADER, null, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_trailers_list, container, false);

        mTrailersListAdapter = new TrailersListAdapter(getActivity(), null, 0);
        mTrailersListView = (ListView) rootView.findViewById(android.R.id.list);
        //   Override to scroll listview in scrollview
        mTrailersListView.setOnTouchListener(new ListView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }

                // Handle ListView touch events.
                v.onTouchEvent(event);
                return true;
            }
        });
        mTrailersListView.setAdapter(mTrailersListAdapter);
        mTrailersListAdapter.setFragment(this);
        mTrailersListAdapter.setListView(mTrailersListView);

        return rootView;
    }


    public void startMovieTrailer(int pos) {
        Cursor cursor = (Cursor) mTrailersListView.getItemAtPosition(pos);
        ((Callback)getActivity()).startMovieTrailer(
                cursor.getString(ADAPTER_BINDER_COL_TRAILER_SOURCE_ID));
    }


    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(TRAILERS_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            _ID = getActivity().getIntent().getIntExtra(Intent.EXTRA_TEXT, -1);
        }
        else {
            SharedPreferences  sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            _ID = sharedPreferences.getInt(getString(R.string.bundle_movie_db_id), 0);
        }

        Uri trailerUri = TrailersTable.buildTrailerUri();

        String selection = TrailersTable.COLUMN_MOVIE_ID + " = " + _ID;

        // Loads Cursor for list
        return new CursorLoader(
                getActivity(),
                trailerUri,
                TRAILER_COLUMNS,
                selection,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor data) {
        mTrailersListAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mTrailersListAdapter.swapCursor(null);
    }

    /*------------------------------------------------------------------------------------------*/

    public interface Callback {
        public void startMovieTrailer(String trailerSourceID);
    }
}

