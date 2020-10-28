package com.rallytac.engageandroid.legba.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.rallytac.engageandroid.R;
import com.rallytac.engageandroid.legba.data.DataManager;
import com.rallytac.engageandroid.legba.data.dto.Channel;
import com.rallytac.engageandroid.legba.engage.RxListener;
import com.rallytac.engageandroid.legba.fragment.MissionFragment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.rallytac.engageandroid.legba.util.RUtils.getImageResource;

public class ChannelBigListAdapter extends RecyclerView.Adapter<ChannelBigListAdapter.ChannelFullViewHolder> {

    private MissionFragment fragment;
    private List<Channel> channels;

    public ChannelBigListAdapter(MissionFragment fragment) {
        this.fragment = fragment;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChannelFullViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.channels_full_item, parent, false);
        return new ChannelFullViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ChannelFullViewHolder holder, int position) {
        Channel currentChannel = channels.get(position);
        holder.channelImage.setImageResource(getImageResource(currentChannel.image));
        holder.channelName.setText(currentChannel.name);
        holder.channelType.setText(getTypeString(currentChannel.type));

        if (currentChannel.id.equals("{G2}")) {
            holder.channelImage.setBorderColor(getWaterBlueColor());
            holder.channelType.setTextColor(getWaterBlueColor());
        } else if (currentChannel.id.equals("{G3}")) {
            holder.channelImage.setBorderColor(getOrangeColor());
            holder.channelType.setTextColor(getOrangeColor());
        }

        setupSpeakerIcon(currentChannel.isSpeakerOn, holder.channelSpeaker);
        holder.channelSpeaker.setOnClickListener(view -> {
            currentChannel.isSpeakerOn = !currentChannel.isSpeakerOn;
            DataManager.getInstance(fragment.getContext()).toggleMute(currentChannel.id, currentChannel.isSpeakerOn);
            this.fragment.binding.missionViewPager.getAdapter().notifyDataSetChanged();
        });
    }

    private int getWaterBlueColor() {
        return fragment.getResources().getColor(R.color.waterBlue);
    }

    private int getOrangeColor() {
        return fragment.getResources().getColor(R.color.orange);
    }

    private String getTypeString(Channel.ChannelType type) {
        switch (type) {
            case PRIMARY:
                return "Primary Channel";
            case PRIORITY:
                return "Priority Channel";
            case RADIO:
                return "Radio Channel";
            default:
                return "";
        }
    }

    private void setupSpeakerIcon(boolean isOn, ImageView button) {
        if (isOn) {
            button.setImageResource(R.drawable.ic_speaker);
        } else {
            button.setImageResource(R.drawable.ic_speaker_off);
        }
    }

    @Override
    public int getItemCount() {
        return channels.size();
    }

    public class ChannelFullViewHolder extends RecyclerView.ViewHolder{
        private final static float LOW_OPACITY = 0.1f;
        private final static float FULL_OPACITY = 1f;

        private final static String PRIMARY_CHANNEL = "Primary Channel";
        private final static String PRIORITY_CHANNEL_1 = "Priority Channel";
        private final static String PRIORITY_CHANNEL_2 = "Priority Channel";

        private RoundedImageView channelImage;
        private TextView channelName;
        private TextView channelType;
        private ImageView channelMic;
        private ImageView channelSpeaker;

        public ChannelFullViewHolder(@NonNull View itemView) {
            super(itemView);

            channelImage = itemView.findViewById(R.id.channel_photo);
            channelName = itemView.findViewById(R.id.channel_name_text);
            channelType = itemView.findViewById(R.id.channel_type_text);
            channelSpeaker = itemView.findViewById(R.id.channel_speaker);
            channelMic = itemView.findViewById(R.id.channel_mic);
        }

/*        @Override
        public void onRx(String id, String other) {
            //setupViewIncommingMessage();
            //toggleLayoutVisiblity(incomingMessageLayout);
        }

        @Override
        public void onJsonRX(String id, String alias, String displayName) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm a");
            LocalDateTime now = LocalDateTime.now();
            String timeText = dtf.format(now);

            String aliasText = alias == null ? "UNKNOWN" : alias;
            String displayNameText = displayName == null ? "Unknown user" : displayName;

            //Incoming SOS
            //toggleLayoutVisiblity(incomingMessageLayout);
            //activity.binding.incomingSosOverlapMessageName.setText(aliasText);
        }

        @Override
        public void stopRx() {
            //incomingMessageLayout.setVisibility(View.INVISIBLE);
            //toggleLayoutVisiblity(activity.binding.incomingSosOverlapLayout);
        }*/
    }
}
