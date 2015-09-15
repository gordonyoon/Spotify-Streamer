package com.example.gordonyoon.spotifystreamer.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import com.example.gordonyoon.spotifystreamer.service.PlayerService;

public class MusicIntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
            onMediaButtonPressed(context, KeyEvent.KEYCODE_MEDIA_PAUSE);
        } else if (action.equals(Intent.ACTION_MEDIA_BUTTON)) {
            final KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (event != null && event.getAction() == KeyEvent.ACTION_DOWN) {
                onMediaButtonPressed(context, event.getKeyCode());
            }
        }
    }

    private void onMediaButtonPressed(Context context, int actionKeyCode) {
        Intent playerIntent = new Intent(context, PlayerService.class);
        switch (actionKeyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                playerIntent.setAction(PlayerService.ACTION_PLAY_PAUSE);
                break;
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                playerIntent.setAction(PlayerService.ACTION_PAUSE);
                break;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                playerIntent.setAction(PlayerService.ACTION_NEXT);
                break;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                playerIntent.setAction(PlayerService.ACTION_PREVIOUS);
                break;
        }
        context.startService(playerIntent);
    }
}
