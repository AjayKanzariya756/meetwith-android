package com.example.zoom_clone.data;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.zoom_clone.models.MeetUser;
import com.example.zoom_clone.models.Meeting;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseManager {
    private static final String TAG = "FirebaseManager";
    private static FirebaseManager instance;

    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;
    private final FirebaseStorage storage;

    private MeetUser curUser;

    private FirebaseManager() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    public FirebaseAuth getAuth() { return auth; }
    public FirebaseFirestore getFirestore() { return firestore; }
    public FirebaseStorage getStorage() { return storage; }
    public FirebaseUser getCurrentFirebaseUser() { return auth.getCurrentUser(); }
    public MeetUser getCurUser() { return curUser; }
    public void setCurUser(MeetUser user) { this.curUser = user; }

    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public void signOut() {
        auth.signOut();
        curUser = null;
    }

    // Generate unique 10-12 digit meeting ID from email
    public static String generateUniqueId(String email) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(email.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            long id = Long.parseLong(hexString.toString(), 16);
            String idStr = String.valueOf(id);
            while (idStr.length() < 10) {
                idStr = "0" + idStr;
            }
            return idStr;
        } catch (Exception e) {
            return String.valueOf(System.currentTimeMillis()).substring(3);
        }
    }

    // Sign in with email & password
    public Task<com.google.firebase.auth.AuthResult> signIn(String email, String password) {
        return auth.signInWithEmailAndPassword(email, password);
    }

    // Sign up with email & password
    public Task<Void> signUp(String email, String password, String name) {
        return auth.createUserWithEmailAndPassword(email, password).continueWithTask(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                throw task.getException();
            }
            FirebaseUser user = task.getResult().getUser();
            String uid = user.getUid();
            String time = String.valueOf(System.currentTimeMillis());
            MeetUser meetUser = new MeetUser(
                    uid,
                    name != null && !name.isEmpty() ? name : email,
                    email,
                    "",
                    time,
                    "Email-Password",
                    generateUniqueId(email),
                    true,
                    true,
                    true
            );
            curUser = meetUser;
            return firestore.collection("users").document(uid).set(meetUser.toMap());
        });
    }

    // Google Sign-In Credential handler
    public Task<Void> signInWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        return auth.signInWithCredential(credential).continueWithTask(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                throw task.getException();
            }
            FirebaseUser user = task.getResult().getUser();
            String uid = user.getUid();

            return firestore.collection("users").document(uid).get().continueWithTask(docTask -> {
                DocumentSnapshot doc = docTask.getResult();
                if (doc != null && doc.exists()) {
                    curUser = doc.toObject(MeetUser.class);
                    return Tasks.forResult(null);
                } else {
                    String time = String.valueOf(System.currentTimeMillis());
                    String email = user.getEmail() != null ? user.getEmail() : "";
                    String name = user.getDisplayName() != null ? user.getDisplayName() : email;
                    String photo = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "";
                    MeetUser meetUser = new MeetUser(
                            uid,
                            name,
                            email,
                            photo,
                            time,
                            "Google",
                            generateUniqueId(email),
                            true,
                            true,
                            true
                    );
                    curUser = meetUser;
                    return firestore.collection("users").document(uid).set(meetUser.toMap());
                }
            });
        });
    }

    // Fetch current user data from Firestore
    public Task<MeetUser> fetchSelfData() {
        if (auth.getCurrentUser() == null) {
            return Tasks.forException(new IllegalStateException("No user logged in"));
        }
        String uid = auth.getCurrentUser().getUid();
        return firestore.collection("users").document(uid).get().continueWith(task -> {
            DocumentSnapshot doc = task.getResult();
            if (doc != null && doc.exists()) {
                curUser = doc.toObject(MeetUser.class);
                return curUser;
            }
            return null;
        });
    }

    // Create instant meeting
    public Task<DocumentReference> createMeeting(String meetingName, String password) {
        if (curUser == null) throw new IllegalStateException("User not loaded");
        String time = String.valueOf(System.currentTimeMillis());
        Meeting meeting = new Meeting(
                meetingName,
                curUser.getMeetingId(),
                curUser.getName(),
                curUser.getEmail(),
                time,
                password
        );
        return firestore.collection("users").document(curUser.getId())
                .collection("your_meeting").add(meeting.toMap());
    }

    // Join meeting
    public Task<Meeting> fetchMeetingByHost(String hostId, String meetingPass) {
        return firestore.collection("users").document(hostId)
                .collection("your_meeting")
                .whereEqualTo("password", meetingPass)
                .get()
                .continueWith(task -> {
                    QuerySnapshot snapshot = task.getResult();
                    if (snapshot != null && !snapshot.isEmpty()) {
                        return snapshot.getDocuments().get(0).toObject(Meeting.class);
                    }
                    return null;
                });
    }

    public Task<MeetUser> findHostByMeetingId(String meetingId) {
        return firestore.collection("users")
                .whereEqualTo("meetingId", meetingId)
                .get()
                .continueWith(task -> {
                    QuerySnapshot snapshot = task.getResult();
                    if (snapshot != null && !snapshot.isEmpty()) {
                        return snapshot.getDocuments().get(0).toObject(MeetUser.class);
                    }
                    return null;
                });
    }

    public Task<DocumentReference> saveJoinedMeeting(Meeting meeting) {
        if (curUser == null) throw new IllegalStateException("User not loaded");
        return firestore.collection("users").document(curUser.getId())
                .collection("Joined_Meeting").add(meeting.toMap());
    }

    // Schedule meeting
    public Task<DocumentReference> createUpcomingMeeting(String name, String password, String scheduledTime) {
        if (curUser == null) throw new IllegalStateException("User not loaded");
        Meeting meeting = new Meeting(
                name,
                curUser.getMeetingId(),
                curUser.getName(),
                curUser.getEmail(),
                scheduledTime,
                password
        );
        return firestore.collection("users").document(curUser.getId())
                .collection("your_meeting").add(meeting.toMap());
    }

    // Queries for lists
    public Query getCreatedMeetingsQuery() {
        if (curUser == null) return null;
        return firestore.collection("users").document(curUser.getId())
                .collection("your_meeting");
    }

    public Query getJoinedMeetingsQuery() {
        if (curUser == null) return null;
        return firestore.collection("users").document(curUser.getId())
                .collection("Joined_Meeting");
    }

    public Query getContactsQuery() {
        if (curUser == null) return null;
        return firestore.collection("users").document(curUser.getId())
                .collection("your_contacts");
    }

    public Query getAllUsersQuery() {
        return firestore.collection("users");
    }

    // Contact management
    public Task<Void> addContact(MeetUser user) {
        if (curUser == null) throw new IllegalStateException("User not loaded");
        return firestore.collection("users").document(curUser.getId())
                .collection("your_contacts").document(user.getId()).set(user.toMap());
    }

    public Task<Void> deleteContact(String contactId) {
        if (curUser == null) throw new IllegalStateException("User not loaded");
        return firestore.collection("users").document(curUser.getId())
                .collection("your_contacts").document(contactId).delete();
    }

    // Profile updates
    public Task<Void> updateUserName(String name) {
        if (curUser == null) throw new IllegalStateException("User not loaded");
        curUser.setName(name);
        return firestore.collection("users").document(curUser.getId())
                .update("name", name);
    }

    public Task<Uri> uploadProfilePicture(Uri fileUri) {
        if (curUser == null) throw new IllegalStateException("User not loaded");
        StorageReference ref = storage.getReference().child("profile_pictures/" + curUser.getId() + ".jpg");
        return ref.putFile(fileUri).continueWithTask(task -> ref.getDownloadUrl()).continueWithTask(urlTask -> {
            Uri downloadUrl = urlTask.getResult();
            curUser.setImage(downloadUrl.toString());
            return firestore.collection("users").document(curUser.getId())
                    .update("image", downloadUrl.toString())
                    .continueWith(t -> downloadUrl);
        });
    }
}
