package com.garrison.popularmovies;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.garrison.popularmovies.sync.PopularMoviesSyncAdapter;

public class MainActivity extends AppCompatActivity implements MoviesFragment.Callback, TrailersListFragment.Callback  {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    boolean mTwoPaned = false;

    public static final String MOVIE_DETAIL_TAG = "movieDetailTag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        if (findViewById(R.id.fragment_movie_detail) != null) {
            mTwoPaned = true;

            if (savedInstanceState == null)  {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_movie_detail, new MovieDetailFragment(), MOVIE_DETAIL_TAG)
                        .commit();
            }
        }
        else
            mTwoPaned = false;

        PopularMoviesSyncAdapter.initializeSyncAdapter(this);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences != null && sharedPreferences.contains(getString(R.string.pref_indicate_failure)))
            if (sharedPreferences.getString(getString(R.string.pref_indicate_failure), "false").equals("true"))
                PopularMoviesSyncAdapter.syncImmediately(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(int _ID) {

        if (mTwoPaned) {
            Bundle fragmentArgs = new Bundle();
            fragmentArgs.putInt(getString(R.string.bundle_movie_db_id), _ID);
            MovieDetailFragment movieDetailFragment = new MovieDetailFragment();
            movieDetailFragment.setArguments(fragmentArgs);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_movie_detail, movieDetailFragment, MOVIE_DETAIL_TAG)
                    .commit();
        }
        else {
            Intent movieDetailIntent = new Intent(this, MovieDetailActivity.class);
            movieDetailIntent.putExtra(Intent.EXTRA_TEXT, _ID);
            startActivity(movieDetailIntent);
        }
    }

    @Override
    public void startMovieTrailer(String trailerSourceID) {
        String trailerToShowURL = getString(R.string.base_trailer_path_youtube) + trailerSourceID;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(trailerToShowURL));
        startActivity(intent);
    }

}
