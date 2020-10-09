package com.rallytac.engageandroid.legba.fragment;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.makeramen.roundedimageview.RoundedImageView;
import com.rallytac.engageandroid.Globals;
import com.rallytac.engageandroid.R;
import com.rallytac.engageandroid.legba.engage.RxListener;
import com.rallytac.engageandroid.legba.view.SwipeButton;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.rallytac.engageandroid.legba.HostActivity;
import com.rallytac.engageandroid.legba.data.dto.Channel;
import com.rallytac.engageandroid.legba.data.dto.Mission;
import com.rallytac.engageandroid.databinding.FragmentMissionBinding;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import timber.log.Timber;

import static com.rallytac.engageandroid.legba.util.DimUtils.convertDpToPx;
import static com.rallytac.engageandroid.legba.util.RUtils.getImageResource;


public class MissionFragment extends Fragment{

    private HostActivity activity;
    private FragmentMissionBinding binding;
    private Mission mission;
    private TextView fragmentDescriptionText;
    private ImageView[] dotIndicators;
    private boolean _pttRequested;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MissionFragmentArgs missionFragmentArgs = MissionFragmentArgs.fromBundle(requireArguments());
        mission = missionFragmentArgs.getMission();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = (HostActivity) requireActivity();

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            activity.findViewById(R.id.overlap_layout).setOnClickListener(view -> toggleSOSLayoutVisiblity());
            activity.binding.sosSwipeButton.setSosEmergencyListener(new SwipeButton.SOSEmergencyListener() {

                @Override
                public void onStart() {
                    activity.binding.eyesGlowAnimation.setVisibility(View.VISIBLE);
                    activity.binding.sosGlowAnimation.setVisibility(View.VISIBLE);
                }

                @Override
                public void onStop() {
                    activity.binding.eyesGlowAnimation.setVisibility(View.GONE);
                    activity.binding.sosGlowAnimation.setVisibility(View.VISIBLE);
                }
            });
        }

        updateToolbar();
        setHasOptionsMenu(true);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_mission, container, false);

        List<Channel> nonRadioChannels = mission.channels
                .stream()
                .filter(channel -> channel.type != Channel.ChannelType.RADIO)
                .collect(Collectors.toList());

        binding.missionViewPager.setAdapter(new ChannelSlidePageAdapter(this, nonRadioChannels));



        setupPTTOnMic();
        setupViewPagerOnPageChangeListener();
        setupViewPagerDotIndicator(nonRadioChannels);
        setUpSlidingUpPanelListener();
        setUpSlidingUpChannels();
        updateDots(0);

        return binding.getRoot();
    }

    private void updateToolbar() {
        requireActivity().findViewById(R.id.logo_image).setVisibility(View.VISIBLE);
        requireActivity().findViewById(R.id.toolbar_title_text).setVisibility(View.VISIBLE);
        ((TextView) requireActivity().findViewById(R.id.toolbar_title_text)).setText(mission.name);
        requireActivity().findViewById(R.id.fragment_description).setVisibility(View.VISIBLE);
        fragmentDescriptionText = requireActivity().findViewById(R.id.fragment_description);
        fragmentDescriptionText.setTextColor(this.getResources().getColor(R.color.paleRed));
        Objects.requireNonNull(((HostActivity) requireActivity()).getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_round_keyboard_arrow_left_24);
    }

    private void setupPTTOnMic(){
        binding.icMicCard.setOnTouchListener((v, event) -> {

            if (event.getAction() == MotionEvent.ACTION_DOWN)
            {
                binding.txImage.setVisibility(View.VISIBLE);
                Log.w("sending", "#SB#: onTouch ACTION_DOWN - startTx");//NON-NLS
                _pttRequested = true;
                Globals.getEngageApplication().startTx(0, 0);
            }
            else if (event.getAction() == MotionEvent.ACTION_UP)
            {
                binding.txImage.setVisibility(View.INVISIBLE);
                Log.w("Stop sending", "#SB#: onTouch ACTION_UP - endTx");//NON-NLS
                _pttRequested = false;
                Globals.getEngageApplication().endTx();

            }

            return true;
        });
    }

    private void setupViewPagerOnPageChangeListener() {
        binding.missionViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                updateDots(position);

                String ordinalPosition;
                switch (position) {
                    case 0:
                        ordinalPosition = "Alpha";
                        break;
                    case 1:
                        ordinalPosition = "Delta";
                        break;
                    case 2:
                        ordinalPosition = "Fire";
                        break;
                    case 3:
                        ordinalPosition = "All Ground";
                        break;
                    default:
                        ordinalPosition = "";
                        break;
                }

                fragmentDescriptionText.setText(ordinalPosition);
            }
        });
    }

    private void setupViewPagerDotIndicator(List<Channel> channels) {
        binding.tabLayout.removeAllViews();
        int dotNumber = channels.size() > 1 ? channels.size() + 1 + 1: channels.size() + 1;
        dotIndicators = new ImageView[dotNumber];
        for (int i = 0; i < dotNumber; i++) {
            dotIndicators[i] = new ImageView(getContext());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(convertDpToPx(this, 4.5), convertDpToPx(this, 4.5));
            layoutParams.setMarginEnd(convertDpToPx(this, 6.3));
            dotIndicators[i].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.default_dot));
            dotIndicators[i].setLayoutParams(layoutParams);
            binding.tabLayout.addView(dotIndicators[i]);
        }
    }

    private void setUpSlidingUpPanelListener() {
        binding.toggleRadioChannelButton.setOnClickListener(view -> {
            if (binding.radioChannelsSlidingupLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                binding.radioChannelsSlidingupLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            } else if (binding.radioChannelsSlidingupLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                binding.radioChannelsSlidingupLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });
        binding.radioChannelsSlidingupLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                ImageButton toggleRadioChannelButton = binding.toggleRadioChannelButton;
                if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    toggleRadioChannelButton.animate().rotation(180F).setInterpolator(new AccelerateDecelerateInterpolator());
                } else if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    toggleRadioChannelButton.animate().rotation(0F).setInterpolator(new AccelerateDecelerateInterpolator());
                }
            }
        });
    }

    private void setUpSlidingUpChannels() {
        List<Channel> radioChannels = mission.channels
                .stream()
                .filter(channel -> channel.type != null)
                //.filter(channel -> channel.type == Channel.ChannelType.RADIO)
                .collect(Collectors.toList());

        RadioChannelsRecyclerViewAdapter adapter = new RadioChannelsRecyclerViewAdapter(new RadioChannelsRecyclerViewAdapter.AdapterDiffCallback(), this);
        binding.radioChannelsRecycler.setHasFixedSize(true);
        binding.radioChannelsRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        binding.radioChannelsRecycler.setAdapter(adapter);
        adapter.setRadioChannels(radioChannels);
    }

    private void updateDots(int position) {
        for (int i = 0; i < dotIndicators.length; i++) {
            double dp;
            int drawableId;

            if (i == position) {
                dp = 5.5;
                drawableId = R.drawable.selected_dot;
            } else {
                dp = 4.5;
                drawableId = R.drawable.default_dot;
            }

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(convertDpToPx(this, dp), convertDpToPx(this, dp));
            layoutParams.setMarginEnd(convertDpToPx(this, 6.3));
            dotIndicators[i].setImageDrawable(ContextCompat.getDrawable(getContext(), drawableId));
            dotIndicators[i].setLayoutParams(layoutParams);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.mission_fragment_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        MenuItem item = menu.findItem(R.id.sos_action);
        View root = item.getActionView();
        root.setOnClickListener(view -> onOptionsItemSelected(item));

/*        root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Timber.i("onTouch ");
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    Timber.i("ACTION_DOWN");
                    toggleSOSLayoutVisiblity();
                    return true;
                }else if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    Timber.i("ACTION_UP");
                    toggleSOSLayoutVisiblity();
                    return true;
                }
                return false;
            }

        });*/

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.sos_action){
            Timber.i("SOS PRESSED");
            toggleSOSLayoutVisiblity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleSOSLayoutVisiblity(){
        View sosLayout = activity.findViewById(R.id.overlap_layout);
        if (sosLayout == null) return;
        if(sosLayout.getVisibility() == View.GONE){
            sosLayout.setVisibility(View.VISIBLE);
        }else{
            sosLayout.setVisibility(View.GONE);
        }
    }



    private class ChannelSlidePageAdapter extends RecyclerView.Adapter<ChannelSlidePageAdapter.GenericViewHolder> {

        private Fragment fragment;
        private List<Channel> channels;
        private ImageView rxImage;

        private int CHANNEL_ITEM = 0;
        private int RESUME_CHANNELS_ITEM = 1;
        private int ADD_CHANNEL_ITEM = 2;

        private int priorityIndicator = 1;
        private boolean paleRed = true;
        private boolean waterBlue = false;
        private boolean orange = false;

        private boolean isPrimarySpeakerOn = true;
        private boolean isPriority1SpekearOn = true;
        private boolean isPriority2SpeakerOn = true;

        public void setChannels(List<Channel> channels) {
            this.channels = channels;
            notifyDataSetChanged();
        }

        public ChannelSlidePageAdapter(Fragment fragment, List<Channel> channels) {
            this.fragment = fragment;
            this.channels = channels;
        }

        @Override
        public int getItemViewType(int position) {
            if (position < channels.size()) {
                return CHANNEL_ITEM;
            } else if (channels.size() > 1 && channels.size() == position){
                return RESUME_CHANNELS_ITEM;
            } else {
                return ADD_CHANNEL_ITEM;
            }
        }

        @NonNull
        @Override
        public GenericViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            if (viewType == CHANNEL_ITEM) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.channel_item, parent, false);
                ChannelViewHolder channelViewHolder = new ChannelViewHolder(itemView);
                Globals.actualListener = channelViewHolder;
                return channelViewHolder;
            } else if (viewType == RESUME_CHANNELS_ITEM) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.channels_resume_item, parent, false);
                return new ChannelResumeViewHolder(itemView);
            } else {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.add_channel_item, parent, false);
                return new AddChannelViewHolder(itemView);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull GenericViewHolder holder, int position) {
            if (holder instanceof ChannelViewHolder) {
                ChannelViewHolder channelHolder = (ChannelViewHolder) holder;

                Channel currentChannel = channels.get(position);
              /*  channelHolder.channelInfo.setOnClickListener(view -> NavHostFragment.findNavController(fragment)
                        .navigate(MissionFragmentDirections.actionMissionFragmentToChannelFragment(currentChannel)));*/
                channelHolder.channelImage.setImageResource(getImageResource(currentChannel.image));
                channelHolder.channelName.setText(currentChannel.name);
                channelHolder.channelType.setText(getTypeString(currentChannel.type));

                if (paleRed) {
                    paleRed = false;
                    waterBlue = true;
                } else if (waterBlue) {
                    waterBlue = false;
                    orange = true;

                    channelHolder.channelImage.setBorderColor(getWaterBlueColor());
                    channelHolder.channelType.setTextColor(getWaterBlueColor());
                    channelHolder.lastMessageTime.setTextColor(getWaterBlueColor());
                    channelHolder.incomingMessageLayout
                            .findViewById(R.id.incoming_message_speaker_layout)
                            .setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.incoming_message_speaker_blue_layout_shape, null));
                    ((ImageView)channelHolder.incomingMessageLayout
                            .findViewById(R.id.incoming_message_speaker))
                            .setImageResource(R.drawable.ic_blue_speaker);
                    ((TextView)channelHolder.incomingMessageLayout
                            .findViewById(R.id.incoming_message_speaking_text))
                            .setTextColor(getResources().getColor(R.color.waterBlue93, null));
                    ((ImageView)channelHolder.incomingMessageLayout
                            .findViewById(R.id.rx_image))
                            .setImageResource(R.drawable.ic_blue_tx);
                } else if (orange) {
                    orange = false;
                    paleRed = true;

                    channelHolder.channelImage.setBorderColor(getOrangeColor());
                    channelHolder.channelType.setTextColor(getOrangeColor());
                    channelHolder.lastMessageTime.setTextColor(getOrangeColor());
                    channelHolder.incomingMessageLayout
                            .findViewById(R.id.incoming_message_speaker_layout)
                            .setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.incoming_message_speaker_orange_layout_shape, null));
                    ((ImageView)channelHolder.incomingMessageLayout
                            .findViewById(R.id.incoming_message_speaker))
                            .setImageResource(R.drawable.ic_orange_speaker);
                    ((TextView)channelHolder.incomingMessageLayout
                            .findViewById(R.id.incoming_message_speaking_text))
                            .setTextColor(getResources().getColor(R.color.orange93, null));
                    ((ImageView)channelHolder.incomingMessageLayout
                            .findViewById(R.id.rx_image))
                            .setImageResource(R.drawable.ic_orange_tx);
                }

                switch (position){
                    case 0:
                        toggleSpeakerIcon(isPrimarySpeakerOn, channelHolder.speakerButton);
                        channelHolder.speakerButton.setOnClickListener(view -> {
                            isPrimarySpeakerOn = !isPrimarySpeakerOn;
                            toggleSpeakerIcon(isPrimarySpeakerOn, (ImageView) view);
                        });
                        return;
                    case 1:
                        toggleSpeakerIcon(isPriority1SpekearOn, channelHolder.speakerButton);
                        channelHolder.speakerButton.setOnClickListener(view -> {
                            isPriority1SpekearOn = !isPriority1SpekearOn;
                            toggleSpeakerIcon(isPriority1SpekearOn, (ImageView) view);
                        });
                        return;
                    case 2:
                        toggleSpeakerIcon(isPriority2SpeakerOn, channelHolder.speakerButton);
                        channelHolder.speakerButton.setOnClickListener(view -> {
                            isPriority2SpeakerOn = !isPriority2SpeakerOn;
                            toggleSpeakerIcon(isPriority2SpeakerOn, (ImageView) view);
                        });
                        return;
                    default:
                }

            } else if (holder instanceof ChannelResumeViewHolder) {
                ChannelResumeViewHolder channelResumeHolder = (ChannelResumeViewHolder) holder;

                channelResumeHolder.primaryChannel.setVisibility(View.VISIBLE);
                channelResumeHolder.primaryChannelImage.setImageResource(getImageResource(channels.get(0).image));
                channelResumeHolder.primaryChannelName.setText(channels.get(0).name);
                toggleSpeakerIcon(isPrimarySpeakerOn, channelResumeHolder.primaryChannelSpeaker);
                channelResumeHolder.primaryChannelSpeaker.setOnClickListener(view -> {
                    isPrimarySpeakerOn = !isPrimarySpeakerOn;
                    toggleSpeakerIcon(isPrimarySpeakerOn, (ImageView) view);
                });


                channelResumeHolder.priorityChannel1.setVisibility(View.VISIBLE);
                channelResumeHolder.priorityChannel1Image.setImageResource(getImageResource(channels.get(1).image));
                channelResumeHolder.priorityChannel1Name.setText(channels.get(1).name);
                toggleSpeakerIcon(isPriority1SpekearOn, channelResumeHolder.priorityChannel1Speaker);
                channelResumeHolder.priorityChannel1Speaker.setOnClickListener(view -> {
                    isPriority1SpekearOn = !isPriority1SpekearOn;
                    toggleSpeakerIcon(isPriority1SpekearOn, (ImageView) view);
                });

                if(channels.size() < 3)return;

                channelResumeHolder.priorityChannel2.setVisibility(View.VISIBLE);
                channelResumeHolder.priorityChannel2Image.setImageResource(getImageResource(channels.get(2).image));
                channelResumeHolder.priorityChannel2Name.setText(channels.get(2).name);
                toggleSpeakerIcon(isPriority2SpeakerOn, channelResumeHolder.priorityChannel2Speaker);
                channelResumeHolder.priorityChannel2Speaker.setOnClickListener(view -> {
                    isPriority2SpeakerOn = !isPriority2SpeakerOn;
                    toggleSpeakerIcon(isPriority2SpeakerOn, (ImageView) view);
                });


            } else if (holder instanceof AddChannelViewHolder) {
                //TODO: Add redirection to addChannelButton.
            }
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
                    String priority = "Priority Channel " + priorityIndicator;
                    priorityIndicator++;
                    return priority;
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
            int resumeItems = 1;
            int addChannelItems = 1;
            return channels.size() > 1 ? channels.size() + resumeItems + addChannelItems : channels.size() + addChannelItems;
        }

        class GenericViewHolder extends RecyclerView.ViewHolder {

            public GenericViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }

        class ChannelViewHolder extends GenericViewHolder implements RxListener{

            private View channelInfo;
            private RoundedImageView channelImage;
            private TextView channelName;
            private TextView channelType;
            private TextView lastMessageTime;
            private ImageView speakerButton;
            private View incomingMessageLayout;

            public ChannelViewHolder(@NonNull View itemView) {
                super(itemView);
                channelInfo = itemView.findViewById(R.id.channel_info);
                channelImage = itemView.findViewById(R.id.channel_image);
                channelName = itemView.findViewById(R.id.channel_name_text);
                channelType = itemView.findViewById(R.id.channel_type_text);
                lastMessageTime = itemView.findViewById(R.id.last_message_time);
                speakerButton = itemView.findViewById(R.id.channel_speaker);
                incomingMessageLayout = itemView.findViewById(R.id.incoming_message_layout);
            }

            @Override
            public void onRx(String id, String other) {
                incomingMessageLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onJsonRX(String id, String alias) {
                incomingMessageLayout.setVisibility(View.VISIBLE);
                ((TextView)incomingMessageLayout
                    .findViewById(R.id.incoming_message_name))
                    .setText(alias != null? alias : "UNKOWN ALIAS");
            }

            @Override
            public void stopRx() {
                incomingMessageLayout.setVisibility(View.INVISIBLE);
            }
        }

        class ChannelResumeViewHolder extends GenericViewHolder {

            private View primaryChannel;
            private RoundedImageView primaryChannelImage;
            private TextView primaryChannelName;
            private ImageView primaryChannelSpeaker;

            private View priorityChannel1;
            private RoundedImageView priorityChannel1Image;
            private TextView priorityChannel1Name;
            private ImageView priorityChannel1Speaker;

            private View priorityChannel2;
            private RoundedImageView priorityChannel2Image;
            private TextView priorityChannel2Name;
            private ImageView priorityChannel2Speaker;

            public ChannelResumeViewHolder(@NonNull View itemView) {
                super(itemView);

                primaryChannel = itemView.findViewById(R.id.primary_channel_layout);
                primaryChannelImage = itemView.findViewById(R.id.primary_channel_image);
                primaryChannelName = itemView.findViewById(R.id.primary_channel_name_text);
                primaryChannelSpeaker = itemView.findViewById(R.id.primary_channel_speaker);

                priorityChannel1 = itemView.findViewById(R.id.priority_channel_1_layout);
                priorityChannel1Name = itemView.findViewById(R.id.priority_channel_1_name_text);
                priorityChannel1Image = itemView.findViewById(R.id.priority_channel_1_image);
                priorityChannel1Speaker = itemView.findViewById(R.id.priority_channel_1_speaker);

                priorityChannel2 = itemView.findViewById(R.id.priority_channel_2_layout);
                priorityChannel2Name = itemView.findViewById(R.id.priority_channel_2_name_text);
                priorityChannel2Image = itemView.findViewById(R.id.priority_channel_2_image);
                priorityChannel2Speaker = itemView.findViewById(R.id.priority_channel_2_speaker);
            }
        }

        class AddChannelViewHolder extends GenericViewHolder {

            public AddChannelViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }
}