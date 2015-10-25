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

import com.garrison.popularmovies.data.PopularMoviesContract.ReviewsTable;

/**
 * Created by Garrison on 2/5/2015.
 */
public class ReviewsListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {


    private final String LOG_TAG = ReviewsListFragment.class.getSimpleName();

    private static final int REVIEWS_LOADER = 3;

    int _ID = -1;

    private ListView mReviewsListView = null;
    private ReviewsListAdapter mReviewsListAdapter = null;

    public static String[] REVIEW_COLUMNS = {
            ReviewsTable.TABLE_NAME + "." + ReviewsTable._ID,
            ReviewsTable.COLUMN_MOVIE_ID,
            ReviewsTable.COLUMN_REVIEW_REF_ID,
            ReviewsTable.COLUMN_REVIEW_AUTHOR,
            ReviewsTable.COLUMN_REVIEW_CONTENT,
            ReviewsTable.COLUMN_REVIEW_URL
    };

    public static final int ADAPTER_BINDER_COL_REVIEW_ID = 0;
    public static final int ADAPTER_BINDER_COL_REVIEW_MOVIE_ID = 1;
    public static final int ADAPTER_BINDER_COL_REVIEW_REF_ID = 2;
    public static final int ADAPTER_BINDER_COL_REVIEW_AUTHOR = 3;
    public static final int ADAPTER_BINDER_COL_REVIEW_CONTENT = 4;
    public static final int ADAPTER_BINDER_COL_REVIEW_URL = 5;

    public ReviewsListFragment() {
        super();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(REVIEWS_LOADER, null, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_reviews_list, container, false);

        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            _ID = getActivity().getIntent().getIntExtra(Intent.EXTRA_TEXT, -1);
        }

        mReviewsListAdapter = new ReviewsListAdapter(getActivity(), null, 0);
        mReviewsListView = (ListView) rootView.findViewById(android.R.id.list);

        //   Override to scroll listview in scrollview
        mReviewsListView.setOnTouchListener(new ListView.OnTouchListener() {
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

        mReviewsListView.setAdapter(mReviewsListAdapter);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(REVIEWS_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        Intent intent = getActivity().getIntent();

        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            _ID = getActivity().getIntent().getIntExtra(Intent.EXTRA_TEXT, -1);
        }
        else {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            _ID = sharedPreferences.getInt(getString(R.string.bundle_movie_db_id), 0);
        }

        Uri reviewUri = ReviewsTable.buildReviewUri();

        String selection = ReviewsTable.COLUMN_MOVIE_ID + " = " + _ID;

        // Loads Cursor for list
        return new CursorLoader(
                getActivity(),
                reviewUri,
                REVIEW_COLUMNS,
                selection,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor data) {
        mReviewsListAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mReviewsListAdapter.swapCursor(null);
    }
}


