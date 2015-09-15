package com.example.gordonyoon.spotifystreamer.service;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import android.support.v7.app.NotificationCompat.Builder;

import com.example.gordonyoon.spotifystreamer.R;
import com.example.gordonyoon.spotifystreamer.activity.MainActivity;
import com.example.gordonyoon.spotifystreamer.broadcastReceiver.MusicIntentReceiver;
import com.example.gordonyoon.spotifystreamer.spotify.TrackInfo;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import static android.support.v7.app.NotificationCompat.Action;
import static android.support.v7.app.NotificationCompat.MediaStyle;

public class PlayerService extends Service implements MediaPlayerController.OnMediaPlayerUpdatedListener {

    public static final String TAG = "PlayerServiceTag";

    public static final int MSG_SET_DURATION = 100;
    public static final int MSG_UPDATE_PLAYER = 110;
    public static final int MSG_UPDATE_PLAY_PAUSE = 120;
    public static final int MSG_FINISH = 130;
    public static final int NOTIFICATION_ID = 200;

    public static final String KEY_TRACK_INFO = "trackInfo";
    public static final String KEY_DURATION = "duration";
    public static final String KEY_TRACK_URL = "trackUrl";
    public static final String KEY_PLAY_STATE = "playState";

    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_PLAY_PAUSE = "action_play_pause";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";
    public static final String ACTION_STOP = "action_stop";
    public static final String ACTION_OPEN = "action_open";
    public static final String ACTION_TOGGLE_CTRLS = "action_toggle_controls";

    public static final String BROADCAST_ACTION = "com.example.gordonyoon.spotifystreamer.service.playpause";

    /* See isServiceRunning() */
    private static PlayerService instance = null;

    private MediaPlayerController mController;

    private MediaSessionCompat mSession;

    /* send messages to PlayerFragment */
    private Messenger mMessenger;

    private final IBinder mBinder = new MusicBinder();

    public PlayerService() {
    }

