package com.rallytac.engageandroid.legba.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
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
import java.util.stream.IntStream;

import static com.rallytac.engageandroid.legba.util.RUtils.getImageResource;

public class ChannelBigListAdapter extends RecyclerView.Adapter<ChannelBigListAdapter.ChannelViewHolder> {

    private MissionFragment fragment;
    private List<Channel> channels;
    private Context context;

    public ChannelBigListAdapter(MissionFragment fragment, Context context) {
        this.fragment = fragment;
        this.context = context;
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
        return new ChannelViewHolder(itemView, context);
    }

    @Override
    public void onBindViewHolder(@NonNull ChannelViewHolder holder, int position) {
        Channel currentChannel = channels.get(position);
        holder.channelImage.setImageResource(getImageResource(currentChannel.getImage()));
        holder.channelName.setText(currentChannel.getName());
        holder.channelTypeText.setText(getTypeString(currentChannel.getType()));
        holder.type = currentChannel.getType();
        holder.channelId = currentChannel.getId();

        int color = getColor(position);
        holder.channelImage.setBorderColor(color);
        holder.channelTypeText.setTextColor(color);
        holder.setBackground(getBackground(color));

        setupSpeakerIcon(currentChannel.isSpeakerOn(), holder.channelSpeaker);
        holder.channelSpeaker.setOnClickListener(view -> {
            currentChannel.setSpeakerOn(!currentChannel.isSpeakerOn());
               DataManager.getInstance(fragment.getContext()).toggleMute(currentChannel.getId(), currentChannel.isSpeakerOn());
            this.fragment.binding.missionViewPager.getAdapter().notifyDataSetChanged();
        });

        setViewState(currentChannel, holder);
    }

    private Drawable getBackground(int position) {
        //ContextCompat.getDrawable(this.context, R.drawable.primary_channel_item_fade_shape)
        boolean match0 = IntStream.iterate(0, i -> i + 6).limit(10).anyMatch(i -> i == position);
        boolean match1 = IntStream.iterate(1, i -> i + 6).limit(10).anyMatch(i -> i == position);
        boolean match2 = IntStream.iterate(2, i -> i + 6).limit(10).anyMatch(i -> i == position);
        boolean match3 = IntStream.iterate(3, i -> i + 6).limit(10).anyMatch(i -> i == position);
        boolean match4 = IntStream.iterate(4, i -> i + 6).limit(10).anyMatch(i -> i == position);
        boolean match5 = IntStream.iterate(5, i -> i + 6).limit(10).anyMatch(i -> i == position);

        if(match0){ return ContextCompat.getDrawable(this.context, R.drawable.primary_channel_item_fade_shape); }
        if(match1){ return ContextCompat.getDrawable(this.context, R.drawable.prioritary1_channel_item_fade_shape); }
        if(match2){ return ContextCompat.getDrawable(this.context, R.drawable.prioritary2_channel_item_fade_shape); }
        if(match3){ return ContextCompat.getDrawable(this.context, R.drawable.prioritary3_channel_item_fade_shape); }
        if(match4){ return ContextCompat.getDrawable(this.context, R.drawable.prioritary4_channel_item_fade_shape); }
        if(match5){ return ContextCompat.getDrawable(this.context, R.drawable.prioritary5_channel_item_fade_shape); }
        return null;
    }

    private int getColor(int position){ //Limiting to 10 iterations, max number will be 60.
        boolean match0 = IntStream.iterate(0, i -> i + 6).limit(10).anyMatch(i -> i == position);
        boolean match1 = IntStream.iterate(1, i -> i + 6).limit(10).anyMatch(i -> i == position);
        boolean match2 = IntStream.iterate(2, i -> i + 6).limit(10).anyMatch(i -> i == position);
        boolean match3 = IntStream.iterate(3, i -> i + 6).limit(10).anyMatch(i -> i == position);
        boolean match4 = IntStream.iterate(4, i -> i + 6).limit(10).anyMatch(i -> i == position);
        boolean match5 = IntStream.iterate(5, i -> i + 6).limit(10).anyMatch(i -> i == position);

        if(match0){ return fragment.getResources().getColor(R.color.paleRed, null); }
        if(match1){ return fragment.getResources().getColor(R.color.waterBlue, null); }
        if(match2){ return fragment.getResources().getColor(R.color.orange, null); }
        if(match3){ return fragment.getResources().getColor(R.color.sicklyYellow, null); }
        if(match4){ return fragment.getResources().getColor(R.color.vividGreen, null); }
        if(match5){ return fragment.getResources().getColor(R.color.violet, null); }
        return 0;
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
            holder.setReceivingState(currentChannel.getRxAlias());
        }else{
            boolean brotherViewIsOnRx = this.channels.stream().anyMatch(channel -> channel.isOnRx());
            if(brotherViewIsOnRx){
                holder.setBrotherIsReceivingState();
            }else{
                holder.setNeutralState();
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
        private Context context;

        public ChannelViewHolder(@NonNull View itemView, Context context) {
            super(itemView);

            root = itemView;
            this.context = context;
            channelImage = itemView.findViewById(R.id.channel_photo);
            channelName = itemView.findViewById(R.id.channel_name_text);
            channelTypeText = itemView.findViewById(R.id.channel_type_text);
            channelSpeaker = itemView.findViewById(R.id.channel_speaker);
            //channelMic = itemView.findViewById(R.id.channel_mic);
            rxImage = itemView.findViewById(R.id.rx_image_primary_channel);
        }

        public void setReceivingState(String alias) {
            channelTypeText.setText(alias);
            rxImage.setVisibility(View.VISIBLE);
            root.setBackground(receivingStateBackground);

            //This prevents the following bug:
            //If there are multiple RX, the first channel will update correctly but from the second and so on,
            //a mix between fadeIn and fadeout state will happen
            fadeIn();
        }

        private void fadeIn() {
            channelImage.setAlpha(FULL_OPACITY);
            channelName.setAlpha(FULL_OPACITY);
            channelTypeText.setAlpha(FULL_OPACITY);
        }

        public void setBrotherIsReceivingState(){
            root.setBackground(ContextCompat.getDrawable(this.context, R.drawable.channel_item_shape));
            fadeOut();
        }

        private void fadeOut(){
            channelImage.setAlpha(LOW_OPACITY);
            channelName.setAlpha(LOW_OPACITY);
            channelTypeText.setAlpha(LOW_OPACITY);
        }

        public void setNeutralState(){
            channelTypeText.setText(getTypeString(type));
            rxImage.setVisibility(View.GONE);
            root.setBackground(ContextCompat.getDrawable(this.context, R.drawable.channel_item_shape));
            fadeIn();
        }

        private Drawable receivingStateBackground;

        public void setBackground(Drawable background) {
            this.receivingStateBackground = background;
        }
    }
}
