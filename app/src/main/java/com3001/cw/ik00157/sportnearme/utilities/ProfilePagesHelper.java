package com3001.cw.ik00157.sportnearme.utilities;

import android.support.annotation.NonNull;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class ProfilePagesHelper {

    private DatabaseReference usersFollowersRef;
    private DatabaseReference usersFollowingRef;
    private TextView tvNrFollowers;
    private TextView tvNrFollowing;

    public ProfilePagesHelper(DatabaseReference usersFollowersRef,
                              DatabaseReference usersFollowingRef,
                              TextView tvNrFollowers,
                              TextView tvNrFollowing){

        this.usersFollowingRef = usersFollowingRef;
        this.usersFollowersRef = usersFollowersRef;
        this.tvNrFollowers = tvNrFollowers;
        this.tvNrFollowing = tvNrFollowing;
    }

    public void setFollowSectionNumbers(){
        setNrFollowersTv();
        setNrFollowingTv();
    }

    private void setNrFollowersTv(){
        usersFollowersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                onNrDiffUsersFollowersChanged(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void onNrDiffUsersFollowersChanged(@NonNull DataSnapshot dataSnapshot){
        if(dataSnapshot.exists()){
            String nrFollowers = Long.toString(dataSnapshot.getChildrenCount());
            tvNrFollowers.setText(nrFollowers);
        } else{
            tvNrFollowers.setText("0");
        }
    }

    private void setNrFollowingTv(){
        usersFollowingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                onNrDiffUserIsFollowingChanged(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void onNrDiffUserIsFollowingChanged(@NonNull DataSnapshot dataSnapshot){
        if(dataSnapshot.exists()){
            String nrFollowing = Long.toString(dataSnapshot.getChildrenCount());
            tvNrFollowing.setText(nrFollowing);
        } else{
            tvNrFollowing.setText("0");
        }
    }

}
