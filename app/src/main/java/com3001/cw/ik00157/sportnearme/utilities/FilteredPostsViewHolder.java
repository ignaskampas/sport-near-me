package com3001.cw.ik00157.sportnearme.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import com3001.cw.ik00157.sportnearme.R;
import com3001.cw.ik00157.sportnearme.activities.CommentsActivity;
import com3001.cw.ik00157.sportnearme.activities.ProfileActivity;
import com3001.cw.ik00157.sportnearme.activities.UserNamesAndPhotosListActivity;
import com3001.cw.ik00157.sportnearme.activities.popupwindows.PostMoreOptionsPopup;
import com3001.cw.ik00157.sportnearme.fragments.DifferentUsersProfileFragment;
import com3001.cw.ik00157.sportnearme.models.UserNameAndPhotoUrl;
import de.hdodenhof.circleimageview.CircleImageView;

public class FilteredPostsViewHolder extends RecyclerView.ViewHolder{

    private final String TAG;

    FirebaseAuth mAuth;
    private FragmentNavigationHelper fragmentNavigationHelper;
    DatabaseReference database, likesRef, eventsAndMembersRef;
    private static DatabaseReference dbPostRef;
    final private Context ctx;
    final private FragmentActivity fragmentActivity;
    private TimeHelper timeHelper;
    private NavigationHelper navigationHelper;

    CircleImageView civPostUserPhoto;
    TextView tvPostCreatorDisplayName, tvPostTimeCreated, tvPostBody, tvNrLikes, tvNrEventMembers, tvPostSport;
    ImageButton btnLike, btnGoingToEvent, btnComment, btnMoreOptions;

    private boolean processingLikeAction, processingGoingToEventAction = false;

    public FilteredPostsViewHolder(View itemView, final FragmentActivity fragmentActivity, final Context ctx, FragmentNavigationHelper fragmentNavigationHelper, final String TAG) {
        super(itemView);

        this.fragmentActivity = fragmentActivity;
        this.ctx = ctx;
        this.fragmentNavigationHelper = fragmentNavigationHelper;
        navigationHelper = NavigationHelper.getInstance();
        this.TAG = TAG;

        this.timeHelper = new TimeHelper(this.TAG);
        mAuth = FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance().getReference();
        likesRef = database.child("likes");
        dbPostRef = database.child("posts");
        eventsAndMembersRef = database.child("eventsAndMembers");

        civPostUserPhoto = itemView.findViewById(R.id.post_user_photo);
        tvPostBody = itemView.findViewById(R.id.post_body);
        tvPostCreatorDisplayName = itemView.findViewById(R.id.post_creator_display_name);
        tvPostTimeCreated = itemView.findViewById(R.id.post_time_created);
        tvPostSport = itemView.findViewById(R.id.post_sport);
        btnLike = itemView.findViewById(R.id.btn_like);
        tvNrLikes = itemView.findViewById(R.id.tv_nr_likes);
        btnComment = itemView.findViewById(R.id.btn_comment);
        btnGoingToEvent = itemView.findViewById(R.id.btn_going_to_event);
        tvNrEventMembers = itemView.findViewById(R.id.tv_nr_event_members);
        btnMoreOptions = itemView.findViewById(R.id.btn_more_options);

        //likesRef.keepSynced(true);
    }

