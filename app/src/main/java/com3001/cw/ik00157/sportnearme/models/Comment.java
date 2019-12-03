package com3001.cw.ik00157.sportnearme.models;

public class Comment {

    private String timeCreated;
    private String text;
    private String creatorDisplayName;
    private String photoUrl;
    private String uid;

    public Comment(){
    }

    public String getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(String timeCreated) {
        this.timeCreated = timeCreated;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCreatorDisplayName() {
        return creatorDisplayName;
    }

    public void setCreatorDisplayName(String creatorDisplayName) {
        this.creatorDisplayName = creatorDisplayName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
