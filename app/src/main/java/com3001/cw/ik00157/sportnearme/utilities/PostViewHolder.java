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
import com3001.cw.ik00157.sportnearme.models.Post;
import com3001.cw.ik00157.sportnearme.models.UserNameAndPhotoUrl;
import de.hdodenhof.circleimageview.CircleImageView;

public class PostViewHolder  extends RecyclerView.ViewHolder{

    private final String TAG;

    private FirebaseAuth mAuth;
    private FragmentNavigationHelper fragmentNavigationHelper;
    private DatabaseReference database, likesRef, eventsAndMembersRef;
    private static DatabaseReference dbPostRef;
    private final Context ctx;
    private final FragmentActivity fragmentActivity;
    private TimeHelper timeHelper;
    private NavigationHelper navigationHelper;

    private CircleImageView civPostUserPhoto;
    private TextView tvPostCreatorDisplayName, tvPostTimeCreated, tvPostBody, tvNrLikes, tvNrEventMembers, tvPostSport;
    private ImageButton btnLike, btnGoingToEvent, btnComment, btnMoreOptions;

    private boolean processingLikeAction, processingGoingToEventAction = false;

    public PostViewHolder(View itemView, final FragmentActivity fragmentActivity, final Context ctx, FragmentNavigationHelper fragmentNavigationHelper, final String TAG) {
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
                    Log.i(TAG, "PostViewHolder: finish()'ed an activity");
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
                    Log.i(TAG, "PostViewHolder: finish()'ed an activity");
                    ((Activity) ctx).overridePendingTransition(0, 0);
                }
            }
        });
    }

    public void onBindViewHolder(@NonNull final Post post, final String postId){

        setRigidData(postId,post);
        setLikeBtn(postId);
        setNrLikesTv(postId);
        setGoingToEventBtn(postId);
        setNrEventMembers(postId);
        setTvNrLikesOnClickListener(postId);
        setTvNrEventMembersOnClickListener(postId);

        getCivPostUserPhoto().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAuth.getCurrentUser().getUid().equals(post.getUid()) ){
                    Log.i(TAG, "Post uid and current user's uid equal");
                    ctx.startActivity(new Intent(ctx, ProfileActivity.class));
                    if(ctx instanceof Activity){
                        ((Activity) ctx).finish();
                        ((Activity) ctx).overridePendingTransition(0, 0);
                    }
                } else{
                    Log.i(TAG, "Post uid and current user's uid equal");
                    Bundle bundle = new Bundle();
                    bundle.putString("uid", post.getUid());
                    fragmentNavigationHelper.changeFragInContainerAddToBackstack(new DifferentUsersProfileFragment(),
                            bundle,
                            R.id.fragment_container,
                            "replace fragment_container fragment with DifferentUsersProfileFragment");
                }
            }
        });

        getTvPostCreatorDisplayName().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAuth.getCurrentUser().getUid().equals(post.getUid()) ){
                    Log.i(TAG, "Post uid and current user's uid equal");
                    ctx.startActivity(new Intent(ctx, ProfileActivity.class));
                    if(ctx instanceof Activity){
                        ((Activity) ctx).finish();
                        ((Activity) ctx).overridePendingTransition(0, 0);
                    }
                } else{
                    Log.i(TAG, "Post uid and current user's uid do not equal");
                    Bundle bundle = new Bundle();
                    bundle.putString("uid", post.getUid());
                    fragmentNavigationHelper.changeFragInContainerAddToBackstack(new DifferentUsersProfileFragment(),
                            bundle,
                            R.id.fragment_container,
                            "replace fragment_container fragment with DifferentUsersProfileFragment");
                }
            }
        });

        getBtnLike().setOnClickListener(new View.OnClickListener() {
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

        getBtnComment().setOnClickListener(new View.OnClickListener() {
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

        getBtnGoingToEvent().setOnClickListener(new View.OnClickListener() {
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

    public void setRigidData(final String postId, final Post post){
        dbPostRef.child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try{
                    Picasso.get().load(post.getPhotoUrl()).placeholder(R.drawable.profile_pic_placeholder).into(civPostUserPhoto);
                } catch(Exception e){
                    e.printStackTrace();
                    civPostUserPhoto.setImageResource(R.drawable.profile_pic_placeholder);
                }
                tvPostCreatorDisplayName.setText(post.getCreatorDisplayName());
                setPostTimeCreated(postId, post.getTimeCreated());
                setPostSport(post.getSport());
                tvPostBody.setText(post.getBody());
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

    public void setPostTimeCreated(final String postId, String postTimeCreated){
        tvPostTimeCreated.setText(timeHelper.getHowLongAgoCreatedFriendlyFormat(postTimeCreated));
    }

    public void setPostSport(String sport){
        if(sport != null){
            tvPostSport.setText(" -  " + sport);
        }
    }

    public CircleImageView getCivPostUserPhoto() {
        return civPostUserPhoto;
    }

    public TextView getTvPostCreatorDisplayName() {
        return tvPostCreatorDisplayName;
    }

    public ImageButton getBtnLike() {
        return btnLike;
    }

    public ImageButton getBtnComment() {
        return btnComment;
    }

    public ImageButton getBtnGoingToEvent() {
        return btnGoingToEvent;
    }
}
