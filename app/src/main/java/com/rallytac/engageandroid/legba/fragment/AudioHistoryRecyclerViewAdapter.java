package com.rallytac.engageandroid.legba.fragment;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
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

import com.rallytac.engageandroid.Globals;
import com.rallytac.engageandroid.R;
import com.rallytac.engageandroid.legba.data.dto.Audio;
import com.rallytac.engageandroid.legba.util.StringUtils;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AudioHistoryRecyclerViewAdapter extends ListAdapter<Audio, AudioHistoryRecyclerViewAdapter.AudioHistoryViewHolder> {

    private List<Audio> audios;
    private Context context;

    final int SENDER_VIEW = 1;
    final int RECEIVER_VIEW = 2;

    protected AudioHistoryRecyclerViewAdapter(@NonNull DiffUtil.ItemCallback<Audio> diffCallback, List<Audio> audios, Context context) {
        super(diffCallback);
        this.audios = audios;
        this.context = context;
    }

    public List<Audio> getAudios(){
        return audios;
    }

    @Override
    public int getItemCount() {
        return audios.size();
    }

    @NonNull
    @Override
    public AudioHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == RECEIVER_VIEW) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.receiver_audio_history_item, parent, false);
            return new ReceiverAudioHistoryViewHolder(itemView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.sender_audio_history_item, parent, false);
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

        if (holder instanceof ReceiverAudioHistoryViewHolder){
            ReceiverAudioHistoryViewHolder receiverAudioHistoryViewHolder = (ReceiverAudioHistoryViewHolder)holder;
            String nameInCaps = StringUtils.getFirstLetterCapsFrom(currentAudio.sender);
            receiverAudioHistoryViewHolder.membersCaps.setText(nameInCaps);
        }

        prepareMediaPlayer(currentAudio, holder);
        setupSeekbarListener(currentAudio, holder);
        //TODO: Set state according to media player
    }

    private void prepareMediaPlayer(Audio audio, AudioHistoryViewHolder holder) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );

        mediaPlayer.setOnPreparedListener(preparedMediaPlayer -> {
            audio.mediaPlayer = preparedMediaPlayer;
            holder.playStopButton.setOnClickListener(view -> {
                if (preparedMediaPlayer.isPlaying()) {
                    preparedMediaPlayer.pause();
                    audio.mediaPlayerPosition = preparedMediaPlayer.getCurrentPosition();
                    holder.playStopButton.setImageResource(R.drawable.ic_round_play_arrow_24);
                    audio.disconnectSeekbar();
                } else {
                    preparedMediaPlayer.seekTo(audio.mediaPlayerPosition);
                    preparedMediaPlayer.start();
                    holder.playStopButton.setImageResource(R.drawable.ic_round_pause_24);
                    audio.connectWithSeekbar(holder.audioBar, context);
                }
            });
        });

        mediaPlayer.setOnCompletionListener(completedMediaPlayer -> {
            audio.mediaPlayerPosition = 0;
            holder.audioBar.setProgress(0);
            audio.disconnectSeekbar();
            holder.playStopButton.setImageResource(R.drawable.ic_round_play_arrow_24);
        });

        try {
            mediaPlayer.setDataSource(context, audio.audioUri);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupSeekbarListener(Audio audio, AudioHistoryViewHolder holder){
        holder.audioBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean changedFromUser) {
                if (audio.mediaPlayer != null && changedFromUser){
                    audio.mediaPlayerPosition = progress;
                    audio.mediaPlayer.seekTo(progress);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    public int getItemViewType(int position) {
        Audio currentAudio = audios.get(position);
        String userAlias = Globals.getEngageApplication().getActiveConfiguration().getUserAlias();
        if (currentAudio.sender.equalsIgnoreCase(userAlias)){
            return SENDER_VIEW;
        } else {
            return RECEIVER_VIEW;
        }
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

    static class ReceiverAudioHistoryViewHolder extends AudioHistoryViewHolder {

        private TextView membersCaps;

        public ReceiverAudioHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            membersCaps = itemView.findViewById(R.id.audio_history_member_caps_text);
        }
    }

    static class AdapterDiffCallback extends DiffUtil.ItemCallback<Audio> {

        @Override
        public boolean areItemsTheSame(@NonNull Audio oldItem, @NonNull Audio newItem) {
            return oldItem.id.equals(newItem.id);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Audio oldItem, @NonNull Audio newItem) {
            return oldItem.equals(newItem);
        }
    }
}
