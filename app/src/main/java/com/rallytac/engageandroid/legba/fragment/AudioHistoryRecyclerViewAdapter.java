package com.rallytac.engageandroid.legba.fragment;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.rallytac.engageandroid.R;
import com.rallytac.engageandroid.legba.HostActivity;
import com.rallytac.engageandroid.legba.data.dto.Audio;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.LogRecord;

public class AudioHistoryRecyclerViewAdapter extends ListAdapter<Audio, AudioHistoryRecyclerViewAdapter.AudioHistoryViewHolder> {

    private List<Audio> audios;
    private Context context;

    final int ODD_VIEW = 1;
    final int EVEN_VIEW = 2;

    protected AudioHistoryRecyclerViewAdapter(@NonNull DiffUtil.ItemCallback<Audio> diffCallback, List<Audio> audios, Context context) {
        super(diffCallback);
        this.audios = audios;
        this.context = context;
    }

    public List<Audio> getAudios(){
        return audios;
    }

    public void setAudios(List<Audio> audios) {
        this.audios = audios;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return audios.size();
    }

    @NonNull
    @Override
    public AudioHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == EVEN_VIEW) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.left_audio_history_item, parent, false);
            return new LeftAudioHistoryViewHolder(itemView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.right_audio_history_item, parent, false);
            return new AudioHistoryViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull AudioHistoryViewHolder holder, int position) {
        Audio currentAudio = audios.get(position);
        holder.name.setText(currentAudio.sender);
        holder.memberNickName.setText(currentAudio.sender); //TODO: nickname cant be reached

        DateTimeFormatter format = DateTimeFormatter.ofPattern("hh:mm a");
        holder.startedTime.setText(currentAudio.startedTime.format(format));

        int minutes = (currentAudio.durationInSeconds % 3600) / 60;
        int seconds = currentAudio.durationInSeconds % 60;

        holder.audioDuration.setText(String.format("%01d:%02d", minutes, seconds));


        //Audio stuff
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );

        mediaPlayer.setOnPreparedListener(preparedMediaPlayer -> {
            currentAudio.mediaPlayer = preparedMediaPlayer;
            holder.playStopButton.setOnClickListener(view -> {
                if (preparedMediaPlayer.isPlaying()) {
                    preparedMediaPlayer.pause();
                    currentAudio.mediaPlayerPosition = preparedMediaPlayer.getCurrentPosition();
                    holder.playStopButton.setImageResource(R.drawable.ic_round_play_arrow_24);

                    currentAudio.mediaPlayerHandler.removeCallbacksAndMessages(null);
                } else {
                    preparedMediaPlayer.seekTo(currentAudio.mediaPlayerPosition);
                    preparedMediaPlayer.start();
                    holder.playStopButton.setImageResource(R.drawable.ic_round_pause_24);

                    // Connection between Seekbar and MediaPlayer

                    holder.audioBar.setMax(mediaPlayer.getDuration() / 1000);

                    currentAudio.mediaPlayerHandler = new Handler();

                    ((HostActivity)context).runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if(currentAudio.mediaPlayer != null){
                                int mCurrentPosition = currentAudio.mediaPlayer.getCurrentPosition() / 1000;
                                holder.audioBar.setProgress(mCurrentPosition);
                            }
                            currentAudio.mediaPlayerHandler.postDelayed(this, 1000);
                        }
                    });

                }
            });
        });

        mediaPlayer.setOnCompletionListener(completedMediaPlayer -> {
            currentAudio.mediaPlayerPosition = 0;
            holder.playStopButton.setImageResource(R.drawable.ic_round_play_arrow_24);
        });

        try {
            mediaPlayer.setDataSource(context, currentAudio.audioUri);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemViewType(int position) {
        int realPosition = position + 1;
        return realPosition % 2 == 0 ? EVEN_VIEW : ODD_VIEW;
    }

    static class AudioHistoryViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView memberNickName;
        TextView memberNumber;
        TextView startedTime;
        ImageButton playStopButton;
        SeekBar audioBar;
        TextView audioDuration;

        public AudioHistoryViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.audio_history_member_name_text);
            memberNickName = itemView.findViewById(R.id.audio_history_member_nickname_text);
            memberNumber = itemView.findViewById(R.id.audio_history_member_number_text);
            startedTime = itemView.findViewById(R.id.audio_history_started_time);
            playStopButton = itemView.findViewById(R.id.audio_history_play_stop_button);
            audioBar = itemView.findViewById(R.id.audio_history_seekbar);
            audioDuration = itemView.findViewById(R.id.audio_history_duration);
        }
    }

    static class LeftAudioHistoryViewHolder extends AudioHistoryViewHolder {

        private TextView membersCaps;

        public LeftAudioHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            membersCaps = itemView.findViewById(R.id.audio_history_member_caps_text);
        }
    }

    static class AdapterDiffCallback extends DiffUtil.ItemCallback<Audio> {

        @Override
        public boolean areItemsTheSame(@NonNull Audio oldItem, @NonNull Audio newItem) {
            return false;//oldItem.getId() == newItem.getId(); TODO: Fix
        }

        @Override
        public boolean areContentsTheSame(@NonNull Audio oldItem, @NonNull Audio newItem) {
            return oldItem.equals(newItem);
        }
    }
}
