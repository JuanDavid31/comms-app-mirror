package com.rallytac.engageandroid.legba.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.rallytac.engageandroid.Globals;
import com.rallytac.engageandroid.R;
import com.rallytac.engageandroid.legba.adapter.ChannelGroupAdapter;
import com.rallytac.engageandroid.legba.adapter.ChannelSlidePageAdapter;
import com.rallytac.engageandroid.legba.data.dto.ChannelGroup;
import com.rallytac.engageandroid.legba.util.StringUtils;
import com.rallytac.engageandroid.legba.view.SwipeButton;
import com.rallytac.engageandroid.legba.viewmodel.MissionViewModel;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.rallytac.engageandroid.legba.HostActivity;
import com.rallytac.engageandroid.legba.data.dto.Channel;
import com.rallytac.engageandroid.legba.data.dto.Mission;
import com.rallytac.engageandroid.databinding.FragmentMissionBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import timber.log.Timber;

import static com.rallytac.engageandroid.legba.util.DimUtils.convertDpToPx;

public class MissionFragment extends Fragment {

    private HostActivity activity;
    private FragmentMissionBinding binding;
    private Mission mission;
    private ChannelSlidePageAdapter cspAdapter;
    private ChannelGroupAdapter cgAdapter;
    private TextView fragmentDescriptionText;
    private ImageView editChannel;
    private EditText channelGroupName;
    private Button btnEdit;
    private View closeLayout;
    private RecyclerView rvChannel;
    private ImageView[] dotIndicators;
    private MenuItem sosAction;
    private TransitionDrawable transition;
    private Context appContext;
    private MissionViewModel vm;
    private List<ChannelGroup> channelsGroup;

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

        channelsGroup = Arrays.asList(new ChannelGroup("Alpha", mission.channels.stream().limit(4).collect(Collectors.toList())),
                new ChannelGroup("Delta", new ArrayList<>()));
        cspAdapter = new ChannelSlidePageAdapter(this, channelsGroup);
        binding.missionViewPager.setAdapter(cspAdapter);

        setupPTTOnMic();
        setupViewPagerOnPageChangeListener();
        setupViewPagerDotIndicator(channelsGroup);
        setUpSlidingUpPanelListener();
        setUpSlidingUpChannels();
        updateDots(0);
        setupEditChannelGroup();

