package com.rallytac.engageandroid.legba.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.rallytac.engageandroid.R;
import com.rallytac.engageandroid.legba.data.dto.Member;


import java.util.ArrayList;
import java.util.List;

import static com.rallytac.engageandroid.legba.util.DimUtils.convertDpToPx;

public class UsersRecyclerViewAdapter extends ListAdapter<Member, UsersRecyclerViewAdapter.ChannelElementViewHolder> {

    private List<Member> members = new ArrayList();

    public void setMembers(List<Member> members) {
        this.members = members;
        notifyDataSetChanged();
    }

    public UsersRecyclerViewAdapter(@NonNull DiffUtil.ItemCallback<Member> diffCallback, Fragment fragment) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public ChannelElementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.member_info_item, parent, false);
        return new MemberViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ChannelElementViewHolder holder, int position) {
        Member member = members.get(position);
        MemberViewHolder memberViewHolder = (MemberViewHolder) holder;

        String name = getFirstLetterCapsFrom(member.getName());

        memberViewHolder.membersCaps.setText(name);
        memberViewHolder.name.setText(member.getName());
        memberViewHolder.memberNickName.setText(member.getNickName());
        memberViewHolder.memberNumber.setText(member.getNumber());
    }

    private String getFirstLetterCapsFrom(String name) {
        String[] names;
        if (name.contains(" ")) {
            names = name.split(" ");
        } else {
            return name.isEmpty() ? "" : String.valueOf(name.toUpperCase().charAt(0));
        }

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
        return members.size();
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

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            membersCaps = itemView.findViewById(R.id.member_caps_text);
            name = itemView.findViewById(R.id.member_name_text);
            memberNickName = itemView.findViewById(R.id.member_nickname_text);
            memberNumber = itemView.findViewById(R.id.member_number_text);
        }
    }

    static class AdapterDiffCallback extends DiffUtil.ItemCallback<Member> {

        @Override
        public boolean areItemsTheSame(@NonNull Member oldItem, @NonNull Member newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Member oldItem, @NonNull Member newItem) {
            return oldItem.equals(newItem);
        }
    }
}