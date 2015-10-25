package com.garrison.popularmovies.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.garrison.popularmovies.MainActivity;
import com.garrison.popularmovies.R;

/**
 * Created by Garrison on 11/18/2014.
 */
public class MoviesLoadedNotificationService extends IntentService {

    private static final int MOVIE_LOADED_NOTIFICATION_ID = 2223;
    NotificationManager mNotificationManager = null;
    boolean fireNotify = false;
    private NotificationCompat.Builder mBuilder = null;
    private int numDeletes = 0;

    public MoviesLoadedNotificationService() {
        super("MoviesLoadedNotificationService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        setNotify();
        doNotify();
    }

    public void setNotify() {

        String contentText = getResources().getString(R.string.notify_statement);

        if (mBuilder == null) {
            mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notify_icon)
                .setContentTitle(getString(R.string.notify_title));
        }
        mBuilder.setContentText(contentText);
    }

    public void doNotify() {
        if (mNotificationManager == null)
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        mNotificationManager.notify(MOVIE_LOADED_NOTIFICATION_ID, mBuilder.build());
    }
}
