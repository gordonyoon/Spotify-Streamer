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
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.PlaybackState;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
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

public class PlayerService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener {

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

    private MediaPlayer mMediaPlayer;
    private MediaSessionCompat mSession;

    /* send messages to PlayerFragment */
    private Messenger mMessenger;

    private AudioManager mAudioManager;
    /* refers to the particular track that is playing */
    private int mCurrentTrack = -1;
    private List<TrackInfo> mTrackInfos;

    private int mDuration;
    private boolean mPrepared = false;
    private boolean mPlayState = true;

    private final IBinder mBinder = new MusicBinder();

    public PlayerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
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
                    if (mPlayState) {
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
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);

        if (mPlayState) {
            updatePlayPause(false);
        } else {
            updatePlayPause(true);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

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
                    play();
                }

                @Override
                public void onPause() {
                    super.onPause();
                    new BuildNotificationTask().execute(
                            generateAction(
                                    android.R.drawable.ic_media_play,
                                    "Play",
                                    ACTION_PLAY));
                    pause();
                }

                @Override
                public void onSkipToNext() {
                    super.onSkipToNext();
                    new BuildNotificationTask().execute(
                            generateAction(
                                    android.R.drawable.ic_media_pause,
                                    "Pause",
                                    ACTION_PAUSE));
                    next();
                }

