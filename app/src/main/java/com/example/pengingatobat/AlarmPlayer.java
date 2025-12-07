package com.example.pengingatobat;

import android.content.Context;
import android.media.MediaPlayer;

public class AlarmPlayer {
    public static MediaPlayer mediaPlayer = null;

    public static void play(Context context, int resId) {
        if (mediaPlayer == null || !mediaPlayer.isPlaying()) {
            mediaPlayer = MediaPlayer.create(context, resId);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }
    }

    public static void stop() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
