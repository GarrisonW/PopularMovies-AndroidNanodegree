package com.garrison.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by Garrison on 8/3/2015.
 */
public class MoviesGridAdapter extends CursorAdapter {

    private final String LOG_TAG = MoviesGridAdapter.class.getSimpleName();

    public MoviesGridAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.gridview_item_movie, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder)view.getTag();

        String posterID = cursor.getString(MoviesFragment.ADAPTER_BINDER_COL_POSTER_PATH);
        String posterPath = context.getString(R.string.base_photo_path) + posterID;

        Picasso.with(context)
                .load(posterPath)
                .placeholder(null)
                .error(R.drawable.abc_ic_menu_cut_mtrl_alpha)
                .into(viewHolder.posterImageView);
    }

    public static class ViewHolder {
        public final ImageView posterImageView;

        public ViewHolder(View view) {
            posterImageView = (ImageView) view.findViewById(R.id.imageview_movie);

        }
    }
}