    public static boolean isServiceRunning() {
        return (instance != null);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        mController = new MediaPlayerController(this, getApplicationContext());

        if (mSession == null) {
            Intent broadcastIntent =
                    new Intent(getApplicationContext(), MusicIntentReceiver.class);
            PendingIntent pendingBroadcastIntent =
                    PendingIntent.getBroadcast(
                            getApplicationContext(), 0, broadcastIntent, 0);

            mSession = new MediaSessionCompat(
                    getApplicationContext(),
                    TAG,
                    new ComponentName(getApplicationContext(), PlayerService.class),
                    pendingBroadcastIntent);

            mSession.setMediaButtonReceiver(pendingBroadcastIntent);

            mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
                    | MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS);

            mSession.setCallback(new MediaSessionCompat.Callback() {
                @Override
                public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
                    return super.onMediaButtonEvent(mediaButtonEvent);
                }

                @Override
                public void onPlay() {
                    super.onPlay();
                    new BuildNotificationTask().execute(
                            generateAction(
                                    android.R.drawable.ic_media_pause,
                                    "Pause",
                                    ACTION_PAUSE));
                    mController.play();
                }

                @Override
                public void onPause() {
                    super.onPause();
                    new BuildNotificationTask().execute(
                            generateAction(
                                    android.R.drawable.ic_media_play,
                                    "Play",
                                    ACTION_PLAY));
                    mController.pause();
                }

                @Override
                public void onSkipToNext() {
                    super.onSkipToNext();
                    new BuildNotificationTask().execute(
                            generateAction(
                                    android.R.drawable.ic_media_pause,
                                    "Pause",
                                    ACTION_PAUSE));
                    mController.next();
                }

                @Override
                public void onSkipToPrevious() {
                    super.onSkipToPrevious();
                    new BuildNotificationTask().execute(
                            generateAction(
                                    android.R.drawable.ic_media_pause,
                                    "Pause",
                                    ACTION_PAUSE));
                    mController.previous();
                }

                @Override
                public void onStop() {
                    super.onStop();
                    stopForeground(true);
                    Intent intent = new Intent(getApplicationContext(), PlayerService.class);
                    stopService(intent);
                    try {
                        mMessenger.send(Message.obtain(null, MSG_FINISH));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            });

            mSession.setActive(true);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();

            // lockscreen uses ACTION_PLAY_PAUSE
            if (action.equals(ACTION_PLAY_PAUSE)) {
                PlaybackStateCompat state = mSession.getController().getPlaybackState();
                if (state != null && state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                    action = ACTION_PAUSE;
                } else {
                    action = ACTION_PLAY;
                }
            }

            if (action.equals(ACTION_PLAY)) {
                mSession.getController().getTransportControls().play();
            } else if (action.equals(ACTION_PAUSE)) {
                mSession.getController().getTransportControls().pause();
            } else if (action.equals(ACTION_PREVIOUS)) {
                mSession.getController().getTransportControls().skipToPrevious();
            } else if (action.equals(ACTION_NEXT)) {
                mSession.getController().getTransportControls().skipToNext();
            } else if (action.equals(ACTION_STOP)) {
                mSession.getController().getTransportControls().stop();
            } else if (action.equals(ACTION_TOGGLE_CTRLS)) {
                SharedPreferences prefs = getSharedPreferences(
                        getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                boolean showControls = prefs.getBoolean(MainActivity.KEY_PREF_SHOW_CTRLS, true);

                if (showControls) {
                    if (mController.isPlaying()) {
                        setStatePlay();
                    } else {
                        setStatePause();
                    }
                } else {
                    if (mSession != null) {
                        mSession.setPlaybackState(new PlaybackStateCompat.Builder().build());
                    }
                }
            }
        }
        return Service.START_STICKY_COMPATIBILITY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        if (mSession != null) {
            mSession.release();
        }
        mController.onDestroy();

        // remove share URL when the service is stopped
        SharedPreferences prefs = getApplication().getSharedPreferences(
                getString(R.string.preference_file_key),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_TRACK_URL).commit();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        onTogglePlayPause(!mController.isPlaying());
    }

    @Override
    public void onTogglePlayPause(boolean play) {
        // update player play button
        try {
            if (mMessenger != null) {
                mMessenger.send(Message.obtain(null, MSG_UPDATE_PLAY_PAUSE, (play) ? 1 : 0, 0));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        if (play) {
            setStatePause();
        } else {
            setStatePlay();
        }

        // update navigation drawer play button
        Intent broadcastIntent = new Intent(BROADCAST_ACTION);
        broadcastIntent.putExtra(KEY_PLAY_STATE, mController.isPlaying());
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }

    @Override
    public void onTrackSelected() {
        // set ShareActionProvider data
        Intent broadcastIntent = new Intent(BROADCAST_ACTION);
        broadcastIntent.putExtra(KEY_TRACK_URL, mController.getCurrentTrackInfo().getPreview());
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);

        updatePlayerUi();
    }

    @Override
    public void onTrackPrepared() {
        new UpdateMetadataTask().execute();
        new BuildNotificationTask().execute(
                generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));

        // update track duration
        try {
            mMessenger.send(Message.obtain(null, MSG_SET_DURATION, mController.getDuration(), 0));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void updatePlayerUi() {
        Bundle data = new Bundle();
        data.putInt(KEY_DURATION, mController.getDuration());
        data.putParcelable(KEY_TRACK_INFO, mController.getCurrentTrackInfo());

        Message message = Message.obtain(null, MSG_UPDATE_PLAYER);
        message.setData(data);

        try {
            mMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void setStatePlay() {
        SharedPreferences prefs = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        boolean showControls = prefs.getBoolean(MainActivity.KEY_PREF_SHOW_CTRLS, true);

        // for lockscreen media buttons
        if (showControls && mSession != null) {
            mSession.setPlaybackState(new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f)
                    .setActions(PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                            | PlaybackStateCompat.ACTION_PAUSE
                            | PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
                    .build());
        }
    }

    private void setStatePause() {
        SharedPreferences prefs = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        boolean showControls = prefs.getBoolean(MainActivity.KEY_PREF_SHOW_CTRLS, true);

        // for lockscreen media buttons
        if (showControls && mSession != null) {
            mSession.setPlaybackState(new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PAUSED, 0, 0.0f)
                    .setActions(PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                            | PlaybackStateCompat.ACTION_PLAY
                            | PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
                    .build());
        }
    }

    private Action generateAction(int icon, String title, String action) {
        Intent intent = new Intent(getApplicationContext(), PlayerService.class);
        intent.setAction(action);
        PendingIntent pendingIntent =
                PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new Action.Builder(icon, title, pendingIntent).build();
    }

    public void init(int position) {
        mController.initPlayer(position);
    }

    public void togglePlayPause() {
        mController.togglePlayPause();
    }

    public boolean next() {
        return mController.next();
    }

    public boolean previous() {
        return mController.previous();
    }

    public void seekTo(int position) {
        mController.seekTo(position);
    }

    /**
     * @return the current position in seconds
     */
    public int getCurrentPosition() {
        return mController.getPlaybackPosition();
    }

    public int getCurrentTrack() {
        return mController.getCurrentTrackPosition();
    }

    public void setTrackInfos(List<TrackInfo> trackInfos) {
        mController.setTrackInfos(trackInfos);
    }

    public void setMessenger(Messenger messenger) {
        mMessenger = messenger;
    }

    public class MusicBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
    }

    private class UpdateMetadataTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                TrackInfo trackInfo = mController.getCurrentTrackInfo();
                if (trackInfo != null) {
                    Bitmap artwork = Picasso.with(getApplicationContext())
                            .load(trackInfo.getLargeImageUrl()).get();
                    mSession.setMetadata(new MediaMetadataCompat.Builder()
                            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, trackInfo.getArtist())
                            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, trackInfo.getAlbumName())
                            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, trackInfo.getTrackName())
                            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mController.getDuration())
                            .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, artwork)
                            .build());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class BuildNotificationTask extends AsyncTask<NotificationCompat.Action, Void, Void> {
        @Override
        protected Void doInBackground(Action... params) {
            try {
                TrackInfo trackInfo = mController.getCurrentTrackInfo();
                if (trackInfo != null) {
                    String songName = trackInfo.getTrackName();
                    String artist = trackInfo.getArtist();
                    String uri = trackInfo.getLargeImageUrl();
                    Bitmap artwork = Picasso.with(getApplicationContext()).load(uri).get();

                    Intent contentIntent = new Intent(getApplicationContext(), MainActivity.class);
                    contentIntent.setAction(ACTION_OPEN);
                    PendingIntent pendingContentIntent = PendingIntent.getActivity(
                            getApplicationContext(),
                            0,
                            contentIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);

                    // Cancel intent
                    Intent cancelIntent = new Intent(getApplicationContext(), PlayerService.class);
                    cancelIntent.setAction(ACTION_STOP);
                    PendingIntent pendingCancelIntent =
                            PendingIntent.getService(getApplicationContext(), 1, cancelIntent, 0);

                    // MediaStyle
                    MediaStyle style = new MediaStyle();
                    style.setMediaSession(mSession.getSessionToken());
                    style.setShowCancelButton(true);
                    style.setCancelButtonIntent(pendingCancelIntent);

                    // Notification Builder
                    Builder builder =
                            (Builder)new Builder(getApplicationContext())
                                    .setStyle(style)
                                    .setSmallIcon(android.R.drawable.ic_media_play)
                                    .setContentTitle(songName)
                                    .setContentText(artist)
                                    .setLargeIcon(artwork)
                                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                                    .setContentIntent(pendingContentIntent)
                                    .setDeleteIntent(pendingCancelIntent);

                    // add notification action buttons (play/pause in the center)
                    builder.addAction(generateAction(
                            android.R.drawable.ic_media_previous, "Previous", ACTION_PREVIOUS));
                    builder.addAction(params[0]);
                    builder.addAction(generateAction(
                            android.R.drawable.ic_media_next, "Next", ACTION_NEXT));

                    NotificationManager notificationManager =
                            (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(NOTIFICATION_ID, builder.build());

                    startForeground(NOTIFICATION_ID, builder.build());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
