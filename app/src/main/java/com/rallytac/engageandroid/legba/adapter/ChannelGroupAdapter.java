package com.rallytac.engageandroid.legba.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.rallytac.engageandroid.R;
import com.rallytac.engageandroid.legba.data.dto.Channel;
import com.rallytac.engageandroid.legba.util.RUtils;

import java.util.List;
import java.util.stream.Collectors;

public class ChannelGroupAdapter extends RecyclerView.Adapter<ChannelGroupAdapter.ChannelGroupViewHolder>{

    private List<Channel> channels;
    private Context context;

    private int priorityIndicator = 1;
    private int radioIndicator = 1;

    public ChannelGroupAdapter(List<Channel> channels, Context context, RecyclerView.Adapter adapter) {
        this.channels = channels;
        this.context = context;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public List<Channel> getCheckChannels() {
        return channels.stream()
                .filter(channel -> channel.status)
                .collect(Collectors.toList());
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChannelGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_group_item, parent, false);
        ChannelGroupViewHolder channelGroupViewHolder = new ChannelGroupViewHolder(view);
        return channelGroupViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ChannelGroupViewHolder holder, int position) {
        Channel channel = channels.get(position);

        holder.channelPhoto.setImageResource(RUtils.getImageResource(channel.image));
        holder.channelNameText.setText(channel.name);
        holder.channelTypeText.setText(getTypeString(channel.type));
        setupCheckChannel(holder, channel);

        holder.principalLayoutItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                channel.status = !channel.status;
                setupCheckChannel(holder, channel);
            }
        });
    }

    private void setupCheckChannel(ChannelGroupViewHolder holder, Channel channel) {
        if(channel.status) {
            holder.principalLayoutItem.setBackground(ContextCompat.getDrawable(context,
                    R.drawable.channel_group_check_item_shape));
            holder.channelCheckImg.setImageResource(R.mipmap.group_158);
        } else {
            holder.principalLayoutItem.setBackground(ContextCompat.getDrawable(context,
                    R.drawable.channel_group_item_shape));
            holder.channelCheckImg.setImageResource(R.mipmap.ellipse_1084_copy_4);
        }
    }

    private String getTypeString(Channel.ChannelType type) {
        switch (type) {
            case PRIMARY:
                return "Primary Channel";
            case PRIORITY:
                String priority = "Priority Channel " + priorityIndicator;
                priorityIndicator++;
                return priority;
            case RADIO:
                String radio = "Radio Channel " + radioIndicator;
                radioIndicator++;
                return radio;
            default:
                return "";
        }
    }

    @Override
    public int getItemCount() {
        return channels.size();
    }

    public class ChannelGroupViewHolder extends RecyclerView.ViewHolder {
        private View principalLayoutItem;
        private ImageView channelPhoto;
        private TextView channelNameText;
        private TextView channelTypeText;
        private ImageView channelCheckImg;

        public ChannelGroupViewHolder(View itemView) {
            super(itemView);
            principalLayoutItem = itemView.findViewById(R.id.principal_layout_item);
            channelPhoto = itemView.findViewById(R.id.channel_photo);
            channelNameText = itemView.findViewById(R.id.channel_name_text);
            channelTypeText = itemView.findViewById(R.id.channel_type_text);
            channelCheckImg = itemView.findViewById(R.id.channel_check_img);
        }
    }

}