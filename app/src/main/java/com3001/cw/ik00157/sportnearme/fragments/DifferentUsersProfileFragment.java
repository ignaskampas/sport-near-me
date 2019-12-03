package com3001.cw.ik00157.sportnearme.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import com3001.cw.ik00157.sportnearme.R;
import com3001.cw.ik00157.sportnearme.activities.ConvoActivity;
import com3001.cw.ik00157.sportnearme.activities.UserNamesAndPhotosListActivity;
import com3001.cw.ik00157.sportnearme.models.User;
import com3001.cw.ik00157.sportnearme.models.UserNameAndPhotoUrl;
import com3001.cw.ik00157.sportnearme.utilities.BackstackHelper;
import com3001.cw.ik00157.sportnearme.utilities.NavigationHelper;
import com3001.cw.ik00157.sportnearme.utilities.PostRecyclerView;
import com3001.cw.ik00157.sportnearme.utilities.ProfilePagesHelper;
import de.hdodenhof.circleimageview.CircleImageView;

public class DifferentUsersProfileFragment extends Fragment implements FragmentManager.OnBackStackChangedListener{

    private static final String TAG = "DIFF_USERS_PROFILE_FRAG";

    private View profileView;
    private Button btnFollow, btnMessage;
    private CircleImageView civProfilePic;
    private TextView tvNrFollowers, tvNrFollowing, tvDisplayName, tvBio;

    private FirebaseAuth mAuth;
    private DatabaseReference database,
            followersRef, followingRef,
            diffUsersRef, diffUsersDisplayNameRef, diffUsersBioRef, diffUsersPhotoUrlRef, diffUsersFollowersRef, diffUsersFollowingRef,
            usersFollowingRef, photoUrlRef,
            contactsRef, usersContactsRef, diffUsersContactsRef;
    private boolean processingFollowAction, processingAddToUsersFollowing, processingRemoveFromUsersFollowing = false;
    private FragmentManager fm;
    private BackstackHelper backstackHelper;

    private String diffUsersBio, diffUsersDisplayName = "";
    private User diffUser;
    private User user;

    private ProfilePagesHelper profilePagesHelper;
    private PostRecyclerView postRecyclerView;
    private NavigationHelper navigationHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        profileView = inflater.inflate(R.layout.fragment_different_users_profile, container, false);
        findViews();

        fm = getFragmentManager();
        fm.addOnBackStackChangedListener(this);
        backstackHelper = new BackstackHelper(fm);
        navigationHelper = NavigationHelper.getInstance();
        Bundle bundle = this.getArguments();

        mAuth = FirebaseAuth.getInstance();
        diffUser = new User();
        user = new User();
        diffUser.setUid(bundle.getString("uid"));
        user.setUid(mAuth.getCurrentUser().getUid());
        instantiateRefs();

        profilePagesHelper = new ProfilePagesHelper(diffUsersFollowersRef,
                diffUsersFollowingRef, tvNrFollowers, tvNrFollowing);
        setDisplayNameListener();
        setPhotoUrlListener();
        setBioListener();
        profilePagesHelper.setFollowSectionNumbers();
        setFollowButtonText();
        setMessageBtnOnClickListener();
        processBtnFollowClick();
        setTvNrFollowersOnClickListener();
        setTvNrFollowingOnClickListener();