        return binding.getRoot();
    }

    public Mission getMission() {
        return mission;
    }

    public Context getAppContext() {
        return appContext;
    }

    public ChannelGroupAdapter getCgAdapter() {
        return cgAdapter;
    }

    public View getCloseLayout() {
        return closeLayout;
    }

    public Button getBtnEdit() {
        return btnEdit;
    }

    private void updateToolbar() {
        requireActivity().findViewById(R.id.logo_image).setVisibility(View.VISIBLE);
        requireActivity().findViewById(R.id.toolbar_title_text).setVisibility(View.VISIBLE);
        ((TextView) requireActivity().findViewById(R.id.toolbar_title_text)).setText(mission.name);
        requireActivity().findViewById(R.id.fragment_description).setVisibility(View.VISIBLE);
        requireActivity().findViewById(R.id.add_channel).setVisibility(View.VISIBLE);
        fragmentDescriptionText = requireActivity().findViewById(R.id.fragment_description);
        fragmentDescriptionText.setTextColor(this.getResources().getColor(R.color.paleRed));
        editChannel = requireActivity().findViewById(R.id.add_channel);
        editChannel.setClickable(true);
        closeLayout = requireActivity().findViewById(R.id.close_layout);
        closeLayout.setClickable(true);
        Objects.requireNonNull(((HostActivity) requireActivity()).getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_round_keyboard_arrow_left_24);
    }

    private void setupEmergencyListeners() {
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
                if(position < channelsGroup.size()) {
                    editChannel.setVisibility(View.VISIBLE);
                    String name = StringUtils.capitalize(channelsGroup.get(position).name);
                    fragmentDescriptionText.setText(name);
                } else {
                    fragmentDescriptionText.setText("");
                    editChannel.setVisibility(View.GONE);
                }

            }
        });
    }

    private void setupViewPagerDotIndicator(List<ChannelGroup> channelGroup) {
        binding.tabLayout.removeAllViews();
        int dotNumber = channelGroup.size() + 1;
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

    private void setupEditChannelGroup() {
        cgAdapter = new ChannelGroupAdapter(mission.channels, this);
        rvChannel = requireActivity().findViewById(R.id.rv_channels);
        rvChannel.setHasFixedSize(true);
        rvChannel.setAdapter(cgAdapter);

        btnEdit = requireActivity().findViewById(R.id.btn_edit);
        channelGroupName = requireActivity().findViewById(R.id.channel_group_name);

        editChannel.setOnClickListener(view -> setupLayoutVisibilityChannelGroup());
        closeLayout.setOnClickListener(view -> setupLayoutVisibilityChannelGroup());
        btnEdit.setOnClickListener(view -> editChannelGroup());

        channelGroupName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String channelNameSearch = editable.toString().toLowerCase();
                String lastName = fragmentDescriptionText.getText().toString().toLowerCase();
                Long validation = channelsGroup.stream()
                        .filter(channelGroup ->
                                thereIsAChannelGroupWith(channelNameSearch, channelGroup))
                        .count();

                if((validation < 1L && channelNameSearch.length() > 0) || lastName.equals(channelNameSearch)) {
                    btnEdit.setClickable(true);
                    btnEdit.setBackground(ContextCompat.getDrawable(appContext, R.drawable.edit_channel_group_btn_shape));
                } else {
                    btnEdit.setClickable(false);
                    btnEdit.setBackground(ContextCompat.getDrawable(appContext, R.drawable.edit_channel_group_btn_fade_shape));
                }
            }
        });
    }

    private void editChannelGroup() {
        List<Channel> channels = cgAdapter.getChannels();
        List<Channel> activeChannels = cgAdapter.getCheckChannels();

        updateChannelsGroup(activeChannels);
        setupLayoutVisibilityChannelGroup();
        setupViewPagerDotIndicator(channelsGroup);

        //mission.setChannels(channels);
        cspAdapter.setChannelsGroup(channelsGroup);
        updateDots(0);
    }

    private void updateChannelsGroup(List<Channel> activeChannels) {
        String lastName = fragmentDescriptionText.getText().toString();
        String newName = channelGroupName.getText().toString();

        for(ChannelGroup channelGroup: channelsGroup) {
            if(channelGroup.name.equals(lastName)) {
                channelGroup.name = newName;
                channelGroup.channels = activeChannels;
                fragmentDescriptionText.setText(newName);
                break;
            }
        }
    }

    public void setupLayoutVisibilityChannelGroup() {
        channelGroupName.setText(fragmentDescriptionText.getText().toString());
        editChannel.setClickable(!editChannel.isClickable());
        toggleLayoutVisiblity(binding.icMicCard);
        toggleLayoutVisiblity(binding.radioChannelsSlidingupLayout);
        toggleLayoutVisiblity(activity.binding.channelGroupLayout);

        String channelName = fragmentDescriptionText.getText().toString();
        ChannelGroup channelGroup = channelsGroup.stream()
                .filter(channelCurrent -> channelCurrent.name.equals(channelName))
                .findFirst()
                .get();

        for(Channel currentChannel: mission.channels) {
            currentChannel.status = false;
        }

        List<Channel> activeChannels = mission.channels.stream()
                .map(channel -> {
                    for(Channel currentChannel: channelGroup.channels) {
                        if(currentChannel.name.equals(channel.name)) {
                            channel.status = true;
                            break;
                        }
                    }
                    return channel;
                })
                .collect(Collectors.toList());
        cgAdapter.setChannels(activeChannels);
    }

    private boolean thereIsAChannelGroupWith(String channelNameSearch, ChannelGroup channelGroup) {
        return channelGroup.name.toLowerCase().equals(channelNameSearch);
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

    private void toggleLayoutVisiblity(View layout) {
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
}