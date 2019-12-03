package com3001.cw.ik00157.sportnearme.models;

public class UsersLastLocation {

    private static double latitude = 10000;
    private static double longitude = 10000;
    private static UsersLastLocation usersLocation = null;

    private UsersLastLocation(){
    }

    public static UsersLastLocation getInstance(){
        if(usersLocation == null){
            usersLocation = new UsersLastLocation();
        }
        return usersLocation;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean locationHasBeenRetrieved(){
        if(latitude != 10000 && longitude != 10000){
            return true;
        } else{
            return false;
        }
    }

}
