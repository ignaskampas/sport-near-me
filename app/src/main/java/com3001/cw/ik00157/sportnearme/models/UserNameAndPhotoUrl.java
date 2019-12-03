package com3001.cw.ik00157.sportnearme.models;

public class UserNameAndPhotoUrl {

    String displayName;
    String photoUrl;

    public UserNameAndPhotoUrl(){
    }

    public UserNameAndPhotoUrl(String displayName, String photoUrl) {
        this.displayName = displayName;
        this.photoUrl = photoUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
