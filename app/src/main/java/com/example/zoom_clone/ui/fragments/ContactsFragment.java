package com.example.zoom_clone.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.zoom_clone.adapters.ContactAdapter;
import com.example.zoom_clone.data.FirebaseManager;
import com.example.zoom_clone.databinding.FragmentContactsBinding;
import com.example.zoom_clone.models.MeetUser;
import com.example.zoom_clone.ui.conference.VideoConferenceActivity;
import com.example.zoom_clone.ui.contacts.ChooseContactActivity;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ContactsFragment extends Fragment implements ContactAdapter.OnContactClickListener {

    private FragmentContactsBinding binding;
    private FirebaseManager firebaseManager;
    private ContactAdapter adapter;
    private List<MeetUser> allContacts = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentContactsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseManager = FirebaseManager.getInstance();
        adapter = new ContactAdapter(this);
        binding.rvContacts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvContacts.setAdapter(adapter);

        binding.swipeRefreshContacts.setOnRefreshListener(this::loadContacts);

        binding.fabAddContact.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ChooseContactActivity.class));
        });

        binding.etSearchContact.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterContacts(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadContacts();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadContacts();
    }

    private void loadContacts() {
        if (firebaseManager.getContactsQuery() == null) return;
        binding.swipeRefreshContacts.setRefreshing(true);

        firebaseManager.getContactsQuery().get().addOnSuccessListener(snapshots -> {
            if (!isAdded()) return;
            binding.swipeRefreshContacts.setRefreshing(false);
            allContacts.clear();
            for (QueryDocumentSnapshot doc : snapshots) {
                allContacts.add(doc.toObject(MeetUser.class));
            }
            adapter.setContacts(allContacts);
            binding.tvNoContacts.setVisibility(allContacts.isEmpty() ? View.VISIBLE : View.GONE);
        }).addOnFailureListener(e -> {
            if (!isAdded()) return;
            binding.swipeRefreshContacts.setRefreshing(false);
        });
    }

    private void filterContacts(String query) {
        if (query.trim().isEmpty()) {
            adapter.setContacts(allContacts);
            return;
        }
        List<MeetUser> filtered = new ArrayList<>();
        for (MeetUser u : allContacts) {
            if (u.getName().toLowerCase().contains(query.toLowerCase()) ||
                u.getEmail().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(u);
            }
        }
        adapter.setContacts(filtered);
    }

    @Override
    public void onCallClick(MeetUser contact) {
        MeetUser me = firebaseManager.getCurUser();
        String myId = me != null ? me.getId() : "user_" + System.currentTimeMillis();
        String myName = me != null ? me.getName() : "Guest";

        Intent intent = new Intent(getActivity(), VideoConferenceActivity.class);
        intent.putExtra("conferenceID", contact.getMeetingId());
        intent.putExtra("userID", myId);
        intent.putExtra("userName", myName);
        startActivity(intent);
    }

    @Override
    public void onContactClick(MeetUser contact) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Contact")
                .setMessage("Remove " + contact.getName() + " from your contacts?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    firebaseManager.deleteContact(contact.getId()).addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Contact removed", Toast.LENGTH_SHORT).show();
                        loadContacts();
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
