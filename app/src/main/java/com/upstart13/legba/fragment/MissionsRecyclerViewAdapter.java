package com.upstart13.legba.fragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.upstart13.legba.R;
import com.upstart13.legba.data.dto.Mission;

import java.util.ArrayList;
import java.util.List;

public class MissionsRecyclerViewAdapter extends ListAdapter<Mission, MissionsRecyclerViewAdapter.ItemViewHolder> {

    private List<Mission> missions = new ArrayList();
    private Context context;

    public void setMissions(List<Mission> missions){
        this.missions = missions;
    }

    public MissionsRecyclerViewAdapter(@NonNull DiffUtil.ItemCallback<Mission> diffCallback, Context context) {
        super(diffCallback);
        this.context = context;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mission_item, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Mission currentMission = missions.get(position);
        holder.missionName.setText(currentMission.name);
        holder.channelsNumber.setText(String.format("%s channels", currentMission.channels.size()));
        holder.channels.removeAllViews();
        //LayoutInflater.from(context).inflate(R.layout.channel_avatar, holder.channels, true);
        RoundedImageView newAvatar = (RoundedImageView) LayoutInflater.from(context).inflate(R.layout.channel_avatar, null);
        holder.channels.addView(newAvatar);
    }

    @Override
    public int getItemCount() {
        return missions.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {

        private View root;
        private TextView missionName;
        private TextView channelsNumber;
        private LinearLayout channels;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            this.root = itemView;
            missionName = itemView.findViewById(R.id.mission_name_text);
            channelsNumber = itemView.findViewById(R.id.mission_channels_number);
            channels = itemView.findViewById(R.id.channels_list_view);
        }
    }

    static class AdapterDiffCallback extends DiffUtil.ItemCallback<Mission> {

        @Override
        public boolean areItemsTheSame(@NonNull Mission oldItem, @NonNull Mission newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Mission oldItem, @NonNull Mission newItem) {
            return oldItem.toString().equals(newItem.toString());
        }
    }
}