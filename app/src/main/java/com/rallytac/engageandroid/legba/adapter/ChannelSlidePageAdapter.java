package com.rallytac.engageandroid.legba.adapter;

import android.content.Intent;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.rallytac.engageandroid.Globals;
import com.rallytac.engageandroid.R;
import com.rallytac.engageandroid.legba.data.DataManager;
import com.rallytac.engageandroid.legba.data.dto.Channel;
import com.rallytac.engageandroid.legba.data.dto.ChannelGroup;
import com.rallytac.engageandroid.legba.engage.RxListener;
import com.rallytac.engageandroid.legba.fragment.MissionFragment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.rallytac.engageandroid.legba.util.RUtils.getImageResource;

public class ChannelSlidePageAdapter extends RecyclerView.Adapter<ChannelSlidePageAdapter.GenericViewHolder> {

    private MissionFragment fragment;
    private List<ChannelGroup> channelsGroup;

    private final int RESUME_CHANNELS_ITEM = 0;
    private final int FULL_CHANNELS_ITEM = 1;
    private final int ADD_CHANNEL_ITEM = 2;

    public void setChannelsGroup(List<ChannelGroup> channelsGroup) {
        this.channelsGroup = channelsGroup;
        notifyDataSetChanged();
    }

    public ChannelSlidePageAdapter(MissionFragment fragment, List<ChannelGroup> channelsGroup) {
        this.fragment = fragment;
        this.channelsGroup = channelsGroup;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < channelsGroup.size()) {
            if (channelsGroup.get(position).getChannels().size() < 4) {
                return RESUME_CHANNELS_ITEM;
            } else {
                return FULL_CHANNELS_ITEM;
            }
        } else {
            return ADD_CHANNEL_ITEM;
        }
    }

    @NonNull
    @Override
    public GenericViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == RESUME_CHANNELS_ITEM) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.channels_resume_item, parent, false);
            ChannelResumeViewHolder channelResumeViewHolder = new ChannelResumeViewHolder(itemView);
            Globals.rxListeners.add(channelResumeViewHolder);
            return channelResumeViewHolder;
        } else if (viewType == FULL_CHANNELS_ITEM) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.channels_general_full_item, parent, false);
            ChannelBigListViewHolder channelBigListViewHolder = new ChannelBigListViewHolder(itemView);
            Globals.rxListeners.add(channelBigListViewHolder);
            return channelBigListViewHolder;
        } else {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.add_channel_item, parent, false);
            return new AddChannelViewHolder(itemView, fragment);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull GenericViewHolder holder, int position) {
        if (holder instanceof ChannelResumeViewHolder) {
            ChannelResumeViewHolder channelResumeHolder = (ChannelResumeViewHolder) holder;
            channelResumeHolder.setChannels(channelsGroup.get(position).getChannels());
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) channelResumeHolder.primaryChannel.getLayoutParams();
            int currentOrientation = fragment.getResources().getConfiguration().orientation;

            ChannelGroup currentChannelGroup = channelsGroup.get(position);
            int currentChannelGroupSize = currentChannelGroup.getChannels().size();
            if (currentChannelGroupSize < 1) {
                channelResumeHolder.primaryChannel.setVisibility(View.GONE);
                channelResumeHolder.priorityChannel1.setVisibility(View.GONE);
                channelResumeHolder.priorityChannel2.setVisibility(View.GONE);
                return;
            }

            Channel firstChannel = currentChannelGroup.getChannels().get(0);

            channelResumeHolder.primaryChannel.setVisibility(View.VISIBLE);
            channelResumeHolder.primaryChannelImage.setImageResource(getImageResource(firstChannel.getImage()));
            channelResumeHolder.primaryChannelName.setText(firstChannel.getName());
            channelResumeHolder.primaryChannelDescription.setText(getTypeString(firstChannel.getType()));
            toggleSpeakerIcon(firstChannel.isSpeakerOn(), channelResumeHolder.primaryChannelSpeaker);
            channelResumeHolder.primaryChannelSpeaker.setOnClickListener(view -> {
                firstChannel.setSpeakerOn(!firstChannel.isSpeakerOn()); ;
                DataManager.getInstance(fragment.getContext()).toggleMute(firstChannel.getId(), firstChannel.isSpeakerOn());
                      notifyDataSetChanged();
            });

            if (currentChannelGroupSize < 2) {
                lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                lp.bottomMargin = 0;
                lp.leftMargin = 0;
                channelResumeHolder.primaryChannel.setLayoutParams(lp);
                channelResumeHolder.priorityChannel1.setVisibility(View.GONE);
                channelResumeHolder.priorityChannel2.setVisibility(View.GONE);

                setFirstChannelViewState(firstChannel, channelResumeHolder, currentChannelGroup.getChannels());
                return;
            } else {
                if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) { //Note: Do not use DimUtils.converDpToPx or will bug the current page
                    lp.width = (int) fragment.getResources().getDimension(R.dimen.primary_channel_width_landscape);
                } else {
                    lp.height = (int) fragment.getResources().getDimension(R.dimen.primary_channel_height_portrait);
                    lp.bottomMargin = (int) fragment.getResources().getDimension(R.dimen.primary_channel_margin_bottom_portrait);
                }
                channelResumeHolder.primaryChannel.setLayoutParams(lp);
            }


            Channel secondChannel = currentChannelGroup.getChannels().get(1);

            channelResumeHolder.priorityChannel1.setVisibility(View.VISIBLE);
            channelResumeHolder.priorityChannel1Image.setImageResource(getImageResource(secondChannel.getImage()));
            channelResumeHolder.priorityChannel1Name.setText(secondChannel.getName());
            channelResumeHolder.priorityChannel1Description.setText(getTypeString(secondChannel.getType()));
            toggleSpeakerIcon(secondChannel.isSpeakerOn(), channelResumeHolder.priorityChannel1Speaker);
            channelResumeHolder.priorityChannel1Speaker.setOnClickListener(view -> {
                secondChannel.setSpeakerOn(!secondChannel.isSpeakerOn());
                DataManager.getInstance(fragment.getContext()).toggleMute(secondChannel.getId(), secondChannel.isSpeakerOn());

                notifyDataSetChanged();
            });

            channelResumeHolder.priorityChannel2.setVisibility(View.GONE);

            setSecondChannelViewState(secondChannel, channelResumeHolder, currentChannelGroup.getChannels());

            if (currentChannelGroupSize < 3) {
                channelResumeHolder.priorityChannel2.setVisibility(View.GONE);
                return;
            }

            Channel thirdChannel = currentChannelGroup.getChannels().get(2);

            channelResumeHolder.priorityChannel2.setVisibility(View.VISIBLE);
            channelResumeHolder.priorityChannel2Image.setImageResource(getImageResource(thirdChannel.getImage()));
            channelResumeHolder.priorityChannel2Name.setText(thirdChannel.getName());
            channelResumeHolder.priorityChannel2Description.setText(getTypeString(thirdChannel.getType()));
            toggleSpeakerIcon(thirdChannel.isSpeakerOn(), channelResumeHolder.priorityChannel2Speaker);

            channelResumeHolder.priorityChannel2Speaker.setOnClickListener(view -> {
                thirdChannel.setSpeakerOn(!thirdChannel.isSpeakerOn());
                DataManager.getInstance(fragment.getContext()).toggleMute(thirdChannel.getId(), thirdChannel.isSpeakerOn());
                notifyDataSetChanged();
            });

            setThirdChannelViewState(thirdChannel, channelResumeHolder, currentChannelGroup.getChannels());

        } else if (holder instanceof ChannelBigListViewHolder) {
            ChannelBigListViewHolder channelBigListViewHolder = (ChannelBigListViewHolder) holder;
            ChannelGroup currentChannelGroup = channelsGroup.get(position);
            channelBigListViewHolder.setChannels(currentChannelGroup.getChannels());
        }
    }

    private void setFirstChannelViewState(Channel firstChannel, ChannelResumeViewHolder holder, List<Channel> channels) {
        if(firstChannel.isOnRx){
            RxListener holder1 = holder;
            holder1.onRx(firstChannel.getId(), firstChannel.getRxAlias(), "");
            //holder.showIncomingMessage(currentChannel.rxAlias);
        }else{
            boolean brotherViewIsOnRx = channels.stream().anyMatch(channel -> channel.isOnRx);
            if(brotherViewIsOnRx){
                holder.fadeOutFirstChannel();
            }
        }
    }

    private void setSecondChannelViewState(Channel secondChannel, ChannelResumeViewHolder holder, List<Channel> channels) {
        if(secondChannel.isOnRx){
            RxListener holder1 = holder;
            holder1.onRx(secondChannel.getId(), secondChannel.getRxAlias(), "");
            //holder.showIncomingMessage(currentChannel.rxAlias);
        }else{
            boolean brotherViewIsOnRx = channels.stream().anyMatch(channel -> channel.isOnRx);
            if(brotherViewIsOnRx){
                holder.fadeOutSecondChannel();
            }
        }
    }

    private void setThirdChannelViewState(Channel currentChannel, ChannelResumeViewHolder holder, List<Channel> channels) {
        if(currentChannel.isOnRx){
            RxListener holder1 = holder;
            holder1.onRx(currentChannel.getId(), currentChannel.getRxAlias(), "");
            //holder.showIncomingMessage(currentChannel.rxAlias);
        }else{
            boolean brotherViewIsOnRx = channels.stream().anyMatch(channel -> channel.isOnRx);
            if(brotherViewIsOnRx){
                holder.fadeOutThirdChannel();
            }
        }
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

    private void toggleSpeakerIcon(boolean isOn, ImageView button) {
        if (isOn) {
            button.setImageResource(R.drawable.ic_speaker);
        } else {
            button.setImageResource(R.drawable.ic_speaker_off);
        }
    }

    @Override
    public int getItemCount() {
        return channelsGroup.size() + 1;
    }

    class GenericViewHolder extends RecyclerView.ViewHolder {

        public GenericViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    class ChannelResumeViewHolder extends GenericViewHolder implements RxListener {
        private final static float LOW_OPACITY = 0.1f;
        private final static float FULL_OPACITY = 1f;

        private final static String PRIMARY_CHANNEL = "Primary Channel";
        private final static String PRIORITY_CHANNEL_1 = "Priority Channel";
        private final static String PRIORITY_CHANNEL_2 = "Priority Channel";

        private View primaryChannel;
        private RoundedImageView primaryChannelImage;
        private TextView primaryChannelName;
        private TextView primaryChannelDescription;
        private ImageView primaryChannelSpeaker;
        private ImageView primaryChannelRxImage;

        private View incomingMessageLayout;
        private TextView incomingMessageName;

        private TextView lastMessageText;
        private TextView lastMessageTime;
        private TextView lastMessageAlias;
        private TextView lastMessageDisplayName;

        private View priorityChannel1;
        private RoundedImageView priorityChannel1Image;
        private TextView priorityChannel1Name;
        private TextView priorityChannel1Description;
        private ImageView priorityChannel1Speaker;
        private ImageView priorityChannel1RxImage;

        private View priorityChannel2;
        private RoundedImageView priorityChannel2Image;
        private TextView priorityChannel2Name;
        private TextView priorityChannel2Description;
        private ImageView priorityChannel2Speaker;
        private ImageView priorityChannel2RxImage;

        private List<Channel> channels;

        public ChannelResumeViewHolder(@NonNull View itemView) {
            super(itemView);

            primaryChannel = itemView.findViewById(R.id.primary_channel_layout);
            primaryChannelImage = itemView.findViewById(R.id.primary_channel_image);
            primaryChannelName = itemView.findViewById(R.id.primary_channel_name_text);
            primaryChannelDescription = itemView.findViewById(R.id.primary_channel_description);
            primaryChannelSpeaker = itemView.findViewById(R.id.primary_channel_speaker);
            primaryChannelRxImage = itemView.findViewById(R.id.rx_image_primary_channel);

            incomingMessageLayout = itemView.findViewById(R.id.incoming_message_layout);
            incomingMessageName = itemView.findViewById(R.id.incoming_message_name);

            lastMessageText = itemView.findViewById(R.id.last_message_text);
            lastMessageTime = itemView.findViewById(R.id.last_message_time);
            lastMessageAlias = itemView.findViewById(R.id.last_message_alias);
            lastMessageDisplayName = itemView.findViewById(R.id.last_message_displayName);

            priorityChannel1 = itemView.findViewById(R.id.priority_channel_1_layout);
            priorityChannel1Name = itemView.findViewById(R.id.priority_channel_1_name_text);
            priorityChannel1Description = itemView.findViewById(R.id.priority_channel_1_description);
            priorityChannel1Image = itemView.findViewById(R.id.priority_channel_1_image);
            priorityChannel1Speaker = itemView.findViewById(R.id.priority_channel_1_speaker);
            priorityChannel1RxImage = itemView.findViewById(R.id.rx_image_priority_channel_1);

            priorityChannel2 = itemView.findViewById(R.id.priority_channel_2_layout);
            priorityChannel2Name = itemView.findViewById(R.id.priority_channel_2_name_text);
            priorityChannel2Description = itemView.findViewById(R.id.priority_channel_2_description);
            priorityChannel2Image = itemView.findViewById(R.id.priority_channel_2_image);
            priorityChannel2Speaker = itemView.findViewById(R.id.priority_channel_2_speaker);
            priorityChannel2RxImage = itemView.findViewById(R.id.rx_image_priority_channel_2);
        }

        public void setChannels(List<Channel> channels) {
            this.channels = channels;
        }

        @Override
        public void onRx(String id, String alias, String displayName) {
            String formattedAlias = alias == null ? "UNKNOWN" : alias;
            String formattedDisplayName = displayName == null ? "Unknown user" : displayName;

            this.channels
                    .stream()
                    .filter(channel -> channel.getId().equals(id))
                    .findFirst()
                    .ifPresent(channel -> {
                        if (this.channels.size() == 1) {
                            channel.isOnRx = true;
                            showIncomingMessageLayout(formattedAlias, formattedDisplayName);
                        } else {
                            showIncomingMessageImage(id, formattedAlias);
                            fadeOutFreeChannels();
                        }
                    });
        }

        private void showIncomingMessageLayout(String alias, String displayName) {
            incomingMessageLayout.setVisibility(View.VISIBLE);
            fadeOutFirstChannel();
            updateBackground();

            String time = DateTimeFormatter
                    .ofPattern("hh:mm a")
                    .format(LocalDateTime.now());

            ((TextView) incomingMessageLayout.findViewById(R.id.incoming_message_name)).setText(alias);

            updateLastMessage(time, alias, displayName);
        }

        private void fadeOutFirstChannel() {
            //Channel info
            primaryChannelImage.setAlpha(LOW_OPACITY);
            primaryChannelName.setAlpha(LOW_OPACITY);
            primaryChannelDescription.setAlpha(LOW_OPACITY);
            //Last message
            lastMessageTime.setAlpha(LOW_OPACITY);
            lastMessageAlias.setAlpha(LOW_OPACITY);
            lastMessageDisplayName.setAlpha(LOW_OPACITY);
            lastMessageText.setAlpha(LOW_OPACITY);
        }

        private void updateBackground() {
            primaryChannel.setBackground(ContextCompat.getDrawable(fragment.getContext(),
                    R.drawable.primary_channel_item_fade_shape));
        }

        private void updateLastMessage(String timeText, String aliasText, String displayNameText) {
            lastMessageTime.setText(timeText);
            lastMessageAlias.setText(aliasText);
            lastMessageDisplayName.setText(displayNameText);
            lastMessageText.setVisibility(View.VISIBLE);
        }

        private void showIncomingMessageImage(String channelId, String alias) {

            Channel firstChannel = channels.get(0);

            if (firstChannel.getId().equals(channelId)) {
                firstChannel.isOnRx = true;
                primaryChannelDescription.setText(alias);
                primaryChannelRxImage.setVisibility(View.VISIBLE);
                primaryChannel.setBackground(ContextCompat.getDrawable(fragment.getContext(), R.drawable.primary_channel_item_fade_shape));

                /*primaryChannelImage.setAlpha(FULL_OPACITY);
                primaryChannelName.setAlpha(FULL_OPACITY);
                primaryChannelDescription.setAlpha(FULL_OPACITY);*/
            }

            Channel secondChannel = channels.get(1);

            if (secondChannel.getId().equals(channelId)) {
                secondChannel.isOnRx = true;
                priorityChannel1Description.setText(alias);
                priorityChannel1RxImage.setVisibility(View.VISIBLE);
                priorityChannel1.setBackground(ContextCompat.getDrawable(fragment.getContext(), R.drawable.prioritary1_channel_item_fade_shape));

                //This 3 lines prevent the following bug:
                //If there are multiple RX, the first channel will update correctly but the second and third will mix a fadeIn and fadeout state
                priorityChannel1Image.setAlpha(FULL_OPACITY);
                priorityChannel1Name.setAlpha(FULL_OPACITY);
                priorityChannel1Description.setAlpha(FULL_OPACITY);
            }

            if (channels.size() == 3 && channels.get(2).getId().equals(channelId)) {
                channels.get(2).isOnRx = true;
                priorityChannel2Description.setText(alias);
                priorityChannel2RxImage.setVisibility(View.VISIBLE);
                priorityChannel2.setBackground(ContextCompat.getDrawable(fragment.getContext(), R.drawable.prioritary2_channel_item_fade_shape));

                //This 3 lines prevent the following bug:
                //If there are multiple RX, the first channel will update correctly but the second and third will mix a fadeIn and fadeout state
                priorityChannel2Image.setAlpha(FULL_OPACITY);
                priorityChannel2Name.setAlpha(FULL_OPACITY);
                priorityChannel2Description.setAlpha(FULL_OPACITY);
            }
        }

        private void fadeOutFreeChannels() {

            if (!this.channels.get(0).isOnRx) {
                fadeOutFirstChannel();
            }

            if (this.channels.size() > 1 && !this.channels.get(1).isOnRx) {
                fadeOutSecondChannel();
            }

            if (this.channels.size() > 2 && !this.channels.get(2).isOnRx) {
                fadeOutThirdChannel();
            }
        }

        private void fadeOutSecondChannel() {
            priorityChannel1Image.setAlpha(LOW_OPACITY);
            priorityChannel1Name.setAlpha(LOW_OPACITY);
            priorityChannel1Description.setAlpha(LOW_OPACITY);
        }

        private void fadeOutThirdChannel() {
            priorityChannel2Image.setAlpha(LOW_OPACITY);
            priorityChannel2Name.setAlpha(LOW_OPACITY);
            priorityChannel2Description.setAlpha(LOW_OPACITY);
        }

        @Override
        public void stopRx(String id, String eventExtraJson) {

            this.channels
                    .stream()
                    .filter(channel -> channel.getId().equals(id))
                    .findFirst()
                    .ifPresent(channel -> {
                        if (this.channels.size() == 1) {
                            channel.isOnRx = false;
                            hideIncomingMessageLayout();
                        } else {
                            hideIncomingMessageImage(id);
                            fadeInFreeChannels();
                        }
                    });
        }

        private void hideIncomingMessageLayout() {
            fadeInFirstchannel();
            incomingMessageLayout.setVisibility(View.GONE);
            primaryChannel.setBackground(ContextCompat.getDrawable(fragment.getContext(), R.drawable.channel_resume_item_box_shape));
        }

        private void fadeInFirstchannel() {
            //Channel info
            primaryChannelImage.setAlpha(FULL_OPACITY);
            primaryChannelName.setAlpha(FULL_OPACITY);
            primaryChannelDescription.setAlpha(FULL_OPACITY);
            //Last message
            lastMessageTime.setAlpha(FULL_OPACITY);
            lastMessageAlias.setAlpha(FULL_OPACITY);
            lastMessageDisplayName.setAlpha(FULL_OPACITY);
            lastMessageText.setAlpha(FULL_OPACITY);
        }

        private void hideIncomingMessageImage(String channelId) {
            Channel firstChannel = channels.get(0);

            if (firstChannel.getId().equals(channelId)) {
                firstChannel.isOnRx = false;
                primaryChannelDescription.setText(PRIMARY_CHANNEL);
                primaryChannel.setBackground(ContextCompat.getDrawable(fragment.getContext(), R.drawable.channel_item_shape));
                primaryChannelRxImage.setVisibility(View.GONE);
            }

            Channel secondChannel = channels.get(1);

            if (secondChannel.getId().equals(channelId)) {
                secondChannel.isOnRx = false;
                priorityChannel1Description.setText(PRIORITY_CHANNEL_1);
                priorityChannel1RxImage.setVisibility(View.GONE);
                priorityChannel1.setBackground(ContextCompat.getDrawable(fragment.getContext(), R.drawable.channel_item_shape));
            }

            if (channels.size() == 3 && channels.get(2).getId().equals(channelId)) {
                channels.get(2).isOnRx = false;
                priorityChannel2.setBackground(ContextCompat.getDrawable(fragment.getContext(), R.drawable.channel_item_shape));
                priorityChannel2Description.setText(PRIORITY_CHANNEL_2);
                priorityChannel2RxImage.setVisibility(View.GONE);
            }
        }

        private void fadeInFreeChannels() {
            if (!this.channels.get(0).isOnRx) {
                primaryChannelImage.setAlpha(FULL_OPACITY);
                primaryChannelName.setAlpha(FULL_OPACITY);
                primaryChannelDescription.setAlpha(FULL_OPACITY);
            }

            if (this.channels.size() > 1 && !this.channels.get(1).isOnRx) {
                priorityChannel1Image.setAlpha(FULL_OPACITY);
                priorityChannel1Name.setAlpha(FULL_OPACITY);
                priorityChannel1Description.setAlpha(FULL_OPACITY);
            }

            if (this.channels.size() > 2 && !this.channels.get(2).isOnRx) {
                priorityChannel2Image.setAlpha(FULL_OPACITY);
                priorityChannel2Name.setAlpha(FULL_OPACITY);
                priorityChannel2Description.setAlpha(FULL_OPACITY);
            }
        }
    }

    public class ChannelBigListViewHolder extends GenericViewHolder implements RxListener {

        private ChannelBigListAdapter channelBigListAdapter;
        private RecyclerView channelsRecyclerView;
        private List<Channel> channels;

        public ChannelBigListViewHolder(@NonNull View itemView) {
            super(itemView);
            channelBigListAdapter = new ChannelBigListAdapter(fragment);
            channelsRecyclerView = itemView.findViewById(R.id.rv_channels);
            channelsRecyclerView.setHasFixedSize(true);
            channelsRecyclerView.setAdapter(channelBigListAdapter);
        }

        public void setChannels(List<Channel> channels) {
            this.channels = channels;
            this.channelBigListAdapter.setChannels(channels);
            this.channelBigListAdapter.notifyDataSetChanged();
        }

        @Override
        public void onRx(String id, String alias, String displayName) {
            String formattedAlias = alias == null ? "UNKNOWN" : alias;

            this.channels
                    .stream()
                    .filter(channel -> channel.getId().equals(id))
                    .findFirst()
                    .ifPresent(channel -> {
                        channel.isOnRx = true;
                        channel.rxAlias = formattedAlias;
                        showIncomingMessage(id, formattedAlias);
                        fadeOutFreeChannels();
                        channelsRecyclerView.getAdapter().notifyDataSetChanged();
                    });
        }

        private void showIncomingMessage(String id, String alias) {
            for (int i = 0; i < channels.size(); i++) {
                ChannelBigListAdapter.ChannelViewHolder currentHolder =
                        (ChannelBigListAdapter.ChannelViewHolder) channelsRecyclerView.findViewHolderForAdapterPosition(i);
                if(currentHolder != null && currentHolder.channelId.equals(id)){
                    currentHolder.showIncomingMessage(alias);
                }
            }
        }

        private void fadeOutFreeChannels() {
            for (int i = 0; i < channels.size(); i++) {
                ChannelBigListAdapter.ChannelViewHolder currentHolder =
                        (ChannelBigListAdapter.ChannelViewHolder) channelsRecyclerView.findViewHolderForAdapterPosition(i);
                if(currentHolder != null && !this.channels.get(i).isOnRx){
                    currentHolder.fadeOut();
                }
            }
        }

        @Override
        public void stopRx(String id, String eventExtraJson) {
            this.channels
                    .stream()
                    .filter(channel -> channel.getId().equals(id))
                    .findFirst()
                    .ifPresent(channel -> {
                        channel.isOnRx = false;
                        channel.rxAlias = "";
                        hideIncomingMessage(id);
                        fadeInFreeChannels();
                        channelsRecyclerView.getAdapter().notifyDataSetChanged();
                    });
        }

        private void hideIncomingMessage(String id) {
            for (int i = 0; i < channels.size(); i++) {
                ChannelBigListAdapter.ChannelViewHolder currentHolder =
                        (ChannelBigListAdapter.ChannelViewHolder) channelsRecyclerView.findViewHolderForAdapterPosition(i);
                if(currentHolder != null && currentHolder.channelId.equals(id)){
                    currentHolder.hideIncomingMessage();
                }
            }
        }

        private void fadeInFreeChannels() {
            for (int i = 0; i < channels.size(); i++) {
                ChannelBigListAdapter.ChannelViewHolder currentHolder =
                        (ChannelBigListAdapter.ChannelViewHolder) channelsRecyclerView.findViewHolderForAdapterPosition(i);
                if(currentHolder != null && !this.channels.get(i).isOnRx){
                    currentHolder.fadeIn();
                }
            }
        }
    }

    public class AddChannelViewHolder extends GenericViewHolder {
        private MissionFragment fragment;
        private ImageView imgCreateChannelGroup;

        public AddChannelViewHolder(@NonNull View itemView, Fragment fragment) {
            super(itemView);
            this.fragment = (MissionFragment) fragment;

            imgCreateChannelGroup = itemView.findViewById(R.id.img_create_channel_group);
            imgCreateChannelGroup.setOnClickListener(view -> this.fragment.toggleCreateEditChannelsGroupLayoutvisibility());
        }
    }
}