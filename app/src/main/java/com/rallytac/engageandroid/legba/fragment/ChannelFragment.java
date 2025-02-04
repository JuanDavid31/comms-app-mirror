package com.rallytac.engageandroid.legba.fragment;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.rallytac.engageandroid.R;
import com.rallytac.engageandroid.databinding.FragmentChannelBinding;
import com.rallytac.engageandroid.legba.HostActivity;

import com.rallytac.engageandroid.legba.data.dto.Channel;
import com.rallytac.engageandroid.legba.data.dto.Member;
import com.rallytac.engageandroid.legba.util.RUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ChannelFragment extends Fragment {

    private FragmentChannelBinding binding;
    private Channel channel;
    private FrameLayout redCircle;
    private TextView notificationsText;
    private int notificationsCount = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChannelFragmentArgs channelFragmentArgs = ChannelFragmentArgs.fromBundle(requireArguments());
        channel = channelFragmentArgs.getChannel();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setupToolbar();
        setHasOptionsMenu(true);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_channel, container, false);

        UsersRecyclerViewAdapter adapter =
                new UsersRecyclerViewAdapter(new UsersRecyclerViewAdapter.AdapterDiffCallback(), this);
        binding.channelElementsRecycler.setHasFixedSize(true);

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            binding.channelElementsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.channelElementsRecycler.setLayoutManager(new GridLayoutManager(getContext(), 2));
        }

        binding.channelElementsRecycler.setAdapter(adapter);

        //
        List<Member> members = new ArrayList<>();
        members = channel.users
                .stream()
                .map(userIdentity -> {
                    Member newMember = new Member();
                    newMember.setName(userIdentity.displayName);
                    newMember.setNickName(userIdentity.displayName);
                    newMember.setState(Member.RequestType.ADDED);
                    newMember.setNumber("544321591");
                    return newMember;
                }).collect(Collectors.toList());
        //
        adapter.setMembers(members);

        return binding.getRoot();
    }

    private void setupToolbar() {
        requireActivity().findViewById(R.id.toolbar_left_title_text).setVisibility(View.VISIBLE);
        ((TextView) requireActivity().findViewById(R.id.toolbar_left_title_text)).setText(channel.getName());
        requireActivity().findViewById(R.id.toolbar_background_image).setVisibility(View.VISIBLE);
        ((ImageView)requireActivity().findViewById(R.id.toolbar_background_image)).setImageResource(RUtils.getImageResource(channel.getImage()));
        requireActivity().findViewById(R.id.logo_image).setVisibility(View.VISIBLE);
        Objects.requireNonNull(((HostActivity) requireActivity()).getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_round_keyboard_arrow_left_24); //Default dp is unknown
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.channel_fragment_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        /*MenuItem item = menu.findItem(R.id.notifications_action);
        View root = item.getActionView();
        root.setOnClickListener(view -> onOptionsItemSelected(item));
        redCircle = root.findViewById(R.id.notifications_action_red_circle);
        notificationsText = root.findViewById(R.id.notifications_action_red_circle_count_text);*/
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        /*if (item.getItemId() == R.id.notifications_action) {
            notificationsCount = (notificationsCount + 1) % 6; //Cycle trhough 0 - 5
            updateNotificationsIcon();
            return true;
        } else */if (item.getItemId() == R.id.history_action) {
            NavHostFragment.findNavController(this)
                    .navigate(ChannelFragmentDirections.actionChannelFragmentToChannelHistoryFragment(channel.getId()));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateNotificationsIcon() {
        // if notifications count extends into two digits, just show the red circle
        if (0 < notificationsCount && notificationsCount < 10) {
            notificationsText.setText(String.valueOf(notificationsCount));
        } else {
            notificationsText.setText("");
        }

        redCircle.setVisibility((notificationsCount > 0) ? View.VISIBLE : View.GONE);
    }

}