package com.garrison.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.content.Context;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.garrison.popularmovies.data.PopularMoviesContract.*;
import com.squareup.picasso.Picasso;

public class MovieDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private final String LOG_TAG = MovieDetailFragment.class.getSimpleName();

    Context mContext;

    ViewHolder viewHolder = null;

    Cursor mCursor;

    boolean mFavorite = false;

    public Button mMovieFavoritesButton = null;

    int _ID = 1;
    String movieRefID = "";

    public static final int MOVIE_LOADER = 1;

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
    public static final int ADAPTER_BINDER_COL_MOVIE_POSTER_PATH = 4;
    public static final int ADAPTER_BINDER_COL_MOVIE_RATING = 5;
    public static final int ADAPTER_BINDER_COL_MOVIE_POPULARITY = 6;
    public static final int ADAPTER_BINDER_COL_MOVIE_RELEASE_DATE = 7;
    public static final int ADAPTER_BINDER_COL_MOVIE_FAVORITE = 8;
    public static final int ADAPTER_BINDER_COL_MOVIE_ORDER = 9;

    public MovieDetailFragment(){
        setHasOptionsMenu(true);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mContext = getActivity();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        Bundle fragmentArguments = getArguments();
        if (fragmentArguments != null) {
            _ID = fragmentArguments.getInt(getString(R.string.bundle_movie_db_id));
        }
        else if (savedInstanceState != null) {
            _ID = savedInstanceState.getInt(getString(R.string.bundle_movie_db_id), -1);
        }
        else {
            String order = sharedPreferences.getString(getString(R.string.pref_key_movie_order), "");
            if (order.equals(getString(R.string.pref_value_movie_order_popularity)))
                _ID = 1;
            else
                _ID = 21;
        }



        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(getString(R.string.bundle_movie_db_id), _ID);
        editor.commit();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        viewHolder = new ViewHolder(rootView);
        rootView.setTag(viewHolder);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_movie_detail, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);

        ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        if (mShareActionProvider != null ) {
            Intent shareMovieIntent = createShareMovieIntent();
            if (shareMovieIntent != null)
                mShareActionProvider.setShareIntent(createShareMovieIntent());
            else
                Toast.makeText(mContext, getString(R.string.info_no_trailers), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Uri moviesUri = MoviesTable.buildMovieUri(_ID);

        return new CursorLoader(
                getActivity(),
                moviesUri,
                MOVIE_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if ((cursor != null) && cursor.getCount() > 0) {
            cursor.moveToFirst();
            mCursor = cursor;
            String movieTitle = cursor.getString(cursor.getColumnIndex(MoviesTable.COLUMN_MOVIE_TITLE));
            movieRefID = cursor.getString(cursor.getColumnIndex(MoviesTable.COLUMN_MOVIE_REF_ID));
            getActivity().setTitle(movieTitle);
            String movieOverview = cursor.getString(cursor.getColumnIndex(MoviesTable.COLUMN_MOVIE_OVERVIEW));
            String movieReleaseDate = cursor.getString(cursor.getColumnIndex(MoviesTable.COLUMN_MOVIE_RELEASE_DATE));
            String movieRating = cursor.getString(cursor.getColumnIndex(MoviesTable.COLUMN_MOVIE_RATING));
            String moviePosterPath = cursor.getString(cursor.getColumnIndex(MoviesTable.COLUMN_MOVIE_POSTER_PATH));

            viewHolder.movieTitleTextView.setText(movieTitle);
            viewHolder.movieOverviewTextView.setText(movieOverview);
            viewHolder.movieReleaseDateTextView.setText(movieReleaseDate.substring(0, 4));
            viewHolder.movieRatingTextView.setText(mContext.getString(R.string.formatted_movie_rating, movieRating));

            // No "runtime" available in JSON API output
            viewHolder.movieRuntimeTextView.setText(mContext.getString(R.string.formatted_movie_minutes, "120"));

            mMovieFavoritesButton = viewHolder.movieFavoriteButton;

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            boolean aFavorite = sharedPreferences.contains(movieRefID);

            if (aFavorite) {
                mMovieFavoritesButton.setText(R.string.button_is_favorite);
                mMovieFavoritesButton.setBackgroundColor(Color.GREEN);
                mFavorite = true;
            }
            else {
                mMovieFavoritesButton.setText(R.string.button_mark_as_favorite);
                mMovieFavoritesButton.setBackgroundColor(Color.LTGRAY);
            }

            mMovieFavoritesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mFavorite)
                        removeMovieFromFavorites();
                    else
                        flagMovieAsFavorite();
                }

            });

            String posterPath = mContext.getString(R.string.base_photo_path_small) + moviePosterPath;

            Picasso.with(mContext)
                    .load(posterPath)
                    .placeholder(null)
                    .error(R.drawable.abc_ic_menu_cut_mtrl_alpha)
                    .into(viewHolder.moviePosterImageView);
        }
        else
            Toast.makeText(mContext, getString(R.string.error_not_found), Toast.LENGTH_LONG);

    }

    private void flagMovieAsFavorite() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String favMoviesRefID = sharedPreferences.getString(movieRefID, null);

        if (favMoviesRefID == null) {
            updateFavoriteInDB("Y");

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(movieRefID, movieRefID);

            editor.commit();

            mMovieFavoritesButton.setText(R.string.button_is_favorite);
            mMovieFavoritesButton.setBackgroundColor(Color.GREEN);

            Toast.makeText(mContext,getString(R.string.info_added_fav), Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(mContext,getString(R.string.warn_already_fav), Toast.LENGTH_SHORT).show();
    }

    private void removeMovieFromFavorites() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(movieRefID);
        editor.commit();

        mMovieFavoritesButton.setText(R.string.button_mark_as_favorite);
        mMovieFavoritesButton.setBackgroundColor(Color.LTGRAY);

        updateFavoriteInDB("N");

        Toast.makeText(mContext,getString(R.string.info_removed_fav), Toast.LENGTH_SHORT).show();
    }

    private void updateFavoriteInDB (String value) {
        String selectStmt = MoviesTable._ID + " = " + _ID;
        ContentValues movieValues = new ContentValues();
        movieValues.put(MoviesTable.COLUMN_MOVIE_FAVORITE, value);
        mContext.getContentResolver().update(MoviesTable.CONTENT_URI_MOVIES, movieValues, selectStmt, null);
    }

    private Intent createShareMovieIntent() {

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");

        Uri trailerUri = TrailersTable.buildTrailerUri();

        String selection = TrailersTable.COLUMN_MOVIE_ID + " = " + _ID;

        String TRAILER_COLUMNS[] = {TrailersTable.COLUMN_TRAILER_SOURCE_ID};

        Cursor cursor = getActivity().getContentResolver().query(trailerUri, TRAILER_COLUMNS, selection, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            String trailerSourceID = cursor.getString(cursor.getColumnIndex(TrailersTable.COLUMN_TRAILER_SOURCE_ID));
            String movieTrailerURL = getString(R.string.base_trailer_path_youtube) + trailerSourceID;
            shareIntent.putExtra(Intent.EXTRA_TEXT, movieTrailerURL);
            return shareIntent;
        }
        return null;
    }

    //  Helper to reduce mapping
    public static class ViewHolder {
        public final ImageView moviePosterImageView;
        public final TextView movieTitleTextView;
        public final TextView movieOverviewTextView;
        public final TextView movieReleaseDateTextView;
        public final TextView movieRatingTextView;
        public final TextView movieRuntimeTextView;
        public final Button movieFavoriteButton;

        public ViewHolder(View view) {
            moviePosterImageView = (ImageView) view.findViewById(R.id.imageview_movie_detail_poster);
            movieTitleTextView = (TextView) view.findViewById(R.id.textview_movie_detail_title);
            movieOverviewTextView = (TextView) view.findViewById(R.id.textview_movie_detail_overview);
            movieReleaseDateTextView = (TextView) view.findViewById(R.id.textview_movie_detail_year);
            movieRatingTextView = (TextView) view.findViewById(R.id.textview_movie_detail_rating);
            movieRuntimeTextView = (TextView) view.findViewById(R.id.textview_movie_detail_length);
            movieFavoriteButton = (Button) view.findViewById(R.id.button_movie_detail_favorite);
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
