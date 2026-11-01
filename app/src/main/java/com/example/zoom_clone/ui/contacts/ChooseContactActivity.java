package com.example.zoom_clone.ui.contacts;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.zoom_clone.adapters.ContactAdapter;
import com.example.zoom_clone.data.FirebaseManager;
import com.example.zoom_clone.databinding.ActivityChooseContactBinding;
import com.example.zoom_clone.models.MeetUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ChooseContactActivity extends AppCompatActivity implements ContactAdapter.OnContactClickListener {

    private ActivityChooseContactBinding binding;
    private FirebaseManager firebaseManager;
    private ContactAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChooseContactBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseManager = FirebaseManager.getInstance();
        adapter = new ContactAdapter(this);
        binding.rvAllUsers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAllUsers.setAdapter(adapter);

        loadAllUsers();
    }

    private void loadAllUsers() {
        firebaseManager.getAllUsersQuery().get().addOnSuccessListener(snapshots -> {
            List<MeetUser> users = new ArrayList<>();
            MeetUser cur = firebaseManager.getCurUser();
            String myId = cur != null ? cur.getId() : "";

            for (QueryDocumentSnapshot doc : snapshots) {
                MeetUser u = doc.toObject(MeetUser.class);
                if (!u.getId().equals(myId)) {
                    users.add(u);
                }
            }
            adapter.setContacts(users);
        });
    }

    @Override
    public void onCallClick(MeetUser contact) {
        onContactClick(contact);
    }

    @Override
    public void onContactClick(MeetUser contact) {
        firebaseManager.addContact(contact).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, contact.getName() + " added to contacts!", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to add contact: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
