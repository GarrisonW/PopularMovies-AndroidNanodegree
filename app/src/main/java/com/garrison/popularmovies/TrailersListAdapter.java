package com.garrison.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.garrison.popularmovies.data.PopularMoviesContract;

/**
 * Created by Garrison on 9/2/2015.
 */
public class TrailersListAdapter extends CursorAdapter {

    private final String LOG_TAG = TrailersListAdapter.class.getSimpleName();

    public TrailersListFragment mTrailersListFragment = null;
    public ListView mTrailersListListView = null;

    public TrailersListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.listview_trailers, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        viewHolder.playImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = mTrailersListListView.getPositionForView((View) v.getParent());
                mTrailersListFragment.startMovieTrailer(pos);
            }
        });

        return view;
    }

    public void setFragment(TrailersListFragment f) {
        mTrailersListFragment = f;
    }
    public void setListView(ListView lv) {
        mTrailersListListView = lv;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder)view.getTag();

        String trailerName = mCursor.getString(TrailersListFragment.ADAPTER_BINDER_COL_TRAILER_NAME);
        viewHolder.trailerNameTextView.setText(trailerName);
    }

    public static class ViewHolder {
        public final ImageView playImage;
        public final TextView trailerNameTextView;

        public ViewHolder(View view) {
            playImage = (ImageView) view.findViewById(R.id.list_trailer_play_button);
            trailerNameTextView = (TextView) view.findViewById(R.id.list_trailer_name);
        }
    }


}
