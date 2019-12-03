package com3001.cw.ik00157.sportnearme.models;

public class User {

    public String uid;
    public String displayName;
    public String email;
    public String bio;
    public String phoneNumber;
    public String photoUrl;

    public User(){
    }

    public User(String uid, String displayName, String email, String phoneNumber){
        this.uid = uid;
        this.displayName = displayName;
        this.email = email;
        this.bio = "";
        this.phoneNumber = phoneNumber;
    }

    public void setPhotoUrl(String photoUrl){
        this.photoUrl = photoUrl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }
}
