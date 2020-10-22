package com.rallytac.engageandroid.legba.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.drawable.TransitionDrawable;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.makeramen.roundedimageview.RoundedImageView;
import com.rallytac.engageandroid.Globals;
import com.rallytac.engageandroid.R;
import com.rallytac.engageandroid.legba.adapter.ChannelGroupAdapter;
import com.rallytac.engageandroid.legba.engage.RxListener;
import com.rallytac.engageandroid.legba.util.RUtils;
import com.rallytac.engageandroid.legba.view.SwipeButton;
import com.rallytac.engageandroid.legba.viewmodel.MissionViewModel;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.rallytac.engageandroid.legba.HostActivity;
import com.rallytac.engageandroid.legba.data.dto.Channel;
import com.rallytac.engageandroid.legba.data.dto.Mission;
import com.rallytac.engageandroid.databinding.FragmentMissionBinding;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import timber.log.Timber;

import static com.rallytac.engageandroid.legba.util.DimUtils.convertDpToPx;
import static com.rallytac.engageandroid.legba.util.RUtils.getImageResource;

public class MissionFragment extends Fragment {

    private HostActivity activity;
    private FragmentMissionBinding binding;
    private Mission mission;
    private ChannelSlidePageAdapter cspAdapter;
    private ChannelGroupAdapter cgAdapter;
    private TextView fragmentDescriptionText;
    private ImageView addChannel;
    private View closeLayout;
    private RecyclerView rvChannel;
    private ImageView[] dotIndicators;
    private MenuItem sosAction;
    private TransitionDrawable transition;
    private Context appContext;
    private MissionViewModel vm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MissionFragmentArgs missionFragmentArgs = MissionFragmentArgs.fromBundle(requireArguments());
        mission = missionFragmentArgs.getMission();
        vm = new ViewModelProvider(this).get(MissionViewModel.class);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = (HostActivity) requireActivity();

        transition = (TransitionDrawable) activity.binding.sosOverlapLayout.getBackground();

