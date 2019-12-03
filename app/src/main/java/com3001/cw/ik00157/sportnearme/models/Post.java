package com3001.cw.ik00157.sportnearme.models;

public class Post {

    private String uid;
    private String body;
    private String creatorDisplayName;
    private String photoUrl;
    private String timeCreated;
    private String timeFromFixedDate;
    private String latitude;
    private String longitude;
    private String sport;

    public Post(){

    }

    public Post(String uid, String body, String creatorDisplayName, String photoUrl){
        this.uid = uid;
        this.body = body;
        this.creatorDisplayName = creatorDisplayName;
        this.photoUrl = photoUrl;
    }

    public String getUid(){
        return this.uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
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

    public String getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(String timeCreated) {
        this.timeCreated = timeCreated;
    }

    public String getTimeFromFixedDate() {
        return timeFromFixedDate;
    }

    public void setTimeFromFixedDate(String timeFromFixedDate) {
        this.timeFromFixedDate = timeFromFixedDate;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getSport() {
        return sport;
    }

    public void setSport(String sport) {
        this.sport = sport;
    }
}
