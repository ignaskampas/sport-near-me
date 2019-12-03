package com3001.cw.ik00157.sportnearme.utilities;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeHelper {

    private final String TAG;

    public TimeHelper(final String TAG){
        this.TAG = TAG;
    }

    public String getHowLongAgoCreatedFriendlyFormat(String timeCreated){

        Long howLongAgoCreatedTimestamp = getHowLongAgoCreatedTimestamp(timeCreated);

        String result = "";
        Long howLongAgoCreated = howLongAgoCreatedTimestamp;
        int timeRounded;

        if((howLongAgoCreated = howLongAgoCreated/1000L) < 60L){
            if (String.valueOf(Math.round(howLongAgoCreated)).equals("0")){
                result = "1" + " s";
            } else {
                result = String.valueOf(Math.round(howLongAgoCreated)) + " s";
            }
        } else if((howLongAgoCreated = howLongAgoCreated/60L) < 60L){
            result = String.valueOf(Math.round(howLongAgoCreated)) + " min";
        } else if((howLongAgoCreated = howLongAgoCreated/60L) < 24L){
            result = String.valueOf(Math.round(howLongAgoCreated)) + " h";
        } else if((howLongAgoCreated = howLongAgoCreated/24L) < 7L){
            result = String.valueOf(Math.round(howLongAgoCreated)) + " d";
        } else if((howLongAgoCreated = howLongAgoCreated/7L) < 4L){
            result = String.valueOf(Math.round(howLongAgoCreated)) + " w";
        } else if((howLongAgoCreated = howLongAgoCreated/4L) < 12L) {
            timeRounded = Math.round(howLongAgoCreated);
            result = String.valueOf(timeRounded);
            if (timeRounded< 2){
                result += " month";
            } else {
                result += " months";
            }
        } else {
            howLongAgoCreated = howLongAgoCreated/12L;

            timeRounded = Math.round(Math.round(howLongAgoCreated));
            result = String.valueOf(timeRounded);
            if (howLongAgoCreated < 2){
                result += " year";
            } else if (howLongAgoCreated < 999){
                result += " years";
            } else {
                result = "";
            }
        }
        return result;
    }



    public Long getHowLongAgoCreatedTimestamp(String timeCreated){
        Log.d(TAG, "getTimestampDifference");

        Long difference = 0L;
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/London"));
        Date timeNow = c.getTime();
        sdf.format(timeNow);
        Date timestamp;
        try{
            timestamp = sdf.parse(timeCreated);
            difference = ((timeNow.getTime() - timestamp.getTime()));
        } catch(ParseException e){
            Log.e(TAG, "getTimestampDifference: " + e.getMessage());
            difference = 0L;
        }
        return difference;
    }

    private String getFixedDate(){
        return "2019-00-00T00:00:00Z";
    }

    public String getTimeFromFixedDate(String timeCreated){

        Long difference = 0L;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/London"));
        Date timestamp;
        Date fixedDate;
        try{
            timestamp = sdf.parse(timeCreated);
            fixedDate = sdf.parse(getFixedDate());
            difference = ((timestamp.getTime() - fixedDate.getTime()));
        } catch(ParseException e){
            Log.e(TAG, "getTimeFromFixedDate: " + e.getMessage());
            difference = 0L;
        }
        return String.valueOf(difference);
    }

    public String getTimestamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        // TimeZone.getDefault().getID() returns the time zone used on the phone, for example: "Europe/London"
        // I could add all data with Europe/London time, then to check how long it was ago, i could use
        // Europe/London time to do the subtractions.
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/London"));
        return sdf.format(new Date());
    }

}
