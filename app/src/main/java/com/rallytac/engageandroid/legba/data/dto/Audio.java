package com.rallytac.engageandroid.legba.data.dto;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;

import androidx.annotation.Nullable;

import com.google.gson.annotations.JsonAdapter;
import com.rallytac.engageandroid.legba.mapping.AudioDeserializer;

import java.time.LocalTime;

@JsonAdapter(AudioDeserializer.class)
public class Audio {

    public enum AudioType {Rx, Tx}

    public AudioType type;

    public LocalTime startedTime;

    public String sender;

    public Uri audioUri;

    public int durationInSeconds;

    public MediaPlayer mediaPlayer;

    public int mediaPlayerPosition;

    public Handler mediaPlayerHandler;

    public Audio(AudioType type, LocalTime startedTime, String sender, Uri audioUri, int durationInSeconds) {
        this.type = type;
        this.startedTime = startedTime;
        this.sender = sender;
        this.audioUri = audioUri;
        this.durationInSeconds = durationInSeconds;
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