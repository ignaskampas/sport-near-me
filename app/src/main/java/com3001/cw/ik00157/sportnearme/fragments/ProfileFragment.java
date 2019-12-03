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
import android.widget.EditText;
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
import com3001.cw.ik00157.sportnearme.activities.HomeActivity;
import com3001.cw.ik00157.sportnearme.activities.UserNamesAndPhotosListActivity;
import com3001.cw.ik00157.sportnearme.models.User;
import com3001.cw.ik00157.sportnearme.utilities.BackstackHelper;
import com3001.cw.ik00157.sportnearme.utilities.BottomNavBarHelper;
import com3001.cw.ik00157.sportnearme.utilities.NavigationHelper;
import com3001.cw.ik00157.sportnearme.utilities.PostRecyclerView;
import com3001.cw.ik00157.sportnearme.utilities.ProfilePagesHelper;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends BaseFragment implements FragmentManager.OnBackStackChangedListener{

    private static final String TAG = "PROFILE_FRAGMENT";

    private View profileView;
    private Button btnUpdateBio;
    private CircleImageView civProfilePic;
    private EditText etUserBio;
    private TextView tvDisplayName, tvNrFollowers, tvNrFollowing;

    private FirebaseAuth mAuth;
    private DatabaseReference database,
            userRef, bioRef, photoUrlRef,
            followersRef, followingRef,
            usersFollowersRef, usersFollowingRef;

    private FragmentManager fm;
    private BackstackHelper backstackHelper;
    private User user;

    private ProfilePagesHelper profilePagesHelper;

    private PostRecyclerView postRecyclerView;
    private NavigationHelper navigationHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        profileView = inflater.inflate(R.layout.fragment_profile, container, false);
        findViews();

        fm = getFragmentManager();
        fm.addOnBackStackChangedListener(this);
        backstackHelper = new BackstackHelper(fm);
        navigationHelper = NavigationHelper.getInstance();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser fireUser = mAuth.getCurrentUser();
        user = new User();
        user.setUid(fireUser.getUid());
        user.setDisplayName(fireUser.getDisplayName());
        instantiateRefs();

        profilePagesHelper = new ProfilePagesHelper(usersFollowersRef,
                usersFollowingRef, tvNrFollowers, tvNrFollowing);
        setTvDisplayName();
        setPhotoUrlListener();
        setBioListener();
        profilePagesHelper.setFollowSectionNumbers();
        setBtnUpdateBioOnClick();

        setTvNrFollowersOnClickListener();
        setTvNrFollowingOnClickListener();

        return profileView;
    }

    @Override
    public void onStart() {
        super.onStart();

        Query query = FirebaseDatabase.getInstance().getReference().child("posts").orderByChild("uid").equalTo(user.getUid());
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

    private void setTvNrFollowersOnClickListener(){
        tvNrFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigationHelper.setRef(usersFollowersRef);
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
                navigationHelper.setRef(usersFollowingRef);
                startActivity(new Intent(getContext(), UserNamesAndPhotosListActivity.class));
                getActivity().finish();
                Log.i(TAG, "ProfileFragment: finish()'ed activity ProfileActivity");
                getActivity().overridePendingTransition(0,0);
            }
        });
    }

    private void findViews(){
        civProfilePic = profileView.findViewById(R.id.civ_profile_pic);
        tvNrFollowers = profileView.findViewById(R.id.tv_nr_followers);
        tvNrFollowing = profileView.findViewById(R.id.tv_nr_following);
        etUserBio = profileView.findViewById(R.id.user_bio);
        btnUpdateBio = profileView.findViewById(R.id.btnUpdateBio);
        tvDisplayName = profileView.findViewById(R.id.user_display_name);
    }

    private void instantiateRefs(){
        database = FirebaseDatabase.getInstance().getReference();
        followersRef = database.child("followers");
        followingRef = database.child("following");
        usersFollowersRef = followersRef.child(user.getUid());
        usersFollowingRef = followingRef.child(user.getUid());
        userRef = database.child("users").child(user.getUid());
        bioRef = userRef.child("bio");
        photoUrlRef = userRef.child("photoUrl");
    }

    private void setTvDisplayName(){
        tvDisplayName.setText(user.getDisplayName());
    }

    private void setPhotoUrlListener(){
        photoUrlRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user.setPhotoUrl(dataSnapshot.getValue().toString());
                try{
                    Picasso.get().load(user.getPhotoUrl()).placeholder(R.drawable.profile_pic_placeholder).into(civProfilePic);
                }catch(Exception e){
                    e.printStackTrace();
                    civProfilePic.setImageResource(R.drawable.profile_pic_placeholder);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setBioListener(){
        bioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user.setBio(dataSnapshot.getValue().toString());
                etUserBio.setText(user.getBio());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setBtnUpdateBioOnClick(){
        btnUpdateBio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newBio = etUserBio.getText().toString();
                bioRef.setValue(newBio);
            }
        });
    }

    @Override
    public void onBackStackChanged() {
        backstackHelper.logCurrentBackstack(TAG);
    }

    @Override
    public void onBackPressed() {
        /*jeigu paskutinis backstack dalykas yra replace...
        tada popBackSTack, ir eit i news feed per navigation
        */

        int num = fm.getBackStackEntryCount();
        if(num > 0 && fm.getBackStackEntryAt(num-1).getName().equals("replace fragment_container fragment with DifferentUsersProfileFragment")){
                Log.d(TAG, "last backstack entry is 'replace fragment_container fragment with DifferentUsersProfileFragment'");
                Fragment newsFeedFragment = new NewsFeedFragment();
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, newsFeedFragment).commit();
                BottomNavBarHelper bottomNavBarHelper = HomeActivity.getBottomNavBarHelper();
                bottomNavBarHelper.setSelectedItemId(R.id.nav_news_feed);
                //fm.popBackStack();
        } else {
                Log.d(TAG, "last backstack entry is NOT 'replace fragment_container fragment with DifferentUsersProfileFragment'");
                fm.popBackStack();
        }

    }
}
