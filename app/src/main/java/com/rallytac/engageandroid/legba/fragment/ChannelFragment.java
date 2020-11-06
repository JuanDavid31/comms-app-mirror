package com.rallytac.engageandroid.legba.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.rallytac.engageandroid.R;
import com.rallytac.engageandroid.databinding.FragmentChannelBinding;
import com.rallytac.engageandroid.legba.HostActivity;

import com.rallytac.engageandroid.legba.data.dto.Channel;
import com.rallytac.engageandroid.legba.data.dto.ChannelElement;
import com.rallytac.engageandroid.legba.data.dto.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        updateToolbar();
        setHasOptionsMenu(true);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_channel, container, false);

        ChannelElementsRecyclerViewAdapter adapter =
                new ChannelElementsRecyclerViewAdapter(new ChannelElementsRecyclerViewAdapter.AdapterDiffCallback(), this);
        binding.channelElementsRecycler.setHasFixedSize(true);
        binding.channelElementsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.channelElementsRecycler.setAdapter(adapter);

        //
        List<ChannelElement> channelElements = new ArrayList<>();
        channel.users.forEach(userIdentity ->{
            Member newMember = new Member();
            newMember.setName(userIdentity.displayName);
            newMember.setNickName(userIdentity.displayName);
            newMember.setState(Member.RequestType.ADDED);
            newMember.setNumber("544321591");
            channelElements.add(newMember);
        });
        //
        channel.setChannelElements(channelElements);

        adapter.setChannelElements(channel.getChannelElements());

        return binding.getRoot();
    }

    private void updateToolbar() {
        requireActivity().findViewById(R.id.toolbar_title_text).setVisibility(View.VISIBLE);
        ((TextView) requireActivity().findViewById(R.id.toolbar_title_text)).setText(channel.getName());
        Objects.requireNonNull(((HostActivity) requireActivity()).getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_round_keyboard_arrow_left_24);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.channel_fragment_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        MenuItem item = menu.findItem(R.id.notifications_action);
        View root = item.getActionView();
        root.setOnClickListener(view -> onOptionsItemSelected(item));
        redCircle = root.findViewById(R.id.notifications_action_red_circle);
        notificationsText = root.findViewById(R.id.notifications_action_red_circle_count_text);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.notifications_action){
            notificationsCount = (notificationsCount + 1)  % 6; //Cycle trhough 0 - 5
            updateNotificationsIcon();
            return true;
        }else if (item.getItemId() == R.id.history_action) {
            Toast.makeText(getContext(), "History pressed", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateNotificationsIcon(){
        // if notifications count extends into two digits, just show the red circle
        if (0 < notificationsCount && notificationsCount < 10) {
            notificationsText.setText(String.valueOf(notificationsCount));
        } else {
            notificationsText.setText("");
        }

        redCircle.setVisibility((notificationsCount > 0) ? View.VISIBLE : View.GONE);
    }

}