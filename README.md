# 🤝 MeetWith — Native Android (Java)

This is the **Pure Java** Native Android port of the **MeetWith** Flutter video conferencing app. It features full support for Firebase Authentication, Cloud Firestore real-time database, Firebase Storage, and ZegoCloud HD Video Conferencing.

---

## 🏗️ Project Architecture & Components

- **Language**: 100% Java (Java 8 / Java 17 compatibility)
- **Architecture**: MVVM / Clean Android Architecture with ViewBinding
- **Backend & Cloud**:
  - **Firebase Authentication**: Email/Password and Google Sign-In (`com.google.android.gms:play-services-auth`)
  - **Cloud Firestore**: Real-time storage for user profiles, meetings, and contacts
  - **Firebase Storage**: Avatar picture uploads
  - **ZegoCloud UIKit**: Prebuilt Video Conference SDK (`com.github.ZEGOCLOUD:zego_uikit_prebuilt_video_conference_android`)
- **Package Name**: `com.example.zoom_clone` (Pre-configured with `google-services.json`)

---

## 📂 Source Code Structure

```
app/src/main/
├── AndroidManifest.xml
├── java/com/example/zoom_clone/
│   ├── adapters/
│   │   ├── ContactAdapter.java           # Contacts RecyclerView adapter
│   │   └── MeetingAdapter.java           # Meeting history & recent meetings adapter
│   ├── data/
│   │   └── FirebaseManager.java          # Centralized Firebase Auth, Firestore, Storage
│   ├── models/
│   │   ├── MeetUser.java                 # User profile POJO
│   │   └── Meeting.java                  # Meeting room POJO
│   ├── ui/
│   │   ├── SplashActivity.java           # Initial auth check & splash
│   │   ├── MainActivity.java             # Host for bottom navigation
│   │   ├── auth/
│   │   │   ├── LoginActivity.java        # Email/Password + Google Sign-In
│   │   │   └── SignUpActivity.java       # User registration
│   │   ├── fragments/
│   │   │   ├── HomeFragment.java         # 4 action buttons (New, Join, Schedule, Share)
│   │   │   ├── HistoryFragment.java      # Tabs for created & joined meetings
│   │   │   ├── ContactsFragment.java     # Searchable contacts list + add button
│   │   │   └── SettingsFragment.java     # User profile card & logout
│   │   ├── meetings/
│   │   │   ├── NewMeetingActivity.java   # Start instant meeting room
│   │   │   ├── JoinMeetingActivity.java  # Join with Meeting ID & password
│   │   │   └── ScheduleMeetingActivity.java # Schedule meeting with Date/Time picker
│   │   ├── conference/
│   │   │   └── VideoConferenceActivity.java # Full ZegoCloud video room
│   │   ├── profile/
│   │   │   └── ProfileActivity.java      # Edit name & upload avatar
│   │   └── contacts/
│   │       └── ChooseContactActivity.java # Browse & add users to contacts
│   └── utils/
│       ├── DateTimeUtils.java            # Date/time formatting helpers
│       └── Secrets.java                  # ZegoCloud AppID & AppSign keys
└── res/
    ├── drawable/                         # Vectors, shapes, app icons
    ├── layout/                           # XML Layouts with ViewBinding
    ├── menu/                             # Bottom navigation menu
    └── values/                           # Colors, strings, themes
```

---

## 🚀 How to Open and Run in Android Studio

1. **Launch Android Studio**.
2. Click **Open** (or **File > Open**) and select:
   ```
   C:\Users\ajayk\.gemini\antigravity\scratch\meetwith-android-java
   ```
3. Android Studio will automatically recognize the project and run **Gradle Sync**.
4. Once sync completes, connect an **Android phone** via USB (or launch an **Android Emulator**).
5. Click the green **Run ▶** button (or press `Shift + F10`) to build and launch the app!

---

## ⚙️ Configuration Notes
- **Firebase**: The pre-configured `google-services.json` is located in [`app/google-services.json`](file:///C:/Users/ajayk/.gemini/antigravity/scratch/meetwith-android-java/app/google-services.json).
- **ZegoCloud Credentials**: Configured in [`Secrets.java`](file:///C:/Users/ajayk/.gemini/antigravity/scratch/meetwith-android-java/app/src/main/java/com/example/zoom_clone/utils/Secrets.java). You can replace `APP_ID` and `APP_SIGN` with your own credentials anytime.
