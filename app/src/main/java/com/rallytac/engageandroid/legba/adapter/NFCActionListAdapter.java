package com.rallytac.engageandroid.legba.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.rallytac.engageandroid.R;
import com.rallytac.engageandroid.legba.data.dto.NFCAction;
import com.rallytac.engageandroid.legba.fragment.NFCActionsFragment;

import java.util.List;

import static com.rallytac.engageandroid.legba.util.RUtils.getImageNFCActionResource;

public class NFCActionListAdapter extends RecyclerView.Adapter<NFCActionListAdapter.NFCActionViewHolder>  {

    private NFCActionsFragment fragment;
    private Context context;
    private List<NFCAction> nfcActions;

    public NFCActionListAdapter(List<NFCAction> nfcActions, NFCActionsFragment fragment, Context context) {
        this.fragment = fragment;
        this.context = context;
        this.nfcActions = nfcActions;
    }

    public List<NFCAction> getNFCActions() {
        return nfcActions;
    }

    public void setNfcActions(List<NFCAction> nfcActions) {
        this.nfcActions = nfcActions;
    }

    @NonNull
    @Override
    public NFCActionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.nfc_action_item, parent, false);
        return new NFCActionViewHolder(itemView, context);
    }

    @Override
    public void onBindViewHolder(@NonNull NFCActionViewHolder holder, int position) {
        NFCAction nfcAction = nfcActions.get(position);
        holder.nfcActionImg.setImageResource(getImageNFCActionResource(nfcAction.getImage()));
        holder.nfcActionName.setText(nfcAction.getName());
        holder.nfcActionDescription.setText(nfcAction.getDescription());
    }

    @Override
    public int getItemCount() {
        return nfcActions.size();
    }

    public class NFCActionViewHolder extends RecyclerView.ViewHolder {

        private RoundedImageView nfcActionImg;
        private TextView nfcActionName;
        private TextView nfcActionDescription;
        private Context context;

        public NFCActionViewHolder(@NonNull View itemView, Context context) {
            super(itemView);

            this.nfcActionImg = itemView.findViewById(R.id.nfc_action_img);
            this.nfcActionName = itemView.findViewById(R.id.nfc_action_name);
            this.nfcActionDescription = itemView.findViewById(R.id.nfc_action_description);
            this.context = context;
        }
    }
}
