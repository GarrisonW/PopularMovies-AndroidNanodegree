package com.garrison.popularmovies.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Garrison on 10/1/2014.
 */
public class PopularMoviesContract {

    // Content Definitions
    public static String CONTENT_AUTHORITY = "com.garrison.popularmovies";
    public static Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static String PATH_MOVIES = "movies";
    public static String PATH_TRAILERS = "trailer";
    public static String PATH_REVIEWS = "reviews";

    /* The Movies Table definition */
    public static final class MoviesTable implements BaseColumns {

        public static Uri CONTENT_URI_MOVIES = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();
        public static String CONTENT_TYPE = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;
        public static String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        public static final String TABLE_NAME = "movies";

        //  Movie Reference ID
        public static final String COLUMN_MOVIE_REF_ID = "movieRefID";
        //  Movie title
        public static final String COLUMN_MOVIE_TITLE = "movieTitle";
        //  Movie override
        public static final String COLUMN_MOVIE_OVERVIEW = "movieOverview";
        //  Movie poster url segment
        public static final String COLUMN_MOVIE_POSTER_PATH = "moviePosterPath";
        //  Movie rating
        public static final String COLUMN_MOVIE_RATING = "rating";
        //  Movie rating
        public static final String COLUMN_MOVIE_POPULARITY = "votes";
        //  Movie relaase date
        public static final String COLUMN_MOVIE_RELEASE_DATE = "releaseDate";
        //  Movie rating
        public static final String COLUMN_MOVIE_FAVORITE = "favorite";
        //  Movie order
        public static final String COLUMN_MOVIE_ORDER = "movieOrder";

        //   Setters and getters for Content Provider Uri - Movies Table
        public static Uri buildMovieUri() {
            return CONTENT_URI_MOVIES;
        }

        public static Uri buildMovieUri(long _id) {
            return ContentUris.withAppendedId(CONTENT_URI_MOVIES, _id);
        }
    }

    /* The Previews Table definition */
    public static final class TrailersTable implements BaseColumns {

        public static Uri CONTENT_URI_TRAILERS = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRAILERS).build();
        public static String CONTENT_TYPE = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_TRAILERS;
        public static String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_TRAILERS;

        public static final String TABLE_NAME = "trailers";

        //  Trailer's Movie Local DB ID
        public static final String COLUMN_MOVIE_ID = "movieID";
        //  Trailer Reference ID
        public static final String COLUMN_TRAILER_REF_ID = "trailerRefID";
        //  Trailer title
        public static final String COLUMN_TRAILER_NAME = "trailerName";
        //  Trailer source (e.g. YouTube)
        public static final String COLUMN_TRAILER_SOURCE = "trailerSource";
        //  Trailer source ID (e.g. YouTube)
        public static final String COLUMN_TRAILER_SOURCE_ID = "trailerSourceID";
        //  Trailer size
        public static final String COLUMN_TRAILER_SIZE = "trailerSize";
        //  Trailer type
        public static final String COLUMN_TRAILER_TYPE = "trailerType";

        public static Uri buildTrailerUri() {
            return CONTENT_URI_TRAILERS;
        }

        public static Uri buildTrailerUri(long _id) {
            return ContentUris.withAppendedId(CONTENT_URI_TRAILERS, _id);
        }
    }

    /* The Reviews Table definition */
    public static final class ReviewsTable implements BaseColumns {

        public static Uri CONTENT_URI_REVIEWS = BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEWS).build();
        public static String CONTENT_TYPE = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_REVIEWS;
        public static String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_REVIEWS;

        public static final String TABLE_NAME = "review";

        //  Review's Movie Local DB ID
        public static final String COLUMN_MOVIE_ID = "movieID";
        //  Review Reference ID
        public static final String COLUMN_REVIEW_REF_ID = "reviewRefID";
        //  Review Author
        public static final String COLUMN_REVIEW_AUTHOR = "reviewAuthor";
        //  Review Text
        public static final String COLUMN_REVIEW_CONTENT = "reviewContent";
        //  Review Online (URL)
        public static final String COLUMN_REVIEW_URL = "reviewURL";


        //   Setters and getters for Content Provider Uri - Reviews Table
        public static Uri buildReviewUri() {
            return CONTENT_URI_REVIEWS;
        }

        public static Uri buildReviewUri(long _id) {
            return ContentUris.withAppendedId(CONTENT_URI_REVIEWS, _id);
        }
    }
}
