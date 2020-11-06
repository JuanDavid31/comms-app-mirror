package com.rallytac.engageandroid.legba.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.rallytac.engageandroid.R;
import com.rallytac.engageandroid.legba.data.dto.Member;

import java.util.ArrayList;
import java.util.List;

public class MembersRecyclerViewAdapter extends ListAdapter<Member, MembersRecyclerViewAdapter.MemberViewHolder> {

    private List<Member> members = new ArrayList();
    private Fragment fragment;

    public void setMembers(List<Member> members) {
        this.members = members;
        notifyDataSetChanged();
    }

    public MembersRecyclerViewAdapter(@NonNull DiffUtil.ItemCallback<Member> diffCallback, Fragment fragment) {
        super(diffCallback);
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public MembersRecyclerViewAdapter.MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.member_item, parent, false);
        return new MembersRecyclerViewAdapter.MemberViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MembersRecyclerViewAdapter.MemberViewHolder holder, int position) {
        Member currentMember = members.get(position);

        String name = getFirstLetterCapsFrom(currentMember.getName());

        holder.membersCaps.setText(name);
        holder.name.setText(currentMember.getName());
        holder.memberNickName.setText(currentMember.getNickName());
        holder.memberNumber.setText(currentMember.getNumber());
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

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class MemberViewHolder extends ChannelElementsRecyclerViewAdapter.ChannelElementViewHolder {

        private TextView name;
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
            return oldItem.getMemberId() == newItem.getMemberId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Member oldItem, @NonNull Member newItem) {
            return oldItem.equals(newItem);
        }
    }
}
