package com.rallytac.engageandroid.legba.data.dto;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.widget.SeekBar;

import androidx.annotation.Nullable;

import com.google.gson.annotations.JsonAdapter;
import com.rallytac.engageandroid.legba.HostActivity;
import com.rallytac.engageandroid.legba.mapping.AudioDeserializer;

import java.time.LocalTime;

import timber.log.Timber;

@JsonAdapter(AudioDeserializer.class)
public class Audio {

    public enum AudioType {Rx, Tx}

    public String id;

    public AudioType type;

    public LocalTime startedTime;

    public String sender;

    public Uri audioUri;

    public int durationInSeconds;

    public MediaPlayer mediaPlayer;

    public int mediaPlayerPosition;

    public Handler mediaPlayerHandler;

    public Audio(String id, AudioType type, LocalTime startedTime, String sender, Uri audioUri, int durationInSeconds) {
        this.id = id;
        this.type = type;
        this.startedTime = startedTime;
        this.sender = sender;
        this.audioUri = audioUri;
        this.durationInSeconds = durationInSeconds;
    }

    public void connectWithSeekbar(SeekBar audioBar, Context context) {
        audioBar.setMax(mediaPlayer.getDuration());

        mediaPlayerHandler = new Handler();

        ((HostActivity) context).runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (mediaPlayer != null) {
                    /*try {*/
                        int mCurrentPosition = mediaPlayer.getCurrentPosition();
                        audioBar.setProgress(mCurrentPosition);
                    /*}catch (IllegalStateException e) {
                        e.printStackTrace();
                        Timber.e("MediaPlayer isPlaying %s", mediaPlayer.isPlaying());
                    }*/
                }
                mediaPlayerHandler.postDelayed(this, 100);
            }
        });
    }

    public void disconnectSeekbar() {
        if (mediaPlayerHandler != null) {
            mediaPlayerHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public String toString() {
        return "Audio{" +
                "type=" + type +
                ", startedTime=" + startedTime +
                ", sender='" + sender + '\'' +
                ", audioUri=" + audioUri +
                ", durationInSeconds=" + durationInSeconds +
                '}';
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return toString().equals(obj.toString());
    }
}