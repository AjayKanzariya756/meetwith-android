package com.example.zoom_clone.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Meeting implements Serializable {
    private String name;
    private String meetingId;
    private String hostName;
    private String hostEmail;
    private String date;
    private String password;

    public Meeting() {
        // Required empty constructor for Firestore
    }

    public Meeting(String name, String meetingId, String hostName, String hostEmail, String date, String password) {
        this.name = name;
        this.meetingId = meetingId;
        this.hostName = hostName;
        this.hostEmail = hostEmail;
        this.date = date;
        this.password = password;
    }

    public String getName() { return name != null ? name : ""; }
    public void setName(String name) { this.name = name; }

    public String getMeetingId() { return meetingId != null ? meetingId : ""; }
    public void setMeetingId(String meetingId) { this.meetingId = meetingId; }

    public String getHostName() { return hostName != null ? hostName : ""; }
    public void setHostName(String hostName) { this.hostName = hostName; }

    public String getHostEmail() { return hostEmail != null ? hostEmail : ""; }
    public void setHostEmail(String hostEmail) { this.hostEmail = hostEmail; }

    public String getDate() { return date != null ? date : ""; }
    public void setDate(String date) { this.date = date; }

    public String getPassword() { return password != null ? password : ""; }
    public void setPassword(String password) { this.password = password; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("meetingId", meetingId);
        map.put("hostName", hostName);
        map.put("hostEmail", hostEmail);
        map.put("date", date);
        map.put("password", password);
        return map;
    }
}
