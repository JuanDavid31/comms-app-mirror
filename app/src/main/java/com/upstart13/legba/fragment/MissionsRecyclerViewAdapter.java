package com.upstart13.legba.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.upstart13.legba.R;
import com.upstart13.legba.data.dto.Channel;
import com.upstart13.legba.data.dto.Mission;

import java.util.ArrayList;
import java.util.List;

public class MissionsRecyclerViewAdapter extends ListAdapter<Mission, MissionsRecyclerViewAdapter.ItemViewHolder> {

    private List<Mission> missions = new ArrayList();
    private Fragment fragment;

    public void setMissions(List<Mission> missions){
        this.missions = missions;
        notifyDataSetChanged();
    }

    public MissionsRecyclerViewAdapter(@NonNull DiffUtil.ItemCallback<Mission> diffCallback, Fragment fragment) {
        super(diffCallback);
        this.fragment = fragment;
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

        holder.rightArrow.setOnClickListener(view -> NavHostFragment.findNavController(fragment)
                .navigate(MissionsListFragmentDirections.actionMissionsFragmentToMissionFragment(currentMission)));

        addChannelsToView(currentMission.channels, holder.channels);
        addRemainingChannelsText(currentMission.channels, holder.channels);
    }

    private void addChannelsToView(List<Channel> channels, LinearLayout channelsView){
        boolean red = true;
        boolean orange = false;
        boolean blue = false;
        boolean green = false;

        for(int i = 0; (i < 6) && (i < channels.size()); i++){
            RoundedImageView newAvatar = (RoundedImageView) LayoutInflater.from(fragment.getContext()).inflate(R.layout.channel_avatar, null);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(convertDpToPx(26.4), ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.setMarginEnd(convertDpToPx(5.3));
            newAvatar.setLayoutParams(layoutParams);
            newAvatar.setImageResource(getImageResource(channels.get(i)));

            if(red){
                red = false;
                orange = true;
            }else if(orange){
                orange = false;
                blue = true;
                newAvatar.setBorderColor(getOrangeColor());
            }else if(blue){
                blue = false;
                green = true;
                newAvatar.setBorderColor(getBlueColor());
            }else if(green){
                green = false;
                orange = true;
                newAvatar.setBorderColor(getGreenColor());
            }
            channelsView.addView(newAvatar);
        }
    }

    private int convertDpToPx(double dp){
        final float scale = fragment.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private int getImageResource(Channel channel){
        if(channel.image == null)return android.R.color.transparent;
        switch (channel.image){
            case "primary":
                return R.mipmap.primary_channel_thumb;
            case  "secondary":
                return R.mipmap.secondary_channel_thumb;
            case "tertiary":
                return R.mipmap.tertiary_channel_thumb;
            case "quaternary":
                return R.mipmap.quaternary_channel_thumb;
            case "quinary":
                return R.mipmap.quinary_channel_thumb;
            default:
                return android.R.color.transparent;
        }
    }

    private int getOrangeColor(){
        return fragment.getResources().getColor(R.color.militarOrange);
    }

    private int getBlueColor(){
        return fragment.getResources().getColor(R.color.militarBlue);
    }

    private int getGreenColor() {
        return fragment.getResources().getColor(R.color.militarGreen);
    }

    private void addRemainingChannelsText(List<Channel> channels, LinearLayout channelsView){
        int remainingChannels = channels.size() - 6;
        if(remainingChannels > 0){
            TextView remainingChannelsView = (TextView) LayoutInflater.from(fragment.getContext()).inflate(R.layout.remaining_channels_number, null);
            remainingChannelsView.setLayoutParams(new LinearLayout.LayoutParams(convertDpToPx(26.8), ViewGroup.LayoutParams.MATCH_PARENT));
            remainingChannelsView.setText(String.format("+%s", remainingChannels));
            channelsView.addView(remainingChannelsView);
        }
    }

    @Override
    public int getItemCount() {
        return missions.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {

        private View root;
        private TextView missionName;
        private TextView channelsNumber;
        private ImageView rightArrow;
        private LinearLayout channels;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            this.root = itemView;
            missionName = itemView.findViewById(R.id.mission_name_text);
            channelsNumber = itemView.findViewById(R.id.mission_channels_number);
            rightArrow = itemView.findViewById(R.id.right_arrow);
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