                @Override
                public void onSkipToPrevious() {
                    super.onSkipToPrevious();
                    new BuildNotificationTask().execute(
                            generateAction(
                                    android.R.drawable.ic_media_pause,
                                    "Pause",
                                    ACTION_PAUSE));
                    previous();
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
    public void onPrepared(MediaPlayer mp) {
        mPrepared = true;
        mDuration = mp.getDuration() / 1000;  // duration in seconds
        if (mPlayState) {
            play();
        }
        new UpdateMetadataTask().execute();
        new BuildNotificationTask().execute(
                generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));

        // update track duration UI
        try {
            mMessenger.send(Message.obtain(null, MSG_SET_DURATION, mDuration, 0));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (next()) {
            updatePlayPause(false);
        } else {
            updatePlayPause(true);
            mPlayState = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanMediaPlayer();
        instance = null;
        if (mSession != null) {
            mSession.release();
        }

        // remove share URL when the service is stopped
        SharedPreferences prefs = getApplication().getSharedPreferences(
                getString(R.string.preference_file_key),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_TRACK_URL).commit();

        // music stopped when service is stopped
        mPlayState = false;
        broadcastPlayState();
    }

    /**
     * Initialize the MediaPlayer with parameters.
     */
    public void init(int position) {
        try {
            mCurrentTrack = position;
            mPlayState = true;
            mPrepared = false;
            updatePlayerUi();

            cleanMediaPlayer();
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);

            String url = mTrackInfos.get(mCurrentTrack).getPreview();
            mMediaPlayer.setDataSource(url);
            mMediaPlayer.prepareAsync();

            broadCastTrackChanged(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Action generateAction(int icon, String title, String action) {
        Intent intent = new Intent(getApplicationContext(), PlayerService.class);
        intent.setAction(action);
        PendingIntent pendingIntent =
                PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new Action.Builder(icon, title, pendingIntent).build();

    }

    public boolean isPlaying() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.isPlaying();
        }
        return false;
    }

    public static boolean isServiceRunning() {
        return (instance != null);
    }

    /**
     * @return the current position in seconds
     */
    public int getCurrentPosition() {
        if (mMediaPlayer != null) {
            return Math.round(((float) mMediaPlayer.getCurrentPosition()) / 1000);
        }
        return 0;
    }

    public int getCurrentTrack() {
        return mCurrentTrack;
    }

    public void setMessenger(Messenger messenger) {
        mMessenger = messenger;
    }

    public void setTrackInfos(List<TrackInfo> trackInfos) {
        mTrackInfos = trackInfos;
    }

    public void togglePlayPause() {
        if (mPlayState) {
            pause();
        } else {
            play();
        }
    }

    public void play() {
        if (mMediaPlayer != null) {
            mPlayState = true;
            if (mPrepared) {
                mMediaPlayer.start();
                requestAudioFocus();
            }
            updatePlayPause(false);
        }

        setStatePlay();
        broadcastPlayState();
    }

    public void pause() {
        if (mMediaPlayer != null) {
            mPlayState = false;
            if (mPrepared) {
                mMediaPlayer.pause();
            }
            updatePlayPause(true);
        }

        setStatePause();
        broadcastPlayState();
    }

    /**
     * @return true if next track exists
     */
    public boolean next() {
        if (mCurrentTrack < mTrackInfos.size() - 1) {
            mCurrentTrack++;
            mPlayState = true;
            init(mCurrentTrack);
            updatePlayPause(false);
            return true;
        }
        return false;
    }

    /**
     * @return true if previous track exists
     */
    public boolean previous() {
        if (mCurrentTrack > 0 && getCurrentPosition() < 2) {
            mCurrentTrack--;
            mPlayState = true;
            init(mCurrentTrack);
            updatePlayPause(false);
            return true;
        } else {
            seekTo(0);
            play();
            return false;
        }
    }

    /**
     * @param position in seconds
     */
    public void seekTo(int position) {
        if (mMediaPlayer != null) {
            if (mPrepared) {
                mMediaPlayer.seekTo(position * 1000);
            }
        }
    }

    public void updatePlayerUi() {
        if (mTrackInfos != null) {
            Bundle data = new Bundle();
            data.putParcelable(KEY_TRACK_INFO, mTrackInfos.get(mCurrentTrack));
            data.putInt(KEY_DURATION, mDuration);

            Message message = Message.obtain(null, MSG_UPDATE_PLAYER);
            message.setData(data);

            try {
                mMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void cleanMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
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

    /**
     * @param play true to set the Play Button UI
     */
    private void updatePlayPause(boolean play) {
        int playInt = (play) ? 1 : 0;
        try {
            mMessenger.send(Message.obtain(null, MSG_UPDATE_PLAY_PAUSE, playInt, 0));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Intended to update the play/pause button in the navigation drawer
     */
    private void broadcastPlayState() {
        Intent broadcastIntent = new Intent(BROADCAST_ACTION);
        broadcastIntent.putExtra(KEY_PLAY_STATE, mPlayState);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }

    private void broadCastTrackChanged(String url) {
        Intent broadcastIntent = new Intent(BROADCAST_ACTION);
        broadcastIntent.putExtra(KEY_TRACK_URL, url);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }

    private void requestAudioFocus() {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            int result = mAudioManager.requestAudioFocus(
                    new AudioManager.OnAudioFocusChangeListener() {

                        @Override
                        public void onAudioFocusChange(int focusChange) {
                            switch (focusChange) {
                                case AudioManager.AUDIOFOCUS_GAIN:
                                    // resume playback
                                    if (!isPlaying()) {
                                        play();
                                    }
                                    mMediaPlayer.setVolume(1.0f, 1.0f);
                                    break;

                                case AudioManager.AUDIOFOCUS_LOSS:
                                    // Lost focus for an unbounded amount of time: stop playback and release media player
                                    if (isPlaying()) {
                                        mMediaPlayer.stop();
                                    }
                                    cleanMediaPlayer();
                                    break;

                                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                                    // Lost focus for a short time, but we have to stop
                                    // playback. We don't release the media player because playback
                                    // is likely to resume
                                    if (isPlaying()) {
                                        pause();
                                    }
                                    break;

                                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                                    // Lost focus for a short time, but it's ok to keep playing
                                    // at an attenuated level
                                    if (isPlaying()) {
                                        mMediaPlayer.setVolume(0.1f, 0.1f);
                                    }
                                    break;
                            }
                        }
                    },
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);

            if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                // could not get audio focus.
                pause();
            }
        }
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
                TrackInfo info = mTrackInfos.get(mCurrentTrack);
                Bitmap artwork =
                        Picasso.with(getApplicationContext()).load(info.getLargeImageUrl()).get();

                mSession.setMetadata(new MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, info.getArtist())
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, info.getAlbumName())
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, info.getTrackName())
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mDuration)
                        .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, artwork)
                        .build());
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
                if (mTrackInfos != null) {
                    String songName = mTrackInfos.get(mCurrentTrack).getTrackName();
                    String artist = mTrackInfos.get(mCurrentTrack).getArtist();
                    String uri = mTrackInfos.get(mCurrentTrack).getLargeImageUrl();
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
                            (Builder) new Builder(getApplicationContext())
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
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
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
