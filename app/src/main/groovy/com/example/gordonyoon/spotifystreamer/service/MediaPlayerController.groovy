package com.example.gordonyoon.spotifystreamer.service

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.PowerManager
import com.example.gordonyoon.spotifystreamer.spotify.TrackInfo
import groovy.transform.CompileStatic

@CompileStatic
class MediaPlayerController implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener {

    private MediaPlayer mMediaPlayer

    private OnMediaPlayerUpdatedListener mListener
    private Context mContext
    private AudioManager mAudioManager

    private List<TrackInfo> mTrackInfos
    private int mCurrentTrack = -1

    private int mDuration = 0
    private boolean mPrepared = false
    private boolean mPlayOnPrepared = true


    MediaPlayerController(OnMediaPlayerUpdatedListener listener, Context context) {
        mListener = listener
        mContext = context

        mAudioManager = mContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    @Override
    void onCompletion(MediaPlayer mp) {
        if (next()) {
            mListener.onTogglePlayPause(false)
        } else {
            mPlayOnPrepared = false
            mListener.onTogglePlayPause(true)
        }
    }

    @Override
    void onPrepared(MediaPlayer mp) {
        mPrepared = true
        if (mPlayOnPrepared) {
            play()
        }
        mDuration = mp.getDuration() / 1000 as int  // mDuration in seconds
        mListener.onTrackPrepared()
    }

    /**
     * Must be called for cleanup
     */
    void onDestroy() {
        cleanMediaPlayer()
        mListener.onTogglePlayPause(true)
    }

    public void initPlayer(int position) {
        try {
            mPrepared = false
            mCurrentTrack = position

            mListener.onTrackSelected()

            cleanMediaPlayer()
            mMediaPlayer = new MediaPlayer()
            mMediaPlayer.with {
                onPreparedListener = this
                onCompletionListener = this
                dataSource = mTrackInfos.get(mCurrentTrack).getPreview()
                audioStreamType = AudioManager.STREAM_MUSIC
                setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK)
                prepareAsync()
            }
        } catch (IOException e) {
            e.printStackTrace()
        }
    }

    boolean isPlaying() {
        mPlayOnPrepared
    }

    void togglePlayPause() {
        if (mPlayOnPrepared) {
            pause()
        } else {
            play()
        }
    }

    void play() {
        mPlayOnPrepared = true
        if (mPrepared) {
            mMediaPlayer?.start()
            requestAudioFocus()
        }
        mListener.onTogglePlayPause(false)
    }

    void pause() {
        mPlayOnPrepared = false
        if (mPrepared) {
            mMediaPlayer?.pause()
        }
        mListener.onTogglePlayPause(true)
    }

    /**
     * @return true if next track exists
     */
    boolean next() {
        if (mCurrentTrack < mTrackInfos.size() - 1) {
            mPlayOnPrepared = true
            initPlayer(++mCurrentTrack)
            return true
        }
        false
    }

    /**
     * @return true if previous track exists
     */
    boolean previous() {
        if (mCurrentTrack > 0 && getPlaybackPosition() < 2) {
            mPlayOnPrepared = true
            initPlayer(--mCurrentTrack)
            true
        } else {
            seekTo(0)
            play()
            false
        }
    }

    /**
     * @param position in seconds
     */
    void seekTo(int position) {
        if (mPrepared) {
            mMediaPlayer?.seekTo(position * 1000)
        }
    }

    void setTrackInfos(List<TrackInfo> trackInfos) {
        mTrackInfos = trackInfos
    }

    /**
     * @return the current position in seconds
     */
    int getPlaybackPosition() {
        mMediaPlayer ? Math.round(mMediaPlayer.getCurrentPosition() / 1000 as float) : 0
    }

    TrackInfo getCurrentTrackInfo() {
        mTrackInfos?.get(mCurrentTrack)
    }

    int getCurrentTrackPosition() {
        mCurrentTrack
    }

    int getDuration() {
        mDuration
    }

    private void cleanMediaPlayer() {
        mMediaPlayer?.release()
        mMediaPlayer = null
    }

    private void requestAudioFocus() {
        if (mAudioManager == null) {
            int result = mAudioManager.requestAudioFocus({
                switch (it) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        mMediaPlayer?.setVolume(1.0f, 1.0f)
                        play()
                        break
                    case AudioManager.AUDIOFOCUS_LOSS:
                        // Lost focus for an unbounded amount of time: stop playback and release media player
                        cleanMediaPlayer()
                        break

                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        // Lost focus for a short time, but we have to stop playback. We don't release the media player because playback is likely to resume
                        pause()
                        break

                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        // Lost focus for a short time, but it's ok to keep playing at an attenuated level
                        mMediaPlayer?.setVolume(0.1f, 0.1f)
                        break
                }
            }, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)

            if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                // could not get audio focus.
                pause()
            }
        }
    }

    static interface OnMediaPlayerUpdatedListener {
        void onTogglePlayPause(boolean play)

        void onTrackSelected()

        void onTrackPrepared()
    }
}