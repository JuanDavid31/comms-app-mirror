package com.rallytac.engageandroid.legba.app;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.ArrayDeque;
import java.util.Locale;
import java.util.Queue;

public class VoiceRecognition implements TextToSpeech.OnInitListener {
    private TextToSpeech textToSpeech;
    private Context context;
    private Queue<String> queue;

    private static VoiceRecognition voiceRecognition;

    public static VoiceRecognition getInstance(Context context) {
        if (voiceRecognition == null) {
            voiceRecognition = new VoiceRecognition(context);
        }
        return voiceRecognition;
    }

    private VoiceRecognition(Context context) {
        this.context = context;
        textToSpeech = new TextToSpeech(context, this);
        textToSpeech.setPitch(1f);
        textToSpeech.setSpeechRate(1f);
        queue = new ArrayDeque<>();
    }

    public void speak(String text) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH,null);
        if(!textToSpeech.isSpeaking()) {
            queue.add(text);
        }
    }

    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.US);
            textToSpeech.setPitch(1f);
            textToSpeech.setSpeechRate(1f);

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language is not supported");
            } else {
                while(!queue.isEmpty()) {
                    speak(queue.poll());
                }
            }
        } else {
            Log.e("TTS", "Initilization Failed");
        }
    }

    public void stopVoiceRecognition() {
        if (textToSpeech != null) {
            textToSpeech.shutdown();
            textToSpeech.stop();
            voiceRecognition = null;
        }
    }
}