        updateToolbar();
        setHasOptionsMenu(true);
        setupEmergencyListeners();
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_mission, container, false);
        binding.toggleRadioChannelButton.setRotation(vm.getToggleRadioChannelButtonRotation());

        List<Channel> nonRadioChannels = mission.channels
                .stream()
                .filter(channel -> channel.type != Channel.ChannelType.RADIO)
                .collect(Collectors.toList());

        cspAdapter = new ChannelSlidePageAdapter(this, nonRadioChannels);
        binding.missionViewPager.setAdapter(cspAdapter);

        setupPTTOnMic();
        setupViewPagerOnPageChangeListener();
        setupViewPagerDotIndicator(nonRadioChannels);
        setUpSlidingUpPanelListener();
        setUpSlidingUpChannels();
        updateDots(0);
        setupAddChannelMission();

        return binding.getRoot();
    }

    private void updateToolbar() {
        requireActivity().findViewById(R.id.logo_image).setVisibility(View.VISIBLE);
        requireActivity().findViewById(R.id.toolbar_title_text).setVisibility(View.VISIBLE);
        ((TextView) requireActivity().findViewById(R.id.toolbar_title_text)).setText(mission.name);
        requireActivity().findViewById(R.id.fragment_description).setVisibility(View.VISIBLE);
        requireActivity().findViewById(R.id.add_channel).setVisibility(View.VISIBLE);
        fragmentDescriptionText = requireActivity().findViewById(R.id.fragment_description);
        fragmentDescriptionText.setTextColor(this.getResources().getColor(R.color.paleRed));
        addChannel = requireActivity().findViewById(R.id.add_channel);
        addChannel.setClickable(true);
        closeLayout = requireActivity().findViewById(R.id.close_layout);
        closeLayout.setClickable(true);
        Objects.requireNonNull(((HostActivity) requireActivity()).getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_round_keyboard_arrow_left_24);
    }

    private void setupEmergencyListeners(){
        activity.binding.sosSwipeButton.setSosEmergencyListener(new SwipeButton.SOSEmergencyListener() {

            boolean isGradientActive = false;

            @Override
            public void onSwipeStart() {
                Timber.i("onSwipeStart");
                sosAction.getActionView()
                        .animate()
                        .alpha(0f)
                        .setDuration(1);

                if (!isGradientActive) {
                    transition.startTransition(300);
                    isGradientActive = true;
                }
            }

            @Override
            public void onSwipeStartEnd() {
                if (isGradientActive) {
                    transition.reverseTransition(300);
                    isGradientActive = false;
                }
            }

            @Override
            public void onSosStart() {
                activity.binding.eyesGlowAnimation.setVisibility(View.VISIBLE);
                activity.binding.sosButtonGlowAnimation.setVisibility(View.VISIBLE);
                activity.binding.sosTxImage.animate()
                        .alpha(1f)
                        .setDuration(900);
                Globals.getEngageApplication().startTx(0, 1);
            }

            @Override
            public void onSosStop() {
                activity.binding.eyesGlowAnimation.setVisibility(View.GONE);
                activity.binding.sosButtonGlowAnimation.setVisibility(View.GONE);
                activity.binding.sosTxImage.animate()
                        .alpha(0.0f)
                        .setDuration(300);
                Globals.getEngageApplication().endTx();
            }

            @Override
            public void onFreeButton(double ms) {
                if (isGradientActive) {
                    transition.reverseTransition((int) ms);
                    isGradientActive = false;
                }
            }

            @Override
            public void onSwipeFinish() {
                toggleLayoutVisiblity(activity.binding.sosOverlapLayout);
                sosAction.getActionView()
                        .animate()
                        .alpha(1f)
                        .setDuration(300);
            }
        });
    }

    private void setupPTTOnMic() {
        binding.icMicCard.setOnTouchListener((v, event) -> {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                binding.txImage.setVisibility(View.VISIBLE);
                Log.w("sending", "#SB#: onTouch ACTION_DOWN - startTx");//NON-NLS
                Globals.getEngageApplication().startTx(0, 0);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                binding.txImage.setVisibility(View.INVISIBLE);
                Log.w("Stop sending", "#SB#: onTouch ACTION_UP - endTx");//NON-NLS
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
        int dotNumber = channels.size() > 1 ? channels.size() + 1 + 1 : channels.size() + 1;
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
                    vm.setToggleRadioChannelButtonRotation(180F);
                } else if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    toggleRadioChannelButton.animate().rotation(0F).setInterpolator(new AccelerateDecelerateInterpolator());
                    vm.setToggleRadioChannelButtonRotation(0F);
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

    private void setupAddChannelMission() {
        cgAdapter = new ChannelGroupAdapter(mission.channels, appContext, binding.missionViewPager.getAdapter());

        rvChannel = requireActivity().findViewById(R.id.rv_channels);
        rvChannel.setHasFixedSize(true);
        rvChannel.setAdapter(cgAdapter);

        addChannel.setOnClickListener(view -> setupLayoutVisibilityChannelGroup());
        closeLayout.setOnClickListener(view -> setupLayoutVisibilityChannelGroup());
        activity.binding.btnCreate.setOnClickListener(view -> {
            cspAdapter.setChannels(cgAdapter.getCheckChannels());
            setupLayoutVisibilityChannelGroup();
        });
    }

    private void setupLayoutVisibilityChannelGroup() {
        addChannel.setClickable(addChannel.isClickable());
        toggleLayoutVisiblity(binding.icMicCard);
        toggleLayoutVisiblity(binding.radioChannelsSlidingupLayout);
        toggleLayoutVisiblity(activity.binding.channelGroupLayout);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.mission_fragment_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        sosAction = menu.findItem(R.id.sos_action);
        View root = sosAction.getActionView();

        root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        toggleLayoutVisiblity(activity.binding.sosOverlapLayout);
                        //toggleLayoutVisiblity(activity.binding.incomingSosOverlapLayout);
                        return true;
                    case MotionEvent.ACTION_UP:
                        activity.binding.sosSwipeButton.dispatchTouchEvent(MotionEvent.obtain(event.getDownTime(),
                                event.getEventTime(),
                                MotionEvent.ACTION_UP,
                                event.getX(),
                                event.getY(),
                                event.getMetaState()));

                        return true;
                    case MotionEvent.ACTION_MOVE:
                        activity.binding.sosSwipeButton.dispatchTouchEvent(MotionEvent.obtain(event.getDownTime(),
                                event.getEventTime(),
                                MotionEvent.ACTION_MOVE,
                                event.getX(),
                                event.getY(),
                                event.getMetaState()));

                        return false;
                    default:
                        return false;
                }
            }
        });

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (appContext == null)
            appContext = context.getApplicationContext();
    }

    private void toggleLayoutVisiblity(View layout){
        if (layout.getVisibility() == View.GONE) {
            layout.animate()
                    .alpha(0.95f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            super.onAnimationEnd(animation);
                            layout.setVisibility(View.VISIBLE);
                        }
                    });
        } else {
            layout.animate()
                    .alpha(0.0f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            layout.setVisibility(View.GONE);
                        }
                    });
        }
    }

    private class ChannelSlidePageAdapter extends RecyclerView.Adapter<ChannelSlidePageAdapter.GenericViewHolder> {

        private Fragment fragment;
        private List<Channel> channels;

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

        private int position;

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
                this.position = position;
                return CHANNEL_ITEM;
            } else if (channels.size() > 1 && channels.size() == position) {
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
                ChannelViewHolder channelViewHolder = new ChannelViewHolder(itemView, position);
                Globals.actualListeners.add(channelViewHolder);
                return channelViewHolder;
            } else if (viewType == RESUME_CHANNELS_ITEM) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.channels_resume_item, parent, false);
                ChannelResumeViewHolder channelResumeViewHolder = new ChannelResumeViewHolder(itemView);

                for(RxListener currentListener: Globals.actualListeners) {
                    if(currentListener instanceof ChannelViewHolder) {
                        currentListener = (ChannelViewHolder) currentListener;
                        ((ChannelViewHolder) currentListener).setChannelResumeViewHolder(channelResumeViewHolder);
                    }
                }

                return channelResumeViewHolder;
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
                channelHolder.channelInfo.setOnClickListener(view -> NavHostFragment.findNavController(fragment)
                        .navigate(MissionFragmentDirections.actionMissionFragmentToChannelFragment(currentChannel)));
                channelHolder.channelImage.setImageResource(getImageResource(currentChannel.image));
                channelHolder.channelName.setText(currentChannel.name);
                channelHolder.channelType.setText(getTypeString(currentChannel.type));

                if (currentChannel.type == Channel.ChannelType.PRIORITY && currentChannel.id ==  2) {

                    channelHolder.channelImage.setBorderColor(getWaterBlueColor());
                    channelHolder.channelType.setTextColor(getWaterBlueColor());
                    channelHolder.lastMessageTime.setTextColor(getWaterBlueColor());
                    channelHolder.incomingMessageLayout
                            .findViewById(R.id.incoming_message_speaker_layout)
                            .setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.incoming_message_speaker_blue_layout_shape, null));
                    ((ImageView) channelHolder.incomingMessageLayout
                            .findViewById(R.id.incoming_message_speaker))
                            .setImageResource(R.drawable.ic_blue_speaker);
                    ((TextView) channelHolder.incomingMessageLayout
                            .findViewById(R.id.incoming_message_speaking_text))
                            .setTextColor(getResources().getColor(R.color.waterBlue93, null));
                    ((ImageView) channelHolder.incomingMessageLayout
                            .findViewById(R.id.rx_image))
                            .setImageResource(R.drawable.ic_blue_tx);
                } else if (currentChannel.type == Channel.ChannelType.PRIORITY && currentChannel.id == 3) {

                    channelHolder.channelImage.setBorderColor(getOrangeColor());
                    channelHolder.channelType.setTextColor(getOrangeColor());
                    channelHolder.lastMessageTime.setTextColor(getOrangeColor());
                    channelHolder.incomingMessageLayout
                            .findViewById(R.id.incoming_message_speaker_layout)
                            .setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.incoming_message_speaker_orange_layout_shape, null));
                    ((ImageView) channelHolder.incomingMessageLayout
                            .findViewById(R.id.incoming_message_speaker))
                            .setImageResource(R.drawable.ic_orange_speaker);
                    ((TextView) channelHolder.incomingMessageLayout
                            .findViewById(R.id.incoming_message_speaking_text))
                            .setTextColor(getResources().getColor(R.color.orange93, null));
                    ((ImageView) channelHolder.incomingMessageLayout
                            .findViewById(R.id.rx_image))
                            .setImageResource(R.drawable.ic_orange_tx);
                }

                switch (position) {
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

                if (channels.size() < 3) return;

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

        class ChannelViewHolder extends GenericViewHolder implements RxListener {
            private final static float LOW_OPACITY = 0.1f;
            private final static float FULL_OPACITY = 1f;

            private final static String PRIMARY_CHANNEL = "Primary Channel";
            private final static String PRIORITY_CHANNEL_1 = "Priority Channel 1";
            private final static String PRIORITY_CHANNEL_2 = "Priority Channel 2";

            private View channelLayout;
            private View channelInfo;
            private RoundedImageView channelImage;
            private TextView channelName;
            private TextView channelType;
            private TextView lastMessageText;
            private TextView lastMessageTime;
            private TextView lastMessageAlias;
            private TextView lastMessageDisplayName;
            private ImageView speakerButton;
            private View incomingMessageLayout;
            private ChannelResumeViewHolder channelResumeViewHolder;
            private Integer channelId;

            public ChannelViewHolder(@NonNull View itemView, int channelId) {
                super(itemView);

                channelLayout = itemView.findViewById(R.id.primary_channel_layout);
                channelInfo = itemView.findViewById(R.id.channel_info);
                channelImage = itemView.findViewById(R.id.channel_image);
                channelName = itemView.findViewById(R.id.channel_name_text);
                channelType = itemView.findViewById(R.id.channel_type_text);
                lastMessageText = itemView.findViewById(R.id.last_message_text);
                lastMessageTime = itemView.findViewById(R.id.last_message_time);
                lastMessageAlias = itemView.findViewById(R.id.last_message_alias);
                lastMessageDisplayName = itemView.findViewById(R.id.last_message_displayName);
                speakerButton = itemView.findViewById(R.id.channel_speaker);
                incomingMessageLayout = itemView.findViewById(R.id.incoming_message_layout);

                this.channelId = channelId;
                channelResumeViewHolder = null;
            }

            public ChannelResumeViewHolder getChannelResumeViewHolder() {
                return channelResumeViewHolder;
            }

            public void setChannelResumeViewHolder(ChannelResumeViewHolder channelResumeViewHolder) {
                this.channelResumeViewHolder = channelResumeViewHolder;
            }

            @Override
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

                incomingMessageLayout.setVisibility(View.VISIBLE);
                showIncommingMessageView(aliasText);

                ((TextView) incomingMessageLayout
                        .findViewById(R.id.incoming_message_name)).setText(aliasText);

                lastMessageTime.setText(timeText);
                lastMessageAlias.setText(aliasText);
                lastMessageDisplayName.setText(displayNameText);
                lastMessageText.setVisibility(View.VISIBLE);

                //Incoming SOS
                //toggleLayoutVisiblity(incomingMessageLayout);
                //activity.binding.incomingSosOverlapMessageName.setText(aliasText);
            }

            @Override
            public void stopRx() {
                //incomingMessageLayout.setVisibility(View.INVISIBLE);
                //toggleLayoutVisiblity(activity.binding.incomingSosOverlapLayout);
                hideIncommingMessageView();
                incomingMessageLayout.setVisibility(View.GONE);
            }

            private void showIncommingMessageView(String alias) {
                channelInfo.setAlpha(LOW_OPACITY);
                lastMessageTime.setAlpha(LOW_OPACITY);
                lastMessageAlias.setAlpha(LOW_OPACITY);
                lastMessageDisplayName.setAlpha(LOW_OPACITY);
                lastMessageText.setAlpha(LOW_OPACITY);

                showLayoutBackgroundIncommingMessageChannel(alias);
            }

            private void hideIncommingMessageView() {
                channelInfo.setAlpha(FULL_OPACITY);
                lastMessageTime.setAlpha(FULL_OPACITY);
                lastMessageAlias.setAlpha(FULL_OPACITY);
                lastMessageDisplayName.setAlpha(FULL_OPACITY);
                lastMessageText.setAlpha(FULL_OPACITY);

                hideLayoutBackgroundIncommingMessageChannel();
            }

            private void showLayoutBackgroundIncommingMessageChannel(String alias) {
                try {
                    if (this.channelId == 0) {
                        channelLayout.setBackground(ContextCompat.getDrawable(appContext,
                                R.drawable.primary_channel_item_fade_shape));

                        if(channelResumeViewHolder == null) return;
                        channelResumeViewHolder.primaryChannel
                                .setBackground(ContextCompat.getDrawable(appContext,
                                        R.drawable.primary_channel_item_fade_shape));

                        channelResumeViewHolder.primaryChannelDescription.setText(alias);
                        channelResumeViewHolder.priorityChannel1Description.setText(PRIORITY_CHANNEL_1);
                        channelResumeViewHolder.priorityChannel2Description.setText(PRIORITY_CHANNEL_2);

                        channelResumeViewHolder.primaryChannel.setAlpha(FULL_OPACITY);
                        channelResumeViewHolder.primaryChannelRx.setVisibility(View.VISIBLE);
                        channelResumeViewHolder.priorityChannel1.setAlpha(LOW_OPACITY);
                        channelResumeViewHolder.priorityChannel2.setAlpha(LOW_OPACITY);
                    }
                    else if(this.channelId == 1) {
                        channelLayout.setBackground(ContextCompat.getDrawable(appContext,
                                R.drawable.prioritary1_channel_item_fade_shape));

                        if(channelResumeViewHolder == null) return;
                        channelResumeViewHolder.priorityChannel1
                                .setBackground(ContextCompat.getDrawable(appContext,
                                        R.drawable.prioritary1_channel_item_fade_shape));

                        channelResumeViewHolder.primaryChannelDescription.setText(PRIMARY_CHANNEL);
                        channelResumeViewHolder.priorityChannel1Description.setText(alias);
                        channelResumeViewHolder.priorityChannel2Description.setText(PRIORITY_CHANNEL_2);

                        channelResumeViewHolder.primaryChannel.setAlpha(LOW_OPACITY);
                        channelResumeViewHolder.priorityChannel1Rx.setVisibility(View.VISIBLE);
                        channelResumeViewHolder.priorityChannel1.setAlpha(FULL_OPACITY);
                        channelResumeViewHolder.priorityChannel2.setAlpha(LOW_OPACITY);
                    } else if(this.channelId == 2) {
                        channelLayout.setBackground(ContextCompat.getDrawable(appContext,
                                R.drawable.prioritary2_channel_item_fade_shape));

                        if(channelResumeViewHolder == null) return;
                        channelResumeViewHolder.priorityChannel2
                                .setBackground(ContextCompat.getDrawable(appContext,
                                        R.drawable.prioritary2_channel_item_fade_shape));

                        channelResumeViewHolder.primaryChannelDescription.setText(PRIMARY_CHANNEL);
                        channelResumeViewHolder.priorityChannel1Description.setText(PRIORITY_CHANNEL_1);
                        channelResumeViewHolder.priorityChannel2Description.setText(alias);

                        channelResumeViewHolder.primaryChannel.setAlpha(LOW_OPACITY);
                        channelResumeViewHolder.priorityChannel2Rx.setVisibility(View.VISIBLE);
                        channelResumeViewHolder.priorityChannel1.setAlpha(LOW_OPACITY);
                        channelResumeViewHolder.priorityChannel2.setAlpha(FULL_OPACITY);
                     }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            private void hideLayoutBackgroundIncommingMessageChannel() {
                try {
                    channelLayout.setBackground(ContextCompat.getDrawable(appContext,
                            R.drawable.channel_item_shape));

                    if(channelResumeViewHolder == null) return;

                    channelResumeViewHolder.primaryChannelDescription.setText(PRIMARY_CHANNEL);
                    channelResumeViewHolder.priorityChannel1Description.setText(PRIORITY_CHANNEL_1);
                    channelResumeViewHolder.priorityChannel2Description.setText(PRIORITY_CHANNEL_2);

                    channelResumeViewHolder.primaryChannel.setAlpha(FULL_OPACITY);
                    channelResumeViewHolder.priorityChannel1.setAlpha(FULL_OPACITY);
                    channelResumeViewHolder.priorityChannel2.setAlpha(FULL_OPACITY);

                    channelResumeViewHolder.primaryChannelRx.setVisibility(View.GONE);
                    channelResumeViewHolder.priorityChannel1Rx.setVisibility(View.GONE);
                    channelResumeViewHolder.priorityChannel2Rx.setVisibility(View.GONE);

                    channelResumeViewHolder.primaryChannel
                            .setBackground(ContextCompat.getDrawable(appContext,
                                    R.drawable.channel_item_shape));
                    channelResumeViewHolder.priorityChannel1
                            .setBackground(ContextCompat.getDrawable(appContext,
                                    R.drawable.channel_item_shape));
                    channelResumeViewHolder.priorityChannel2
                            .setBackground(ContextCompat.getDrawable(appContext,
                                    R.drawable.channel_item_shape));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        class ChannelResumeViewHolder extends GenericViewHolder {

            private View primaryChannel;
            private RoundedImageView primaryChannelImage;
            private TextView primaryChannelName;
            private TextView primaryChannelDescription;
            private ImageView primaryChannelSpeaker;
            private ImageView primaryChannelRx;

            private View priorityChannel1;
            private RoundedImageView priorityChannel1Image;
            private TextView priorityChannel1Name;
            private TextView priorityChannel1Description;
            private ImageView priorityChannel1Speaker;
            private ImageView priorityChannel1Rx;

            private View priorityChannel2;
            private RoundedImageView priorityChannel2Image;
            private TextView priorityChannel2Name;
            private TextView priorityChannel2Description;
            private ImageView priorityChannel2Speaker;
            private ImageView priorityChannel2Rx;

            public ChannelResumeViewHolder(@NonNull View itemView) {
                super(itemView);

                primaryChannel = itemView.findViewById(R.id.primary_channel_layout);
                primaryChannelImage = itemView.findViewById(R.id.primary_channel_image);
                primaryChannelName = itemView.findViewById(R.id.primary_channel_name_text);
                primaryChannelDescription = itemView.findViewById(R.id.primary_channel_description);
                primaryChannelSpeaker = itemView.findViewById(R.id.primary_channel_speaker);
                primaryChannelRx = itemView.findViewById(R.id.rx_image_primary_channel);

                priorityChannel1 = itemView.findViewById(R.id.priority_channel_1_layout);
                priorityChannel1Name = itemView.findViewById(R.id.priority_channel_1_name_text);
                priorityChannel1Description = itemView.findViewById(R.id.priority_channel_1_description);
                priorityChannel1Image = itemView.findViewById(R.id.priority_channel_1_image);
                priorityChannel1Speaker = itemView.findViewById(R.id.priority_channel_1_speaker);
                priorityChannel1Rx = itemView.findViewById(R.id.rx_image_priority_channel_1);

                priorityChannel2 = itemView.findViewById(R.id.priority_channel_2_layout);
                priorityChannel2Name = itemView.findViewById(R.id.priority_channel_2_name_text);
                priorityChannel2Description = itemView.findViewById(R.id.priority_channel_2_description);
                priorityChannel2Image = itemView.findViewById(R.id.priority_channel_2_image);
                priorityChannel2Speaker = itemView.findViewById(R.id.priority_channel_2_speaker);
                priorityChannel2Rx = itemView.findViewById(R.id.rx_image_priority_channel_2);
            }
        }

        class AddChannelViewHolder extends GenericViewHolder {

            public AddChannelViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }
}