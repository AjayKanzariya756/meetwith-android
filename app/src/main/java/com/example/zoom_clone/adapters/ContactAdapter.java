package com.example.zoom_clone.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.zoom_clone.R;
import com.example.zoom_clone.models.MeetUser;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    public interface OnContactClickListener {
        void onCallClick(MeetUser contact);
        void onContactClick(MeetUser contact);
    }

    private List<MeetUser> contactList = new ArrayList<>();
    private final OnContactClickListener listener;

    public ContactAdapter(OnContactClickListener listener) {
        this.listener = listener;
    }

    public void setContacts(List<MeetUser> contacts) {
        this.contactList = contacts != null ? contacts : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        MeetUser contact = contactList.get(position);
        holder.bind(contact, listener);
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView ivAvatar;
        private final TextView tvName;
        private final TextView tvEmail;
        private final ImageButton btnAction;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivContactAvatar);
            tvName = itemView.findViewById(R.id.tvContactName);
            tvEmail = itemView.findViewById(R.id.tvContactEmail);
            btnAction = itemView.findViewById(R.id.btnAction);
        }

        public void bind(MeetUser contact, OnContactClickListener listener) {
            tvName.setText(contact.getName());
            tvEmail.setText(contact.getEmail());

            if (contact.getImage() != null && !contact.getImage().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(contact.getImage())
                        .placeholder(R.drawable.student)
                        .error(R.drawable.student)
                        .into(ivAvatar);
            } else {
                ivAvatar.setImageResource(R.drawable.student);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onContactClick(contact);
            });

            btnAction.setOnClickListener(v -> {
                if (listener != null) listener.onCallClick(contact);
            });
        }
    }
}
