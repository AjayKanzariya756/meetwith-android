package com.example.zoom_clone.models;

import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MeetUser implements Serializable {
    private String id;
    private String name;
    private String email;
    private String image;
    
    @PropertyName("created_at")
    private String createdAt;
    
    private String method;
    private String meetingId;
    private boolean isAudioConnect;
    private boolean isSpeakerOn;
    private boolean isVideoOn;

    public MeetUser() {
        // Required empty constructor for Firestore
    }

    public MeetUser(String id, String name, String email, String image, String createdAt,
                    String method, String meetingId, boolean isAudioConnect,
                    boolean isSpeakerOn, boolean isVideoOn) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.image = image;
        this.createdAt = createdAt;
        this.method = method;
        this.meetingId = meetingId;
        this.isAudioConnect = isAudioConnect;
        this.isSpeakerOn = isSpeakerOn;
        this.isVideoOn = isVideoOn;
    }

    public String getId() { return id != null ? id : ""; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name != null ? name : ""; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email != null ? email : ""; }
    public void setEmail(String email) { this.email = email; }

    public String getImage() { return image != null ? image : ""; }
    public void setImage(String image) { this.image = image; }

    @PropertyName("created_at")
    public String getCreatedAt() { return createdAt != null ? createdAt : ""; }
    @PropertyName("created_at")
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getMethod() { return method != null ? method : ""; }
    public void setMethod(String method) { this.method = method; }

    public String getMeetingId() { return meetingId != null ? meetingId : ""; }
    public void setMeetingId(String meetingId) { this.meetingId = meetingId; }

    public boolean isAudioConnect() { return isAudioConnect; }
    public void setAudioConnect(boolean audioConnect) { isAudioConnect = audioConnect; }

    public boolean isSpeakerOn() { return isSpeakerOn; }
    public void setSpeakerOn(boolean speakerOn) { isSpeakerOn = speakerOn; }

    public boolean isVideoOn() { return isVideoOn; }
    public void setVideoOn(boolean videoOn) { isVideoOn = videoOn; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("email", email);
        map.put("image", image);
        map.put("created_at", createdAt);
        map.put("method", method);
        map.put("meetingId", meetingId);
        map.put("isAudioConnect", isAudioConnect);
        map.put("isSpeakerOn", isSpeakerOn);
        map.put("isVideoOn", isVideoOn);
        return map;
    }
}
