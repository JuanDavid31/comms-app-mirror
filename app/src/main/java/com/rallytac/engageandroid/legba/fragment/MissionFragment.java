package com.rallytac.engageandroid.legba.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.rallytac.engageandroid.Constants;
import com.rallytac.engageandroid.EngageApplication;
import com.rallytac.engageandroid.Globals;
import com.rallytac.engageandroid.R;
import com.rallytac.engageandroid.legba.adapter.ChannelListAdapter;
import com.rallytac.engageandroid.legba.adapter.ChannelSlidePageAdapter;
import com.rallytac.engageandroid.legba.app.VoiceRecognition;
import com.rallytac.engageandroid.legba.data.DataManager;
import com.rallytac.engageandroid.legba.data.dto.ChannelGroup;
import com.rallytac.engageandroid.legba.engage.GroupDiscoveryInfo;
import com.rallytac.engageandroid.legba.engage.GroupDiscoveryListener;
import com.rallytac.engageandroid.legba.engage.RxListener;
import com.rallytac.engageandroid.legba.view.SwipeButton;
import com.rallytac.engageandroid.legba.viewmodel.MissionViewModel;
import com.rallytac.engageandroid.legba.viewmodel.ViewModelFactory;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.rallytac.engageandroid.legba.HostActivity;
import com.rallytac.engageandroid.legba.data.dto.Channel;
import com.rallytac.engageandroid.legba.data.dto.Mission;
import com.rallytac.engageandroid.databinding.FragmentMissionBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import timber.log.Timber;

import static com.rallytac.engageandroid.legba.util.DimUtils.convertDpToPx;

public class MissionFragment extends Fragment implements RxListener, GroupDiscoveryListener {

    private HostActivity activity;
    public FragmentMissionBinding binding;
    private ChannelSlidePageAdapter channelSlidePageAdapter;
    private ChannelListAdapter channelListAdapter;
    private ImageView[] dotIndicators;
    private MenuItem sosAction;
    private TransitionDrawable sosOverlapBackgroundTransition;
    private Context context;
    private MissionViewModel vm;
    private VoiceRecognition voiceRecognition;
    private int currentPage;
    private boolean lastPage;

    private boolean isNewNameValid;
    private boolean isPttButtonDown = false;
    private long lastEventDownTime = 0;

    private class KeyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            long eventDownTime = intent.getLongExtra(Constants.KEY_EVENT_EXTRA_DOWN_TIME, 0);

            if (lastEventDownTime == 0) {
                lastEventDownTime = eventDownTime;
            } else {
                long elapsedTime = (eventDownTime - lastEventDownTime);
                if (elapsedTime <= 500) {
                    lastEventDownTime = 0;
                    if (isPttButtonDown) {
                        pttAction(MotionEvent.ACTION_UP);
                    } else {
                        pttAction(MotionEvent.ACTION_DOWN);
                    }
                }
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Timber.i("onCreate");
        super.onCreate(savedInstanceState);
        context = getContext();
        ViewModelFactory vmFactory = new ViewModelFactory((EngageApplication) getActivity().getApplication());
        vm = new ViewModelProvider(this, vmFactory).get(MissionViewModel.class);
        voiceRecognition = VoiceRecognition.getInstance(getActivity());
        Globals.rxListeners.add(this);
        Globals.groupDiscoveryListener = this;
        currentPage = -1;
        setupMission();

        KeyReceiver receiver = new KeyReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.KEY_EVENT_ACTION);
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy() {
        Timber.i("onDestroy");
        voiceRecognition.stopVoiceRecognition();
        super.onDestroy();
    }


    private void setupMission() {
        MissionFragmentArgs missionFragmentArgs = MissionFragmentArgs.fromBundle(requireArguments());
        String missionId = missionFragmentArgs.getMissionId();

        vm.setupMission(missionId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Timber.i("onCreateView");
        activity = (HostActivity) requireActivity();

        sosOverlapBackgroundTransition = (TransitionDrawable) activity.binding.sosOverlapLayout.getBackground();

        updateToolbar();
        setHasOptionsMenu(true);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_mission, container, false);

        List<ChannelGroup> channelsGroup = vm
                .getChannelsGroup();
        channelSlidePageAdapter = new ChannelSlidePageAdapter(this, channelsGroup);
        binding.missionViewPager.setAdapter(channelSlidePageAdapter);

        recoverState();
        setupEmergencyListeners();
        setupViewPagerOnPageChangeListener();
        setupPTTOnMic();
        setupViewPagerDotIndicator(vm.getChannelsGroup());
        setUpSlidingUpPanelListener();
        setUpSlidingUpChannels();
        updateDots(0);
        setupEditCurrentChannelGroupLayout();

        return binding.getRoot();
    }

