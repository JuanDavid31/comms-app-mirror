package com.upstart13.legba.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.upstart13.legba.R;
import com.upstart13.legba.data.dto.ChannelElement;

import java.util.ArrayList;
import java.util.List;

public class ChannelElementsRecyclerViewAdapter extends ListAdapter<ChannelElement, ChannelElementsRecyclerViewAdapter.ItemViewHolder> {

    private List<ChannelElement> subChannelsAndMembers = new ArrayList();
    private Fragment fragment;

    public void setSubChannelsAndMembers(List<ChannelElement> subChannelsAndMembers){
        this.subChannelsAndMembers = subChannelsAndMembers;
        notifyDataSetChanged();
    }

    public ChannelElementsRecyclerViewAdapter(@NonNull DiffUtil.ItemCallback<ChannelElement> diffCallback, Fragment fragment) {
        super(diffCallback);
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.member_item, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        ChannelElement currentElement = subChannelsAndMembers.get(position);

        if(currentElement.type.equals("subchannel"))return;

        //Is there a better way to do this?
        String[] names = currentElement.name.split(" ");
        String name = "";
        for(int i = 0; (i < names.length) && (i < 2); i++){
            name += Character.toUpperCase(names[i].charAt(0));
        }

        holder.membersCaps.setText(name);
        holder.memberName.setText(currentElement.name);
        holder.memberNickName.setText(currentElement.nickName);
        holder.memberNumber.setText(currentElement.number);

        /*if(currentElement.range != null){//The member is a primary range
            holder.memberCapsBox.setBackground(fragment.getResources().getDrawable(R.drawable.primary_member_caps_box_shape));
            holder.memberNumber.setTextColor(fragment.getResources().getColor(R.color.orange));
            holder.memberPrimaryType.setVisibility(View.VISIBLE);
        }*/

        /*holder.channels.removeAllViews();*/

        /*addChannelsToView(currentElement.channels, holder.channels);
        addRemainingChannelsText(currentElement.channels, holder.channels);*/
    }

    @Override
    public int getItemCount() {
        return subChannelsAndMembers.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {

        private View root;
        private View memberCapsBox;
        private TextView membersCaps;
        private TextView memberName;
        private TextView memberNickName;
        private TextView memberNumber;
        private View memberPrimaryType;
        //private LinearLayout channels;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            this.root = itemView;
            memberCapsBox = itemView.findViewById(R.id.member_caps_layout);
            membersCaps = itemView.findViewById(R.id.member_caps_text);
            memberName = itemView.findViewById(R.id.member_name_text);
            memberNickName = itemView.findViewById(R.id.member_nickname_text);
            memberNumber = itemView.findViewById(R.id.member_number_text);
            memberPrimaryType = itemView.findViewById(R.id.member_primary_type_text);
            //channels = itemView.findViewById(R.id.channels_list_view);
        }
    }

    static class AdapterDiffCallback extends DiffUtil.ItemCallback<ChannelElement> {

        @Override
        public boolean areItemsTheSame(@NonNull ChannelElement oldItem, @NonNull ChannelElement newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull ChannelElement oldItem, @NonNull ChannelElement newItem) {
            return oldItem.toString().equals(newItem.toString());
        }
    }
}