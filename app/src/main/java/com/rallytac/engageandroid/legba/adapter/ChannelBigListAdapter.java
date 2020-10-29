package com.rallytac.engageandroid.legba.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.rallytac.engageandroid.R;
import com.rallytac.engageandroid.legba.data.DataManager;
import com.rallytac.engageandroid.legba.data.dto.Channel;
import com.rallytac.engageandroid.legba.fragment.MissionFragment;

import java.util.List;

import static com.rallytac.engageandroid.legba.util.RUtils.getImageResource;

public class ChannelBigListAdapter extends RecyclerView.Adapter<ChannelBigListAdapter.ChannelViewHolder> {

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
    public ChannelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.channels_full_item, parent, false);
        return new ChannelViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ChannelViewHolder holder, int position) {
        Channel currentChannel = channels.get(position);
        holder.channelImage.setImageResource(getImageResource(currentChannel.getImage()));
        holder.channelName.setText(currentChannel.getName());
        holder.channelTypeText.setText(getTypeString(currentChannel.getType()));
        holder.type = currentChannel.getType();

        if (currentChannel.getId().equals("{G2}")) {
            holder.channelImage.setBorderColor(getWaterBlueColor());
            holder.channelTypeText.setTextColor(getWaterBlueColor());
        } else if (currentChannel.getId().equals("{G3}")) {
            holder.channelImage.setBorderColor(getOrangeColor());
            holder.channelTypeText.setTextColor(getOrangeColor());
        }

        holder.channelId = currentChannel.getId();

        setupSpeakerIcon(currentChannel.isSpeakerOn(), holder.channelSpeaker);
        holder.channelSpeaker.setOnClickListener(view -> {
            currentChannel.setSpeakerOn(!currentChannel.isSpeakerOn());
               DataManager.getInstance(fragment.getContext()).toggleMute(currentChannel.getId(), currentChannel.isSpeakerOn());
            this.fragment.binding.missionViewPager.getAdapter().notifyDataSetChanged();
        });

        setViewState(currentChannel, holder);
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

    private void setViewState(Channel currentChannel, ChannelViewHolder holder){
        if(currentChannel.isOnRx()){
            holder.showIncomingMessage(currentChannel.getRxAlias());
        }else{
            boolean brotherViewIsOnRx = this.channels.stream().anyMatch(channel -> channel.isOnRx());
            if(brotherViewIsOnRx){
                holder.fadeOut();
            }
        }
    }

    @Override
    public int getItemCount() {
        return channels.size();
    }

    public class ChannelViewHolder extends RecyclerView.ViewHolder{
        private final static float LOW_OPACITY = 0.1f;
        private final static float FULL_OPACITY = 1f;

        private View root;
        private RoundedImageView channelImage;
        private TextView channelName;
        private TextView channelTypeText;
        //private ImageView channelMic;
        private ImageView channelSpeaker;
        private ImageView rxImage;

        private Channel.ChannelType type;
        public String channelId;

        public ChannelViewHolder(@NonNull View itemView) {
            super(itemView);

            root = itemView;
            channelImage = itemView.findViewById(R.id.channel_photo);
            channelName = itemView.findViewById(R.id.channel_name_text);
            channelTypeText = itemView.findViewById(R.id.channel_type_text);
            channelSpeaker = itemView.findViewById(R.id.channel_speaker);
            //channelMic = itemView.findViewById(R.id.channel_mic);
            rxImage = itemView.findViewById(R.id.rx_image_primary_channel);
        }

        public void showIncomingMessage(String alias) {
            channelTypeText.setText(alias);
            rxImage.setVisibility(View.VISIBLE);
            root.setBackground(ContextCompat.getDrawable(fragment.getContext(), R.drawable.primary_channel_item_fade_shape));

            //This prevents the following bug:
            //If there are multiple RX, the first channel will update correctly but from the second and so on,
            //a mix between fadeIn and fadeout state will happen
            fadeIn();
        }

        public void fadeIn() {
            channelImage.setAlpha(FULL_OPACITY);
            channelName.setAlpha(FULL_OPACITY);
            channelTypeText.setAlpha(FULL_OPACITY);
        }

        public void fadeOut(){
            channelImage.setAlpha(LOW_OPACITY);
            channelName.setAlpha(LOW_OPACITY);
            channelTypeText.setAlpha(LOW_OPACITY);
        }

        public void hideIncomingMessage(){
            channelTypeText.setText(getTypeString(type));
            rxImage.setVisibility(View.GONE);
            root.setBackground(ContextCompat.getDrawable(fragment.getContext(), R.drawable.channel_item_shape));
        }
    }
}
