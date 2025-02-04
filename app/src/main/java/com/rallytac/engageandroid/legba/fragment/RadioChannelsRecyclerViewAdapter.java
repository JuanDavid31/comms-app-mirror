package com.rallytac.engageandroid.legba.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.makeramen.roundedimageview.RoundedImageView;

import com.rallytac.engageandroid.R;
import com.rallytac.engageandroid.legba.data.dto.Channel;

import java.util.ArrayList;
import java.util.List;

import static com.rallytac.engageandroid.legba.util.RUtils.getImageResource;

public class RadioChannelsRecyclerViewAdapter extends ListAdapter<Channel, RadioChannelsRecyclerViewAdapter.ItemViewHolder> {

    private List<Channel> radioChannels = new ArrayList();
    private Fragment fragment;

    public void setRadioChannels(List<Channel> radioChannels){
        this.radioChannels = radioChannels;
        notifyDataSetChanged();
    }

    public RadioChannelsRecyclerViewAdapter(@NonNull DiffUtil.ItemCallback<Channel> diffCallback, Fragment fragment) {
        super(diffCallback);
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.radio_channel_item, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Channel currentChannel = radioChannels.get(position);

        holder.radioChannelImage.setImageResource(getImageResource(currentChannel.getImage()));
        holder.radioChannelName.setText(currentChannel.getName());
    }

    @Override
    public int getItemCount() {
        return radioChannels.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {


        private RoundedImageView radioChannelImage;
        private TextView radioChannelName;
        //private LinearLayout channels;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            radioChannelImage = itemView.findViewById(R.id.radio_channel_image);
            radioChannelName = itemView.findViewById(R.id.radio_channel_name);
        }
    }

    static class AdapterDiffCallback extends DiffUtil.ItemCallback<Channel> {

        @Override
        public boolean areItemsTheSame(@NonNull Channel oldItem, @NonNull Channel newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Channel oldItem, @NonNull Channel newItem) {
            return oldItem.toString().equals(newItem.toString());
        }
    }
}