package com.example.gordonyoon.spotifystreamer.activity;


import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.gordonyoon.spotifystreamer.R;
import com.example.gordonyoon.spotifystreamer.fragment.ArtistFragment;
import com.example.gordonyoon.spotifystreamer.fragment.PlayerFragment;
import com.example.gordonyoon.spotifystreamer.fragment.SelectCountryDialogFragment;
import com.example.gordonyoon.spotifystreamer.fragment.TrackFragment;
import com.example.gordonyoon.spotifystreamer.service.PlayerService;
import com.example.gordonyoon.spotifystreamer.spotify.TrackInfo;

import java.util.List;

public class MainActivity extends ActionBarActivity
        implements ArtistFragment.OnArtistSelectedListener, ArtistFragment.OnArtistSearchedListener,
        TrackFragment.OnTrackSelectedListener {

    public final static String KEY_PREF_SHOW_CTRLS = "showControls";

    private ShareActionProvider mShareActionProvider;

    private boolean mTwoPane;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mTitle;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String trackUrl = intent.getStringExtra(PlayerService.KEY_TRACK_URL);
            if (trackUrl != null) {
                if (mShareActionProvider != null) {
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, trackUrl);
                    shareIntent.setType("text/plain");
                    mShareActionProvider.setShareIntent(shareIntent);
                }
            } else {
                boolean playState = intent.getBooleanExtra(PlayerService.KEY_PLAY_STATE, false);
                updateDrawerPlayPauseButton(playState);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            FragmentManager manager = getSupportFragmentManager();
            if (manager.findFragmentByTag(ArtistFragment.TAG) == null) {
                ArtistFragment artistFragment = ArtistFragment.newInstance();
                manager.beginTransaction()
                        .add(R.id.fragment_container, artistFragment, ArtistFragment.TAG)
                        .addToBackStack(ArtistFragment.TAG).commit();

                mTitle = getTitle();
            }
        }

        mTwoPane = getResources().getBoolean(R.bool.large_layout);

        initializeDrawer();
        attachDrawerMediaButtonListeners();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            stopService(new Intent(this, PlayerService.class));
        }
    }

    private void initializeDrawer() {
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(R.string.now_playing);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void attachDrawerMediaButtonListeners() {
        final SharedPreferences prefs = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        final Intent intent = new Intent(this, PlayerService.class);

        ImageButton previous = (ImageButton)findViewById(R.id.drawer_button_prev);
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean showControls = prefs.getBoolean(KEY_PREF_SHOW_CTRLS, true);
                if (PlayerService.isServiceRunning() && showControls) {
                    intent.setAction(PlayerService.ACTION_PREVIOUS);
                    startService(intent);
                }
            }
        });
        ImageButton playPause = (ImageButton)findViewById(R.id.drawer_button_play_pause);
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean showControls = prefs.getBoolean(KEY_PREF_SHOW_CTRLS, true);
                if (PlayerService.isServiceRunning() && showControls) {
                    intent.setAction(PlayerService.ACTION_PLAY_PAUSE);
                    startService(intent);
                }
            }
        });
        ImageButton next = (ImageButton)findViewById(R.id.drawer_button_next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean showControls = prefs.getBoolean(KEY_PREF_SHOW_CTRLS, true);
                if (PlayerService.isServiceRunning() && showControls) {
                    intent.setAction(PlayerService.ACTION_NEXT);
                    startService(intent);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider)MenuItemCompat.getActionProvider(item);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        mShareActionProvider.setShareIntent(intent);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.action_now_playing:
                openPlayer();
                break;
            case R.id.select_country:
                SelectCountryDialogFragment f = new SelectCountryDialogFragment();
                f.show(getSupportFragmentManager(), SelectCountryDialogFragment.TAG);
                break;
            case R.id.toggle_controls:
                openToggleControlsDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void openToggleControlsDialog() {
        final SharedPreferences prefs = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        boolean showControls = prefs.getBoolean(KEY_PREF_SHOW_CTRLS, true);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setNegativeButton(R.string.dialog_button_negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        if (showControls) {
            builder.setTitle(R.string.dialog_title_disable)
                    .setMessage(R.string.dialog_message_disable)
                    .setPositiveButton(R.string.dialog_positive_button_disable,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    toggleControls(false);

                                }
                            });
        } else {
            builder.setTitle(R.string.dialog_title_enable)
                    .setMessage(R.string.dialog_message_enable)
                    .setPositiveButton(R.string.dialog_button_positive_enable,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    toggleControls(true);
                                }
                            });

        }

        builder.show();
    }

    private void toggleControls(boolean enable) {
        final SharedPreferences prefs = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_PREF_SHOW_CTRLS, enable).commit();

        if (PlayerService.isServiceRunning()) {
            Intent intent = new Intent(this, PlayerService.class);
            intent.setAction(PlayerService.ACTION_TOGGLE_CTRLS);
            startService(intent);
        }
    }

    @Override
    public void onBackPressed() {
        if (!mTwoPane) {
            FragmentManager manager = getSupportFragmentManager();
            int backStackCount = manager.getBackStackEntryCount();
            if (backStackCount > 1) {
                String name = manager.getBackStackEntryAt(backStackCount - 2).getName();
                manager.popBackStackImmediate(name, 0);
                Fragment f = manager.findFragmentByTag(name);
                if (f != null) {
                    manager.beginTransaction().replace(R.id.fragment_container, f).commit();
                    if (name.equals(ArtistFragment.TAG) || name.equals(PlayerFragment.TAG)) {
                        setActionBarTitle(R.string.app_name);
                    } else if (name.equals(TrackFragment.TAG)) {
                        setActionBarTitle(R.string.title_top_tracks);
                    }
                    return;
                }
            }
        }
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // intended for when PlayerDialog is opened via notification
        openPlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mBroadcastReceiver, new IntentFilter(PlayerService.BROADCAST_ACTION));
    }

    private void openPlayer() {
        if (PlayerService.isServiceRunning()) {
            FragmentManager manager = getSupportFragmentManager();
            if (manager.findFragmentByTag(PlayerFragment.TAG) == null) {
                if (mTwoPane) {
                    new PlayerFragment().show(manager, PlayerFragment.TAG);
                } else {
                    manager.beginTransaction()
                            .replace(R.id.fragment_container, new PlayerFragment())
                            .addToBackStack(PlayerFragment.TAG).commit();
                }
            }
            setActionBarTitle(R.string.app_name);
        } else {
            Toast.makeText(this, R.string.toast_empty_player, Toast.LENGTH_SHORT).show();
        }
    }

    private void setActionBarTitle(int titleRes) {
        mTitle = getString(titleRes);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(titleRes));
        }
    }

    @Override
    public void onTrackSelected(List<TrackInfo> trackInfos, int position) {
        PlayerFragment playerFragment = PlayerFragment.newInstance(trackInfos, position);
        if (mTwoPane) {
            playerFragment.show(getSupportFragmentManager(), PlayerFragment.TAG);
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, playerFragment, PlayerFragment.TAG)
                    .addToBackStack(PlayerFragment.TAG).commit();
            setActionBarTitle(R.string.app_name);
        }
    }

    @Override
    public void onArtistSelected(String spotifyId) {
        int containerId = (mTwoPane) ? R.id.tracks_container : R.id.fragment_container;
        TrackFragment trackFragment = TrackFragment.newInstance(spotifyId);

        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction()
                .replace(containerId, trackFragment, TrackFragment.TAG)
                .addToBackStack(TrackFragment.TAG).commit();

        if (!mTwoPane) {
            setActionBarTitle(R.string.title_top_tracks);
        }
    }

    @Override
    public void onArtistSearched() {
        FragmentManager manager = getSupportFragmentManager();
        Fragment f = manager.findFragmentByTag(TrackFragment.TAG);
        if (f != null) {
            manager.beginTransaction().remove(f).commit();
        }
    }

    private void updateDrawerPlayPauseButton(boolean playState) {
        ImageButton button = (ImageButton)findViewById(R.id.drawer_button_play_pause);
        if (button != null) {
            if (playState) {
                button.setImageResource(android.R.drawable.ic_media_pause);
            } else {
                button.setImageResource(android.R.drawable.ic_media_play);
            }
        }
    }
}