    private void updateToolbar() {
        activity.binding.logoImage.setVisibility(View.VISIBLE);
        activity.binding.toolbarTitleText.setVisibility(View.VISIBLE);
        activity.binding.toolbarTitleText.setText(vm.getMission().getName());
        activity.binding.fragmentDescription.setVisibility(View.VISIBLE);
        activity.binding.showCreateEditChannelsGroupButton.setVisibility(View.VISIBLE);
        activity.binding.fragmentDescription.setTextColor(this.getResources().getColor(R.color.paleRed));
        Objects.requireNonNull(((HostActivity) requireActivity()).getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_round_keyboard_arrow_left_24);
    }

    private void recoverState() {
        binding.toggleRadioChannelButton.setRotation(vm.getToggleRadioChannelButtonRotation());
        if (vm.isMissionOnSos()) {
            ((RxListener) this).onRx(vm.getIncomingSosChannelId(), vm.getIncomingSosAlias(), vm.getIncomingSosDisplayName(), true);
        }
    }

    private void setupEmergencyListeners() {
        activity.binding.sosSwipeButton.setSosEmergencyListener(new SwipeButton.SOSEmergencyListener() {

            boolean isGradientActive = false;

            @Override
            public void onSwipeStart() {
                sosAction.getActionView()
                        .animate()
                        .alpha(0f)
                        .setDuration(1);

                if (!isGradientActive) {
                    sosOverlapBackgroundTransition.startTransition(300);
                    isGradientActive = true;
                }
            }

            @Override
            public void onSwipeStartEnd() {
                if (isGradientActive) {
                    sosOverlapBackgroundTransition.reverseTransition(300);
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

                String[] activeGroupIds = vm.getChannelsGroup()
                        .get(currentPage)
                        .getChannels()
                        .stream()
                        .map(Channel::getId)
                        .toArray(String[]::new);

                DataManager.getInstance().startTx(true, activeGroupIds);
            }

            @Override
            public void onSosStop() {
                activity.binding.eyesGlowAnimation.setVisibility(View.GONE);
                activity.binding.sosButtonGlowAnimation.setVisibility(View.GONE);
                activity.binding.sosTxImage.animate()
                        .alpha(0.0f)
                        .setDuration(300);

                String[] activeGroupIds = vm.getChannelsGroup()
                        .get(currentPage)
                        .getChannels()
                        .stream()
                        .map(Channel::getId)
                        .toArray(String[]::new);

                DataManager.getInstance().endTx(activeGroupIds);
            }

            @Override
            public void onFreeButton(double ms) {
                if (isGradientActive) {
                    sosOverlapBackgroundTransition.reverseTransition((int) ms);
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

    private void setupViewPagerOnPageChangeListener() {
        binding.missionViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                boolean speak = currentPage != position;
                currentPage = position;
                int channelsGroupsSize = vm.getChannelsGroup().size();
                lastPage = currentPage == channelsGroupsSize;
                updateDots(position);

                if (position < channelsGroupsSize) {
                    activity.binding.showCreateEditChannelsGroupButton.setVisibility(View.VISIBLE);
                    String name = vm.getChannelsGroup().get(position).getName();
                    activity.binding.fragmentDescription.setText(name);
                    if (speak) {
                        voiceRecognition.speak(name);
                    }
                } else {
                    activity.binding.fragmentDescription.setText(getString(R.string.add_title));
                    activity.binding.showCreateEditChannelsGroupButton.setVisibility(View.GONE);
                }

                //updateChannelListAdapter(); // Prevent a visual bug bug creates another problem when the storage is clean and the user tries to create a new channel group.
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupPTTOnMic() {
        binding.icMicCard.setOnTouchListener((view, event) -> {

            return pttAction(event.getAction());
        });
    }

    private boolean pttAction(int action) {
        if (lastPage) {
            return false;
        }

        String[] activeGroupIds = vm.getChannelsGroup()
                .get(currentPage)
                .getChannels()
                .stream()
                .map(Channel::getId)
                .toArray(String[]::new);

        /*if (vm.isMissionOnSos()){
            binding.txImage.setVisibility(View.INVISIBLE);
            DataManager.getInstance().endTx(activeGroupIds);
            return false;
        }*/

        if (action == MotionEvent.ACTION_DOWN) {
            isPttButtonDown = true;
            binding.txImage.setVisibility(View.VISIBLE);
            Log.w("sending", "#SB#: onTouch ACTION_DOWN - startTx");//NON-NLS
            Timber.i("Tx to %s", activeGroupIds);
            DataManager.getInstance().startTx(false, activeGroupIds);
        } else if (action == MotionEvent.ACTION_UP) {
            isPttButtonDown = false;
            binding.txImage.setVisibility(View.INVISIBLE);
            Log.w("Stop sending", "#SB#: onTouch ACTION_UP - endTx");//NON-NLS
            DataManager.getInstance().endTx(activeGroupIds);
        }
        return true;
    }

    private void setupViewPagerDotIndicator(List channelGroup) {
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
        RadioChannelsRecyclerViewAdapter adapter = new RadioChannelsRecyclerViewAdapter(new RadioChannelsRecyclerViewAdapter.AdapterDiffCallback(), this);
        binding.radioChannelsRecycler.setHasFixedSize(true);
        binding.radioChannelsRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        binding.radioChannelsRecycler.setAdapter(adapter);
        adapter.setRadioChannels(vm.getAudioChannels());
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

    private void updateChannelListAdapter() {
        List<Channel> allChannels = vm
                .getAudioChannels()
                .stream()
                .peek(channel -> channel.setActive(false))
                .collect(Collectors.toList());

        if (!lastPage) {
            activity.binding.deleteChannelViewButton.setVisibility(View.VISIBLE);

            List<Channel> activeChannels = vm.getChannelsGroup().get(currentPage).getChannels();
            allChannels = allChannels
                    .stream()
                    .map(channel -> {
                        activeChannels
                                .stream()
                                .filter(activeChannel -> activeChannel.getId().equals(channel.getId()))
                                .findFirst()
                                .ifPresent(activeChannel -> {
                                    channel.setActive(true);
                                });
                        return channel;
                    })
                    .collect(Collectors.toList());
        } else {
            activity.binding.deleteChannelViewButton.setVisibility(View.GONE);
        }

        channelListAdapter.setChannels(allChannels);
    }

    private void setupEditCurrentChannelGroupLayout() {
        List<Channel> allChannels = //new ArrayList<>();
                vm.getAudioChannels();
        if (allChannels.isEmpty()) {
            activity.binding.channelsRecycler.setVisibility(View.GONE);
            activity.binding.defineAChannelText.setVisibility(View.VISIBLE);
        } else {
            activity.binding.channelsRecycler.setVisibility(View.VISIBLE);
            activity.binding.defineAChannelText.setVisibility(View.GONE);
        }
        channelListAdapter = new ChannelListAdapter(allChannels, this);
        activity.binding.channelsRecycler.setHasFixedSize(true);
        activity.binding.channelsRecycler.setAdapter(channelListAdapter);

        activity.binding.showCreateEditChannelsGroupButton.setOnClickListener(view -> toggleCreateEditChannelsGroupLayoutvisibility());
        activity.binding.closeCreateEditChannelsGroupViewButton.setOnClickListener(view -> toggleCreateEditChannelsGroupLayoutvisibility());
        activity.binding.createEditChannelsGroupButton.setOnClickListener(view -> updateCurrentChannelsGroup());
        activity.binding.deleteChannelViewButton.setOnClickListener(view -> toggleLayoutVisiblity(activity.binding.removeChannelGroupLayout));
        activity.binding.removeChannelGroupNoOptionButton.setOnClickListener(view -> toggleLayoutVisiblity(activity.binding.removeChannelGroupLayout));
        activity.binding.removeChannelGroupYesOptionButton.setOnClickListener(view -> {
            removeChannelGroup();
            toggleLayoutVisiblity(activity.binding.removeChannelGroupLayout);
            hideCreateEditChannelsGroupLayoutAndUpdateUi();
        });

        activity.binding.channelGroupNameText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String channelGroupNameSearch = editable.toString().toLowerCase().trim();
                String currentChannelGroupName = activity.binding.fragmentDescription.getText().toString().toLowerCase().trim();

                boolean isNameRepeated = vm.getChannelsGroup()
                        .stream()
                        .map(ChannelGroup::getName)
                        .peek(channelGroupName -> Timber.i("Before filter %s", channelGroupName))//Debugging purposes
                        .filter(channelGroupName -> !channelGroupName.equalsIgnoreCase(currentChannelGroupName))//Ignores current name, even if it is 'add title'
                        .peek(channelGroupName -> Timber.i("After filter %s", channelGroupName)) //Debugging purposes
                        .anyMatch(channelGroupName -> channelGroupName.equalsIgnoreCase(channelGroupNameSearch));

                if (isNameRepeated && !(currentChannelGroupName.equals(""))) {
                    Toast.makeText(context, "Titles have to be unique, this title exists.", Toast.LENGTH_SHORT).show();
                }

                isNewNameValid = !isNameRepeated && !channelGroupNameSearch.isEmpty();

                if (isNewNameValid && !channelListAdapter.getCheckedChannels().isEmpty()) {
                    activity.binding.createEditChannelsGroupButton.setBackground(ContextCompat.getDrawable(context, R.drawable.pale_red_shape));
                } else {
                    activity.binding.createEditChannelsGroupButton.setBackground(ContextCompat.getDrawable(context, R.drawable.black_shape));
                }
            }
        });
    }


    private void updateCurrentChannelsGroup() { //Rename
        String newName = activity.binding.channelGroupNameText.getText().toString();
        List<Channel> checkedChannels = channelListAdapter.getCheckedChannels();
        if (!isNewNameValid && checkedChannels.isEmpty()) {
            Toast.makeText(context, "Please select at least one channel and write a proper name for this view", Toast.LENGTH_SHORT).show();
            return;
        } else if (!isNewNameValid) {
            Toast.makeText(context, "Please write a proper name for this view", Toast.LENGTH_SHORT).show();
            return;
        } else if (checkedChannels.isEmpty()) {
            Toast.makeText(context, "Please select at least one channel", Toast.LENGTH_SHORT).show();
            return;
        }

        updateNameAndActiveChannelsOnCurrentChannelsGroup(newName, checkedChannels);
        hideCreateEditChannelsGroupLayoutAndUpdateUi();
    }

    private void updateNameAndActiveChannelsOnCurrentChannelsGroup(String newName, List<Channel> channels) {

        if (lastPage) {//Creates a new channelGroup
            ChannelGroup newChannelGroup = new ChannelGroup(newName, vm.getMission().getId(), channels);
            vm.addChannelGroup(newChannelGroup);
            vm.getChannelsGroup().add(newChannelGroup);
            lastPage = false;
        } else {//Edits current channelGroup
            ChannelGroup currentChannelGroup = vm.getChannelsGroup().get(currentPage);

            currentChannelGroup.setName(newName);
            currentChannelGroup.setChannels(channels);

            vm.updateChannelGroup(currentChannelGroup);
        }

        activity.binding.fragmentDescription.setText(newName);
        voiceRecognition.speak(newName);
        activity.binding.showCreateEditChannelsGroupButton.setVisibility(View.VISIBLE);
    }

    private void hideCreateEditChannelsGroupLayoutAndUpdateUi() {
        toggleCreateEditChannelsGroupLayoutvisibility();
        channelSlidePageAdapter.setChannelsGroup(vm.getChannelsGroup());
        setupViewPagerDotIndicator(vm.getChannelsGroup());
        updateDots(currentPage);
    }

    public void toggleCreateEditChannelsGroupLayoutvisibility() {
        setupCreateEditChannelsGroupButton(true);
        hideKeyboard(activity.binding.createEditChannelsGroupLayout);
        String fragmentDescription = activity.binding.fragmentDescription.getText().toString();
        activity.binding.channelGroupNameText.setText(mapFragmentDescriptionName(fragmentDescription));
        toggleLayoutVisiblity(binding.icMicCard);
        toggleLayoutVisiblity(binding.radioChannelsSlidingupLayout);
        toggleLayoutVisiblity(activity.binding.channelGroupLayout);

        updateChannelListAdapter();
    }

    public void setupCreateEditChannelsGroupButton(boolean areThereActiveChannels) {

        String currentName = activity.binding.channelGroupNameText.getText().toString().trim();
        if (areThereActiveChannels && currentName.length() > 0) {
            activity.binding.createEditChannelsGroupButton.setBackground(ContextCompat.getDrawable(context, R.drawable.pale_red_shape));
        } else {
            activity.binding.createEditChannelsGroupButton.setBackground(ContextCompat.getDrawable(context, R.drawable.black_shape));
        }

        if (lastPage) {
            activity.binding.createEditChannelsGroupButton.setText("Create");
        } else {
            activity.binding.createEditChannelsGroupButton.setText("Save");
        }
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private String mapFragmentDescriptionName(String fragmentDescriptionName) {
        if (fragmentDescriptionName.equalsIgnoreCase(getString(R.string.add_title))) {
            long numberOfViews = vm.getChannelsGroup()
                    .stream()
                    .map(channelGroup -> channelGroup.getName().toLowerCase())
                    .filter(name -> name.contains("view "))
                    .count();
            return String.format("View %s", (numberOfViews + 1));
        } else {
            return fragmentDescriptionName;
        }
    }

    private void removeChannelGroup() {
        vm.deleteChannelGroup(currentPage);
        boolean channelGroupSize = currentPage >= vm.getChannelsGroup().size();

        if (channelGroupSize) {
            lastPage = true;
            activity.binding.fragmentDescription.setText("");
            activity.binding.showCreateEditChannelsGroupButton.setVisibility(View.GONE);
        } else {
            activity.binding.fragmentDescription.setText(vm.getChannelsGroup().get(currentPage).getName());
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.mission_fragment_menu, menu);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        sosAction = menu.findItem(R.id.sos_action);
        View root = sosAction.getActionView();

        root.setOnTouchListener((view, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    toggleLayoutVisiblity(activity.binding.sosOverlapLayout);
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
        });

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

    @Override
    public void onStop() {
        super.onStop();
        Globals.rxListeners.remove(this);
    }

    @Override
    public void onRx(String id, String alias, String displayName, boolean isSos) {
        if (!isSos) return;
        Timber.i("OnRx SOS");

        vm.getAudioChannels()
                .stream()
                .map(Channel::getId)
                .filter(channelId -> channelId.equalsIgnoreCase(id))
                .findFirst()
                .ifPresent(channel -> {
                    Timber.i("OnRx SOS ifPresent %s %s %s", id, alias, displayName);
                    vm.setIncomingSosChannelId(id);
                    vm.setIncomingSosAlias(alias);
                    vm.setIncomingSosDisplayName(displayName);
                    vm.setMissionOnSos(true);

                    pauseActiveTx(); //Only works if full duplex is enable

                    activity.binding.incomingSosOverlapMessageName.setText(alias);
                    if (activity.binding.incomingSosOverlapLayout.getVisibility() == View.GONE) {
                        // this method (onRx) is being called every time a speaker/talker is added, so this is being called more than once
                        //making this toggle bug the incomingSosOverlapLayout
                        toggleLayoutVisiblity(activity.binding.incomingSosOverlapLayout);
                    }
                });
    }

    private void pauseActiveTx() {
        vm.getAudioChannels().forEach(audioChannel -> audioChannel.setOnRx(false));
        String[] channelIds = vm.getAudioChannels()
                .stream()
                .peek(audioChannel -> audioChannel.setOnRx(false))
                .map(Channel::getId)
                .toArray(String[]::new);

        binding.txImage.setVisibility(View.INVISIBLE);
        DataManager.getInstance().endTx(channelIds);
    }

    @Override
    public void stopRx(String id, String eventExtraJson) {
        Timber.i("stopRX");
        if (vm.isMissionOnSos()) {
            Timber.i("stopRX MissionsOnSos");
            vm.setMissionOnSos(false);
            toggleLayoutVisiblity(activity.binding.incomingSosOverlapLayout);
        }
    }

    @Override
    public void onGroupDiscover(String groupId, GroupDiscoveryInfo groupDiscoveryInfo) {
        Timber.i("onGroupDiscover");
        vm.addChannelUser(groupDiscoveryInfo);
    }

    @Override
    public void onGroupRediscover(String groupId, GroupDiscoveryInfo groupDiscoveryInfo) {
        Timber.i("onGroupRediscover");
        vm.addChannelUser(groupDiscoveryInfo);
    }

    @Override
    public void onGroupUndiscover(String groupId, GroupDiscoveryInfo groupDiscoveryInfo) {
        Timber.i("onGroupUndiscover");
        vm.removeChannelUser(groupDiscoveryInfo);
    }
}