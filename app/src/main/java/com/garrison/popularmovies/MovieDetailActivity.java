package com.garrison.popularmovies;

import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


import com.garrison.popularmovies.sync.PopularMoviesSyncAdapter;

/**
 * Created by Garrison on 8/9/2015.
 */
public class MovieDetailActivity extends AppCompatActivity implements TrailersListFragment.Callback {

    private final String LOG_TAG = MovieDetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        Bundle fragmentArguments = new Bundle();
        fragmentArguments.putInt(getString(R.string.bundle_movie_db_id), getIntent().getIntExtra(Intent.EXTRA_TEXT, -1));

        MovieDetailFragment movieDetailFragment = new MovieDetailFragment();
        movieDetailFragment.setArguments(fragmentArguments);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_movie_detail, movieDetailFragment)
                .commit();
    }

    @Override
    public void startMovieTrailer(String trailerSourceID) {
        String trailerToShowURL = getString(R.string.base_trailer_path_youtube) + trailerSourceID;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(trailerToShowURL));
        startActivity(intent);
    }

}
