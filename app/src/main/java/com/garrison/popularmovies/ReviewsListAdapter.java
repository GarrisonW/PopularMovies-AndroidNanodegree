package com.garrison.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Garrison on 9/2/2015.
 */
public class ReviewsListAdapter extends CursorAdapter {

    private final String LOG_TAG = ReviewsListAdapter.class.getSimpleName();

    public ReviewsListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.listview_reviews, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder)view.getTag();

        String reviewAuthor = cursor.getString(ReviewsListFragment.ADAPTER_BINDER_COL_REVIEW_AUTHOR);
        viewHolder.reviewAuthorTextView.setText(reviewAuthor);

        String reviewContent = cursor.getString(ReviewsListFragment.ADAPTER_BINDER_COL_REVIEW_CONTENT);
        viewHolder.reviewContentsTextView.setText(reviewContent);

    }

    //  Helper to reduce mapping
    public static class ViewHolder {

        public final TextView reviewAuthorTextView;
        public final TextView reviewContentsTextView;
        //public final TextView reviewURLTextView;

        public ViewHolder(View view) {
            reviewAuthorTextView = (TextView) view.findViewById(R.id.list_review_author);
            reviewContentsTextView = (TextView) view.findViewById(R.id.list_review_contents);
            //reviewURLTextView = (TextView) view.findViewById(R.id.list_review_url);
        }
    }
}