        return  profileView;
    }

    private void setTvNrFollowersOnClickListener(){
        tvNrFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigationHelper.setRef(diffUsersFollowersRef);
                startActivity(new Intent(getContext(), UserNamesAndPhotosListActivity.class));
                getActivity().finish();
                Log.i(TAG, "ProfileFragment: finish()'ed activity ProfileActivity");
                getActivity().overridePendingTransition(0,0);
            }
        });
    }

    private void setTvNrFollowingOnClickListener(){
        tvNrFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigationHelper.setRef(diffUsersFollowingRef);
                startActivity(new Intent(getContext(), UserNamesAndPhotosListActivity.class));
                getActivity().finish();
                Log.i(TAG, "ProfileFragment: finish()'ed activity ProfileActivity");
                getActivity().overridePendingTransition(0,0);
            }
        });
    }

    private void findViews(){
        tvDisplayName = profileView.findViewById(R.id.user_display_name);
        civProfilePic = profileView.findViewById(R.id.civ_profile_pic);
        tvNrFollowers = profileView.findViewById(R.id.tv_nr_followers);
        tvNrFollowing = profileView.findViewById(R.id.tv_nr_following);
        btnFollow = profileView.findViewById(R.id.btn_follow);
        btnMessage = profileView.findViewById(R.id.btn_message);
        tvBio = profileView.findViewById(R.id.tv_user_bio);
    }

    private void instantiateRefs(){
        database = FirebaseDatabase.getInstance().getReference();
        followersRef = database.child("followers");
        followingRef = database.child("following");
        diffUsersRef = database.child("users").child(diffUser.getUid());
        diffUsersDisplayNameRef = diffUsersRef.child("displayName");
        diffUsersPhotoUrlRef = diffUsersRef.child("photoUrl");
        diffUsersBioRef = diffUsersRef.child("bio");
        diffUsersFollowersRef = followersRef.child(diffUser.getUid());
        diffUsersFollowingRef = followingRef.child(diffUser.getUid());
        usersFollowingRef = followingRef.child(user.getUid());
        contactsRef = database.child("contacts");
        usersContactsRef = contactsRef.child(user.getUid());
        diffUsersContactsRef = contactsRef.child(diffUser.getUid());
        photoUrlRef = database.child("users").child(user.getUid()).child("photoUrl");

        /* For debugging purposes:
        firebase gives permission denied to this, as desired.
        database.child("followers").removeValue();
        database.child("following").removeValue();
        database.child("following").child(diffUser.getUid()).removeValue();
        database.child("followers").child(diffUser.getUid()).removeValue();
        */
    }

    @Override
    public void onStart() {
        super.onStart();

        Query query = FirebaseDatabase.getInstance().getReference().child("posts").orderByChild("uid").equalTo(diffUser.getUid());
        postRecyclerView = new PostRecyclerView(query,
                getActivity(),
                getContext(),
                profileView,
                TAG);
        // This means that every time this activity is stopped by another, this rv destroys, so this needs to be put in onCreate,
        // but also its state would need to be saved in onStopped, and resumed in onStart or onStart. Or don't put it in onCreate
        // and leave it only in onStart()
        postRecyclerView.displayRV();
    }

    private void setBioListener(){
        diffUsersBioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                onBioChanged(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void onBioChanged(@NonNull DataSnapshot dataSnapshot){
        try{
            diffUsersBio = dataSnapshot.getValue().toString();
            if(diffUsersBio.equals("")){
                tvBio.setText("");
                tvBio.setHint(diffUsersDisplayName + " has not written a bio...");
            } else{
                tvBio.setText(diffUsersBio);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void setDisplayNameListener(){
        diffUsersDisplayNameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                onDisplayNameChanged(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void onDisplayNameChanged(@NonNull DataSnapshot dataSnapshot){
        diffUser.setDisplayName(dataSnapshot.getValue().toString());
        tvDisplayName.setText(diffUser.getDisplayName());
    }

    private void setPhotoUrlListener(){
        diffUsersPhotoUrlRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                onPhotoUrlChanged(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void onPhotoUrlChanged(@NonNull DataSnapshot dataSnapshot){
        diffUser.setPhotoUrl(dataSnapshot.getValue().toString());
        try{
            Picasso.get().load(diffUser.getPhotoUrl()).placeholder(R.drawable.profile_pic_placeholder).into(civProfilePic);
        }catch(Exception e){
            e.printStackTrace();
            civProfilePic.setImageResource(R.drawable.profile_pic_placeholder);
        }
    }

    private void setFollowButtonText(){
        diffUsersFollowersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                onUserIsFollowingDiffUserChanged(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setMessageBtnOnClickListener(){
        btnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent convoActivityIntent = new Intent(getContext(), ConvoActivity.class);
                convoActivityIntent.putExtra("diffUsersId", diffUser.getUid());
                startActivity(convoActivityIntent);
                getActivity().finish();
                Log.i(TAG, "finish()'ed activity which is displaying the current DifferentUsersProfileFragment");
            }
        });
    }

    private void onUserIsFollowingDiffUserChanged(@NonNull DataSnapshot dataSnapshot){
        if(dataSnapshot.hasChild(user.getUid())){
            // user is following diff user
            btnFollow.setText("Following");
        }
        else{
            // user is not following diff user
            btnFollow.setText("Follow");
        }
    }

    private void processBtnFollowClick(){
        btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processFollowActionForDiffUser();
                //addToDiffUsersFollowers();
                processFollowActionForUser();
            }
        });
    }

    private void processFollowActionForUser(){
        processingAddToUsersFollowing = true;

        usersFollowingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(processingAddToUsersFollowing){
                    if(dataSnapshot.hasChild(diffUser.getUid())){
                        // remove from user's following list
                        removeFromUsersFollowing();
                        processingAddToUsersFollowing = false;
                        processContactsWhenUnfollowingDiffUser();
                    } else {
                        // add to list of user's list of following
                        addToUsersFollowing();
                        processingAddToUsersFollowing = false;
                        processContactsWhenFollowingDiffUser();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                processingAddToUsersFollowing = false;
            }
        });
    }

    private void processContactsWhenUnfollowingDiffUser(){
        diffUsersFollowingRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    // diff user is following user, so leave contacts as they are
                }else{
                    // diff user is not following user, so remove both users from
                    // eachother's contacts
                    removeUserFromDiffUsersContacts();
                    removeDiffUserFromUsersContacts();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void removeUserFromDiffUsersContacts(){
        diffUsersContactsRef.child(user.getUid()).removeValue();
    }

    private void removeDiffUserFromUsersContacts(){
        usersContactsRef.child(diffUser.getUid()).removeValue();
    }

    private void addCurrentUsersNameAndPhotoUrlToDbRef(final DatabaseReference ref){
        ref.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){// this user is already in ref. So do nothing
                } else {
                    // add user to ref
                    final FirebaseUser fireUser = mAuth.getCurrentUser();
                    photoUrlRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                String photoUrl = dataSnapshot.getValue().toString();
                                UserNameAndPhotoUrl userNameAndPhotoUrl = new UserNameAndPhotoUrl(fireUser.getDisplayName(), photoUrl);
                                ref.child(user.getUid()).setValue(userNameAndPhotoUrl);
                            } else{
                                // photoUrl does not exists, add only user's display name
                                UserNameAndPhotoUrl userNameAndPhotoUrl = new UserNameAndPhotoUrl();
                                userNameAndPhotoUrl.setDisplayName(fireUser.getDisplayName());
                                ref.child(user.getUid()).setValue(userNameAndPhotoUrl);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void addDiffUsersNameAndPhotoUrlToDbRef(final DatabaseReference ref){
        ref.child(diffUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    // diff user is already in ref. So do nothing
                } else {
                    // add diff user to ref
                    diffUsersPhotoUrlRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                String photoUrl = dataSnapshot.getValue().toString();
                                UserNameAndPhotoUrl newContact = new UserNameAndPhotoUrl(diffUser.getDisplayName(), photoUrl);
                                ref.child(diffUser.getUid()).setValue(newContact);
                            } else{
                                // photoUrl does not exists, add only user's display name
                                UserNameAndPhotoUrl userNameAndPhotoUrl = new UserNameAndPhotoUrl();
                                userNameAndPhotoUrl.setDisplayName(diffUser.getDisplayName());
                                ref.child(diffUser.getUid()).setValue(userNameAndPhotoUrl);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void processContactsWhenFollowingDiffUser(){
        addCurrentUsersNameAndPhotoUrlToDbRef(diffUsersContactsRef);
        addDiffUsersNameAndPhotoUrlToDbRef(usersContactsRef);
    }

    private void addToUsersFollowing(){
        UserNameAndPhotoUrl newFollower = new UserNameAndPhotoUrl(diffUser.getDisplayName(), diffUser.getPhotoUrl());
        usersFollowingRef.child(diffUser.getUid()).setValue(newFollower);
    }

    private void removeFromUsersFollowing(){
        processingRemoveFromUsersFollowing = true;

        usersFollowingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(processingRemoveFromUsersFollowing) {
                    if (dataSnapshot.hasChild(diffUser.getUid())) {
                        // remove from user's following list
                        usersFollowingRef.child(diffUser.getUid()).removeValue();
                        processingRemoveFromUsersFollowing = false;
                    } else {
                        // user wasn't following diff user
                        processingRemoveFromUsersFollowing = false;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                processingRemoveFromUsersFollowing = false;
            }
        });

    }

    private void processFollowActionForDiffUser(){
        processingFollowAction = true;

        diffUsersFollowersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(processingFollowAction){
                    if(dataSnapshot.hasChild(user.getUid())){
                        // remove user from diff user's followers
                        removeFromDiffUsersFollowers();
                        processingFollowAction = false;
                    } else {
                        // add user to diff user's followers
                        addToDiffUsersFollowers();
                        processingFollowAction = false;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                processingFollowAction = false;
            }
        });
    }

    private void addToDiffUsersFollowers(){
        final FirebaseUser fireUser = mAuth.getCurrentUser();
        photoUrlRef = database.child("users").child(user.getUid()).child("photoUrl");

        photoUrlRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String photoUrl = dataSnapshot.getValue().toString();
                    UserNameAndPhotoUrl newFollower = new UserNameAndPhotoUrl(fireUser.getDisplayName(), photoUrl);
                    diffUsersFollowersRef.child(user.getUid()).setValue(newFollower);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void removeFromDiffUsersFollowers(){
        diffUsersFollowersRef.child(user.getUid()).removeValue();
    }

    @Override
    public void onBackStackChanged() {
        backstackHelper.logCurrentBackstack(TAG);
    }

}
