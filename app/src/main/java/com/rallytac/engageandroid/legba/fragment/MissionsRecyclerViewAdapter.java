package com.rallytac.engageandroid.legba.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.rallytac.engageandroid.R;
import com.rallytac.engageandroid.legba.data.DataManager;
import com.rallytac.engageandroid.legba.data.dto.Channel;
import com.rallytac.engageandroid.legba.data.dto.Mission;
import com.rallytac.engageandroid.legba.viewmodel.MissionsListViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.rallytac.engageandroid.legba.util.DimUtils.convertDpToPx;
import static com.rallytac.engageandroid.legba.util.RUtils.getImageResource;

public class MissionsRecyclerViewAdapter extends ListAdapter<Mission, MissionsRecyclerViewAdapter.ItemViewHolder> {

    private List<Mission> missions = new ArrayList();
    private Fragment fragment;
    private MissionsListViewModel vm;

    public void setMissions(List<Mission> missions) {
        this.missions = missions;
        notifyDataSetChanged();
    }

    public MissionsRecyclerViewAdapter(@NonNull DiffUtil.ItemCallback<Mission> diffCallback, Fragment fragment, MissionsListViewModel vm) {
        super(diffCallback);
        this.fragment = fragment;
        this.vm = vm;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.mission_item, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Mission currentMission = missions.get(position);
        List<Channel> audioChannels = currentMission.getChannels()
                .stream()
                .filter(channel -> channel.getEngageType() == Channel.EngageType.AUDIO)
                .collect(Collectors.toList());

        holder.missionLayout.setOnClickListener(view -> {
            NavHostFragment.findNavController(fragment)
                .navigate(MissionsListFragmentDirections.actionMissionsFragmentToMissionFragment(currentMission));
            DataManager.getInstance().switchToMissionOnEngageEngine(currentMission);
        });

        holder.trashLayout.setOnClickListener(view -> {
            missions.remove(position);
            vm.deleteMission(currentMission);
            notifyDataSetChanged();
        });

        holder.settingsLayout.setOnClickListener(view -> {
            NavHostFragment.findNavController(fragment)
                    .navigate(MissionsListFragmentDirections.actionMissionsFragmentToMissionEditActivity(currentMission));
        });


        holder.missionName.setText(currentMission.getName());
        holder.channelsNumber.setText(String.format("%s Channels", audioChannels.size()));
        holder.channels.removeAllViews();

        addChannelsToView(audioChannels, holder.channels);
        addRemainingChannelsText(audioChannels, holder.channels);
    }

    private void addChannelsToView(List<Channel> channels, LinearLayout channelsView) {
        boolean tomato = true;
        boolean orange = false;
        boolean blue = false;
        boolean white = false;

        for (int i = 0; (i < 6) && (i < channels.size()); i++) {
            RoundedImageView newAvatar = (RoundedImageView) LayoutInflater.from(fragment.getContext()).inflate(R.layout.channel_avatar, null);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(convertDpToPx(fragment, 26.4), ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.setMarginEnd(convertDpToPx(fragment, 5.3));
            newAvatar.setLayoutParams(layoutParams);
            newAvatar.setImageResource(getImageResource(channels.get(i).getImage()));

            if (tomato) {
                tomato = false;
                orange = true;
            } else if (orange) {
                orange = false;
                blue = true;
                newAvatar.setBorderColor(getOrangeColor());
            } else if (blue) {
                blue = false;
                white = true;
                newAvatar.setBorderColor(getBlueColor());
            } else if (white) {
                white = false;
                orange = true;
                newAvatar.setBorderColor(getWhiteColor());
            }
            channelsView.addView(newAvatar);
        }
    }

    private int getOrangeColor() {
        return fragment.getResources().getColor(R.color.orange);
    }

    private int getBlueColor() {
        return fragment.getResources().getColor(R.color.waterBlue);
    }

    private int getWhiteColor() {
        return fragment.getResources().getColor(R.color.white);
    }

    private void addRemainingChannelsText(List<Channel> channels, LinearLayout channelsView) {
        int remainingChannels = channels.size() - 6;
        if (remainingChannels > 0) {
            TextView remainingChannelsView = (TextView) LayoutInflater.from(fragment.getContext()).inflate(R.layout.remaining_channels_number, null);
            remainingChannelsView.setLayoutParams(new LinearLayout.LayoutParams(convertDpToPx(fragment, 26.8), ViewGroup.LayoutParams.MATCH_PARENT));
            remainingChannelsView.setText(String.format("+%s", remainingChannels));
            channelsView.addView(remainingChannelsView);
        }
    }

    @Override
    public int getItemCount() {
        return missions.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {

        private View missionLayout;
        private TextView missionName;
        private TextView channelsNumber;
        private LinearLayout channels;
        private View trashLayout;
        private View settingsLayout;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            missionLayout = itemView.findViewById(R.id.mission_layout);
            missionName = itemView.findViewById(R.id.mission_name_text);
            channelsNumber = itemView.findViewById(R.id.mission_channels_number);
            channels = itemView.findViewById(R.id.channels_list_view);
            trashLayout = itemView.findViewById(R.id.trash_layout);
            settingsLayout = itemView.findViewById(R.id.settings_layout);
        }
    }

    static class AdapterDiffCallback extends DiffUtil.ItemCallback<Mission> {

        @Override
        public boolean areItemsTheSame(@NonNull Mission oldItem, @NonNull Mission newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Mission oldItem, @NonNull Mission newItem) {
            return oldItem.toString().equals(newItem.toString());
        }
    }
}