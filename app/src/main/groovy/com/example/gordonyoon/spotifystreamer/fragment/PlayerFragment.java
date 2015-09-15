package com.example.gordonyoon.spotifystreamer.fragment;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.gordonyoon.spotifystreamer.R;
import com.example.gordonyoon.spotifystreamer.service.PlayerService;
import com.example.gordonyoon.spotifystreamer.spotify.TrackInfo;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class PlayerFragment extends DialogFragment implements SeekBar.OnSeekBarChangeListener {

    public final static String TAG = "TAG_PLAYERFRAGMENT";

    private static final String ARG_TRACK_INFOS = "trackInfos";
    private static final String ARG_POSITION = "position";

    private List<TrackInfo> mTrackInfos;

    private int mCurrentTrack;

    private boolean mIsSeeking = false;

    private boolean mIsDialog = false;

    SeekBar mSeekBar;
    Handler mHandler;

    private ServiceConnection mMusicConnection;

    private PlayerService mPlayerService;

    private boolean mIsServiceBound;

    private final Messenger mMessenger = new Messenger(new IncomingHandler());
    private int mProgress;

    public PlayerFragment() {
    }

    public static PlayerFragment newInstance(List<TrackInfo> trackInfos, int position) {
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        ArrayList<TrackInfo> trackInfosArrayList = new ArrayList<TrackInfo>(trackInfos);
        args.putParcelableArrayList(ARG_TRACK_INFOS, trackInfosArrayList);
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();

        if (getArguments() != null) {
            mTrackInfos = getArguments().getParcelableArrayList(ARG_TRACK_INFOS);
            mCurrentTrack = getArguments().getInt(ARG_POSITION);
        } /* else started from the service notification */

        mMusicConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                PlayerService.MusicBinder binder = (PlayerService.MusicBinder)service;
                mPlayerService = binder.getService();
                mPlayerService.setMessenger(mMessenger);

                if (savedInstanceState == null && getArguments() != null) {
                    mCurrentTrack = getArguments().getInt(ARG_POSITION);
                    if (mCurrentTrack != mPlayerService.getCurrentTrack()) {
                        getActivity().startService(new Intent(getActivity(), PlayerService.class));
                        mPlayerService.setTrackInfos(mTrackInfos);
                        mPlayerService.init(mCurrentTrack);
                    }
                }
                mPlayerService.updatePlayerUi();
                mIsServiceBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mIsServiceBound = false;
            }
        };
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mIsDialog = true;

        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        getActivity().bindService(
                new Intent(getActivity(), PlayerService.class),
                mMusicConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unbindService(mMusicConnection);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_player, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        attachMediaControlListeners(view);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getArguments() != null) {
            getArguments().putInt(ARG_POSITION, mPlayerService.getCurrentTrack());
        }
    }

    private void attachMediaControlListeners(View view) {
        ImageButton playPause = (ImageButton)view.findViewById(R.id.button_play_pause);
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlayPauseButtonPressed(v);
            }
        });
        ImageButton next = (ImageButton)view.findViewById(R.id.button_next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNextButtonPressed(v);
            }
        });
        ImageButton previous = (ImageButton)view.findViewById(R.id.button_prev);
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPreviousButtonPressed(v);
            }
        });

        mSeekBar = (SeekBar)getView().findViewById(R.id.seekBar);
        mSeekBar.setOnSeekBarChangeListener(this);
        attachSeekBar();
    }

    /**
     * @param view - the ImageButton to Play/Pause
     */
    public void onPlayPauseButtonPressed(View view) {
        mPlayerService.togglePlayPause();
    }

    public void onNextButtonPressed(View view) {
        if (mPlayerService.next()) {
        }
    }

    public void onPreviousButtonPressed(View view) {
        if (mPlayerService.previous()) {
        }

    }

    private void attachSeekBar() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mPlayerService != null && !mIsSeeking) {
                    int currentPosition = mPlayerService.getCurrentPosition();
                    updateCurrentPosition(currentPosition);
                }
                mHandler.postDelayed(this, 500);
            }
        });
    }

    private void updateCurrentPosition(int position) {
        if (getView() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
            Date date = new Date(position * 1000);

            ((TextView)getView().findViewById(R.id.player_progress))
                    .setText(String.valueOf(sdf.format(date)));
        }
        mSeekBar.setProgress(position);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            mProgress = progress;
            ((TextView)getView().findViewById(R.id.player_progress))
                    .setText(String.valueOf(progress));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mIsSeeking = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mIsSeeking = false;

        updateCurrentPosition(mProgress);
        mPlayerService.seekTo(mProgress);
    }

    private void setDuration(int duration) {
        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
        Date date = new Date(duration * 1000);
        ((TextView)getView().findViewById(R.id.player_duration)).setText(String.valueOf(sdf.format(date)));
        mSeekBar.setMax(duration);
    }

    private void setPlayUi() {
        ImageView view = (ImageView)getView().findViewById(R.id.button_play_pause);
        view.setImageResource(android.R.drawable.ic_media_play);
    }

    private void setPauseUi() {
        ImageView view = (ImageView)getView().findViewById(R.id.button_play_pause);
        view.setImageResource(android.R.drawable.ic_media_pause);
    }

    private void updatePlayerUi(TrackInfo trackInfo) {
        String artist = trackInfo.getArtist();
        String album = trackInfo.getAlbumName();
        String track = trackInfo.getTrackName();
        String artwork = trackInfo.getLargeImageUrl();

        ((TextView)getView().findViewById(R.id.player_artist)).setText(artist);
        ((TextView)getView().findViewById(R.id.player_album)).setText(album);
        ((TextView)getView().findViewById(R.id.player_track)).setText(track);
        updateArtwork(artwork);

    }

    private void updateArtwork(String artwork) {
        ImageView imageView = (ImageView)getView().findViewById(R.id.player_artwork);
        int orientation = getResources().getConfiguration().orientation;
        int bounds = -1;

        if (mIsDialog) {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                bounds = (int)(getActivity().getWindow().peekDecorView().getHeight() * 0.5);
            }
        } else {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                bounds = (int)(getActivity().getWindow().peekDecorView().getHeight() * 0.3);
            } else {
                bounds = (int)(getActivity().getWindow().peekDecorView().getWidth() * 0.8);
            }
        }
        loadImage(artwork, imageView, bounds);
    }

    private void loadImage(String load, ImageView into, int resizeBounds) {
        if (resizeBounds < 0) {
            if (load != null) {
                Picasso.with(getActivity())
                        .load(load)
                        .into(into);
            } else {
                Picasso.with(getActivity())
                        .load(R.drawable.greybox)
                        .into(into);
            }
        } else {
            if (load != null) {
                Picasso.with(getActivity())
                        .load(load)
                        .resize(resizeBounds, resizeBounds)
                        .into(into);
            } else {
                Picasso.with(getActivity())
                        .load(R.drawable.greybox)
                        .resize(resizeBounds, resizeBounds)
                        .into(into);
            }
        }
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (getView() != null) {
                switch (msg.what) {
                    case PlayerService.MSG_FINISH:
                        boolean twoPane = getResources().getBoolean(R.bool.large_layout);
                        if (twoPane) {
                            dismiss();
                        } else {
                            getActivity().finish();
                        }
                        break;
                    case PlayerService.MSG_SET_DURATION:
                        setDuration(msg.arg1);
                        break;
                    case PlayerService.MSG_UPDATE_PLAYER:
                        TrackInfo trackInfo = msg.getData().getParcelable(PlayerService.KEY_TRACK_INFO);
                        updatePlayerUi(trackInfo);

                        int duration = msg.getData().getInt(PlayerService.KEY_DURATION);
                        setDuration(duration);
                    case PlayerService.MSG_UPDATE_PLAY_PAUSE:
                        if (msg.arg1 == 1) {
                            setPlayUi();
                        } else {
                            setPauseUi();
                        }
                    default:
                        super.handleMessage(msg);
                }
            }
        }
    }
}