    private void setTvNrLikesOnClickListener(final String postId){
        tvNrLikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatabaseReference postLikesRef = likesRef.child(postId);
                navigationHelper.setRef(postLikesRef);
                ctx.startActivity(new Intent(ctx, UserNamesAndPhotosListActivity.class));
                if(ctx instanceof Activity){
                    ((Activity) ctx).finish();
                    Log.i(TAG, "FilteredPostsViewHolder: finish()'ed an activity");
                    ((Activity) ctx).overridePendingTransition(0, 0);
                }
            }
        });
    }

    private void setTvNrEventMembersOnClickListener(final String postId){
        tvNrEventMembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference eventMembersRef = eventsAndMembersRef.child(postId);
                navigationHelper.setRef(eventMembersRef);
                ctx.startActivity(new Intent(ctx, UserNamesAndPhotosListActivity.class));
                if(ctx instanceof Activity){
                    ((Activity) ctx).finish();
                    Log.i(TAG, "FilteredPostsViewHolder: finish()'ed an activity");
                    ((Activity) ctx).overridePendingTransition(0, 0);
                }
            }
        });
    }

    public void onBindViewHolder(final String postId){

        setPostDataFromPostsTable(postId);
        setLikeBtn(postId);
        setNrLikesTv(postId);
        btnLikeSetOnClickListener(postId);
        setGoingToEventBtn(postId);
        setNrEventMembers(postId);
        btnGoingToEventSetOnClickListener(postId);
        btnCommentSetOnClickListener(postId);
        tvPostCreatorDisplayNameSetOnClickL(postId);
        civPostUserPhotoSetOnClickListener(postId);
        setTvNrLikesOnClickListener(postId);
        setTvNrEventMembersOnClickListener(postId);

        btnMoreOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Clicked on post's more options button");
                Intent moreOptionsPopUp = new Intent(ctx, PostMoreOptionsPopup.class);
                moreOptionsPopUp.putExtra("postId", postId);
                fragmentActivity.startActivity(moreOptionsPopUp);
            }
        });

    }

    private void btnLikeSetOnClickListener(final String postId){
        btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processingLikeAction = true;

                likesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(processingLikeAction) {
                            if (dataSnapshot.child(postId).hasChild(mAuth.getCurrentUser().getUid())) {
                                // remove user's like
                                likesRef.child(postId).child(mAuth.getCurrentUser().getUid()).removeValue();
                                processingLikeAction = false;
                            } else {
                                // like the post
                                final FirebaseUser user = mAuth.getCurrentUser();
                                DatabaseReference userPhotoUrlRef = database.child("users").child(user.getUid()).child("photoUrl");

                                userPhotoUrlRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        String photoUrl = "";
                                        if(dataSnapshot.exists()){
                                            photoUrl = dataSnapshot.getValue().toString();
                                        }
                                        UserNameAndPhotoUrl userNameAndPhotoUrl = new UserNameAndPhotoUrl();
                                        userNameAndPhotoUrl.setDisplayName(user.getDisplayName());
                                        userNameAndPhotoUrl.setPhotoUrl(photoUrl);
                                        likesRef.child(postId).child(mAuth.getCurrentUser().getUid()).setValue(userNameAndPhotoUrl);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                processingLikeAction = false;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    private void setPostDataFromPostsTable(String postId){
        dbPostRef.child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("body").exists()){
                    tvPostBody.setText(dataSnapshot.child("body").getValue().toString());
                }
                if(dataSnapshot.child("creatorDisplayName").exists()){
                    tvPostCreatorDisplayName.setText(dataSnapshot.child("creatorDisplayName").getValue().toString());
                }
                if(dataSnapshot.child("photoUrl").exists()){
                    try{
                        Picasso.get().load(dataSnapshot.child("photoUrl").getValue().toString()).placeholder(R.drawable.profile_pic_placeholder).into(civPostUserPhoto);
                    } catch(Exception e){
                        e.printStackTrace();
                        civPostUserPhoto.setImageResource(R.drawable.profile_pic_placeholder);
                    }
                }
                if(dataSnapshot.child("timeCreated").exists()){
                    tvPostTimeCreated.setText(timeHelper.getHowLongAgoCreatedFriendlyFormat(dataSnapshot.child("timeCreated").getValue().toString()));
                }
                if(dataSnapshot.child("sport").exists()){
                    tvPostSport.setText(" -  " + dataSnapshot.child("sport").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void setLikeBtn(final String postId){
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(postId).hasChild(mAuth.getUid())){
                    btnLike.setImageResource(R.drawable.ic_thumb_up_red);
                }
                else{
                    btnLike.setImageResource(R.drawable.ic_thumb_up);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void setGoingToEventBtn(final String postId){
        // eventsAndMembersRef
        eventsAndMembersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(postId).hasChild(mAuth.getUid())){
                    btnGoingToEvent.setImageResource(R.drawable.ic_going_to_event_red);
                }
                else{
                    btnGoingToEvent.setImageResource(R.drawable.ic_going_to_event);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void setNrLikesTv(final String postId){
        DatabaseReference postLikesRef = likesRef.child(postId);

        postLikesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String nrLikes = Long.toString(dataSnapshot.getChildrenCount());
                    tvNrLikes.setText(nrLikes);
                } else{
                    tvNrLikes.setText(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void setNrEventMembers(final String postId){
        DatabaseReference eventMembersRef = eventsAndMembersRef.child(postId);

        eventMembersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String nrMembers = Long.toString(dataSnapshot.getChildrenCount());
                    tvNrEventMembers.setText(nrMembers);
                } else{
                    tvNrEventMembers.setText(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void btnGoingToEventSetOnClickListener(final String postId){
        btnGoingToEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                processingGoingToEventAction = true;

                eventsAndMembersRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(processingGoingToEventAction) {
                            if (dataSnapshot.child(postId).hasChild(mAuth.getCurrentUser().getUid())) {
                                // remove user from event
                                eventsAndMembersRef.child(postId).child(mAuth.getCurrentUser().getUid()).removeValue();
                                processingGoingToEventAction = false;
                            } else {
                                // add user to the event
                                final FirebaseUser user = mAuth.getCurrentUser();
                                DatabaseReference userPhotoUrlRef = database.child("users").child(user.getUid()).child("photoUrl");

                                userPhotoUrlRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        String photoUrl = "";
                                        if(dataSnapshot.exists()){
                                            photoUrl = dataSnapshot.getValue().toString();
                                        }
                                        UserNameAndPhotoUrl userNameAndPhotoUrl = new UserNameAndPhotoUrl();
                                        userNameAndPhotoUrl.setDisplayName(user.getDisplayName());
                                        userNameAndPhotoUrl.setPhotoUrl(photoUrl);

                                        eventsAndMembersRef.child(postId).child(mAuth.getCurrentUser().getUid()).setValue(userNameAndPhotoUrl);
                                        processingGoingToEventAction = false;
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        processingGoingToEventAction = false;
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        processingGoingToEventAction = false;
                    }
                });
            }
        });
    }

    private void btnCommentSetOnClickListener(final String postId){
        btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent commentsIntent = new Intent(ctx, CommentsActivity.class);
                commentsIntent.putExtra("postId", postId);
                ctx.startActivity(commentsIntent);
                if(ctx instanceof Activity){
                    ((Activity) ctx).finish();
                    Log.i(TAG, "finish()'ed activity HomeActivity");
                }
            }
        });
    }

    private void tvPostCreatorDisplayNameSetOnClickL(final String postId){
        tvPostCreatorDisplayName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbPostRef.child(postId).child("uid").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            if(mAuth.getCurrentUser().getUid().equals(dataSnapshot.getValue().toString())){
                                Log.i(TAG, "Post uid and current user's uid equal");
                                ctx.startActivity(new Intent(ctx, ProfileActivity.class));
                                if(ctx instanceof Activity){
                                    ((Activity) ctx).finish();
                                    ((Activity) ctx).overridePendingTransition(0, 0);
                                }
                            } else{
                                navigateToDiffUsersFrag(dataSnapshot.getValue().toString());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    private void civPostUserPhotoSetOnClickListener(final String postId){
        civPostUserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbPostRef.child(postId).child("uid").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            if(mAuth.getCurrentUser().getUid().equals(dataSnapshot.getValue().toString())){
                                Log.i(TAG, "Post uid and current user's uid equal");
                                ctx.startActivity(new Intent(ctx, ProfileActivity.class));
                                if(ctx instanceof Activity){
                                    ((Activity) ctx).finish();
                                    ((Activity) ctx).overridePendingTransition(0, 0);
                                }
                            } else{
                                navigateToDiffUsersFrag(dataSnapshot.getValue().toString());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    private void navigateToDiffUsersFrag(String postUid){
        Bundle bundle = new Bundle();
        bundle.putString("uid", postUid);

        fragmentNavigationHelper.changeFragInContainerAddToBackstack(new DifferentUsersProfileFragment(),
                bundle,
                R.id.fragment_container,
                "replace fragment_container fragment with DifferentUsersProfileFragment");
    }


}
