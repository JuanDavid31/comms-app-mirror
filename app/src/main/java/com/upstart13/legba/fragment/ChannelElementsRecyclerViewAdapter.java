package com.upstart13.legba.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
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
import com.upstart13.legba.data.dto.ChannelElement;
import com.upstart13.legba.data.dto.Member;
import com.upstart13.legba.data.dto.Subchannel;
import com.upstart13.legba.util.DimUtils;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static com.upstart13.legba.util.DimUtils.convertDpToPx;
import static com.upstart13.legba.util.RUtils.getImageResource;

public class ChannelElementsRecyclerViewAdapter extends ListAdapter<ChannelElement, ChannelElementsRecyclerViewAdapter.ChannelElementViewHolder> {

    private List<ChannelElement> channelElements = new ArrayList();
    private Fragment fragment;

    public void setChannelElements(List<ChannelElement> channelElements) {
        this.channelElements = channelElements;
        notifyDataSetChanged();
    }

    public ChannelElementsRecyclerViewAdapter(@NonNull DiffUtil.ItemCallback<ChannelElement> diffCallback, Fragment fragment) {
        super(diffCallback);
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public ChannelElementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MEMBER_ID) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.member_info_item, parent, false);
            return new MemberViewHolder(itemView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.subchannel_item, parent, false);
            return new SubchannelViewHolder(itemView);
        }
    }

    private final int MEMBER_ID = 0;
    private final int CHANNEL_ID = 1;

    @Override
    public int getItemViewType(int position) {
        ChannelElement currentElement = channelElements.get(position);
        Timber.i(currentElement.toString());
        if (currentElement instanceof Member) {
            return MEMBER_ID;
        } else {
            return CHANNEL_ID;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ChannelElementViewHolder holder, int position) {
        ChannelElement element = channelElements.get(position);
        if (element instanceof Member) {
            Member member = (Member) element;
            MemberViewHolder memberViewHolder = (MemberViewHolder) holder;

            String name = getFirstLetterCapsFrom(member.name);

            memberViewHolder.membersCaps.setText(name);
            memberViewHolder.name.setText(member.name);
            memberViewHolder.memberNickName.setText(member.nickName);
            memberViewHolder.memberNumber.setText(member.number);
            memberViewHolder.micImage.setImageResource(getStateImage(member.state));
            memberViewHolder.requestPendingText.setVisibility(member.state == Member.RequestType.PENDING ? View.VISIBLE : View.INVISIBLE);
        } else if(element instanceof Subchannel){
            Subchannel subChannel = (Subchannel) element;
            SubchannelViewHolder subchannelViewHolder = (SubchannelViewHolder) holder;

            subchannelViewHolder.root.setOnClickListener(view -> NavHostFragment.findNavController(fragment)
                    .navigate(ChannelFragmentDirections.actionChannelFragmentToSubchannelFragment(subChannel)));

            subchannelViewHolder.subchannelImage.setImageResource(getImageResource(subChannel.image));
            subchannelViewHolder.name.setText(subChannel.name);

            subchannelViewHolder.membersLayout.removeAllViews();
            for (int i = 0; (i < 6) && (i < subChannel.members.size()); i++) {
                FrameLayout newMember = (FrameLayout) LayoutInflater.from(fragment.getContext()).inflate(R.layout.member_caps_layout, null);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(DimUtils.convertDpToPx(fragment, 26.4), ViewGroup.LayoutParams.MATCH_PARENT);
                layoutParams.setMarginEnd(DimUtils.convertDpToPx(fragment, 5.3));
                newMember.setLayoutParams(layoutParams);
                String memberCaps = getFirstLetterCapsFrom(subChannel.members.get(i).name);
                ((TextView)newMember.findViewById(R.id.member_caps_text)).setText(memberCaps);
                subchannelViewHolder.membersLayout.addView(newMember);
            }

            int remainingChannels = subChannel.members.size() - 6;
            if (remainingChannels <= 0) { return; }
            TextView remainingChannelsView = (TextView) LayoutInflater.from(fragment.getContext()).inflate(R.layout.remaining_channels_number, null);
            remainingChannelsView.setLayoutParams(new LinearLayout.LayoutParams(convertDpToPx(fragment, 26.8), ViewGroup.LayoutParams.MATCH_PARENT));
            remainingChannelsView.setText(String.format("+%s", remainingChannels));
            subchannelViewHolder.membersLayout.addView(remainingChannelsView);
        }

    }

    private String getFirstLetterCapsFrom(String name) {
        //Is there a better way to do this?
        String[] names = name.split(" ");
        String result = "";
        for (int i = 0; (i < names.length) && (i < 2); i++) {
            result += Character.toUpperCase(names[i].charAt(0));
        }
        return result;
    }

    public int getStateImage(Member.RequestType state) {
        if (state == null) return android.R.color.transparent;
        switch (state) {
            case ADDED:
                return R.drawable.ic_mic;
            case NOT_ADDED:
                return R.mipmap.ic_mic_plus;
            case PENDING:
                return R.drawable.ic_mic_pending_request;
            default:
                return android.R.color.transparent;
        }
    }

    @Override
    public int getItemCount() {
        return channelElements.size();
    }

    static class ChannelElementViewHolder extends RecyclerView.ViewHolder {

        protected TextView name;

        public ChannelElementViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    static class MemberViewHolder extends ChannelElementViewHolder {

        private TextView membersCaps;
        private TextView memberNickName;
        private TextView memberNumber;
        private TextView requestPendingText;
        private ImageButton micImage;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            membersCaps = itemView.findViewById(R.id.member_caps_text);
            name = itemView.findViewById(R.id.member_name_text);
            memberNickName = itemView.findViewById(R.id.member_nickname_text);
            memberNumber = itemView.findViewById(R.id.member_number_text);
            requestPendingText = itemView.findViewById(R.id.request_pending_text);
            micImage = itemView.findViewById(R.id.member_state_image);
        }
    }

    static class SubchannelViewHolder extends ChannelElementViewHolder {

        private View root;
        private RoundedImageView subchannelImage;
        private LinearLayout membersLayout;

        public SubchannelViewHolder(@NonNull View itemView) {
            super(itemView);
            root = itemView;
            subchannelImage = itemView.findViewById(R.id.subchannel_image);
            name = itemView.findViewById(R.id.subchannel_name_text);
            membersLayout = itemView.findViewById(R.id.channels_list_view);
        }
    }

    static class AdapterDiffCallback extends DiffUtil.ItemCallback<ChannelElement> {

        @Override
        public boolean areItemsTheSame(@NonNull ChannelElement oldItem, @NonNull ChannelElement newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull ChannelElement oldItem, @NonNull ChannelElement newItem) {
            return oldItem.equals(newItem);
        }
    }
}