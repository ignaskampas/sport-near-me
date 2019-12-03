package com3001.cw.ik00157.sportnearme.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
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
import com3001.cw.ik00157.sportnearme.activities.HomeActivity;
import com3001.cw.ik00157.sportnearme.activities.ProfileActivity;
import com3001.cw.ik00157.sportnearme.activities.UserNamesAndPhotosListActivity;
import com3001.cw.ik00157.sportnearme.models.Comment;
import com3001.cw.ik00157.sportnearme.models.UserNameAndPhotoUrl;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommentViewHolder extends RecyclerView.ViewHolder{

    private final String TAG;

    private FirebaseAuth mAuth;
    private DatabaseReference database, commentLikesRef;
    private final Context ctx;
    private TimeHelper timeHelper;

    private CircleImageView civCommentUserPhoto;
    private TextView tvCommentCreatorDisplayName, tvCommentTimeCreated, tvCommentText, tvNrLikes;
    private ImageButton btnLike;
    private NavigationHelper navigationHelper;

    private boolean processingLikeAction = false;

    public CommentViewHolder(View itemView, final Context ctx, final String TAG) {
        super(itemView);

        this.ctx = ctx;
        this.TAG = TAG;

        this.timeHelper = new TimeHelper(this.TAG);
        navigationHelper = NavigationHelper.getInstance();
        mAuth = FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance().getReference();
        commentLikesRef = database.child("commentLikes");

        civCommentUserPhoto = itemView.findViewById(R.id.comment_user_photo);
        tvCommentText = itemView.findViewById(R.id.comment_text);
        tvCommentCreatorDisplayName = itemView.findViewById(R.id.comment_creator_display_name);
        tvCommentTimeCreated = itemView.findViewById(R.id.comment_time_created);
        btnLike = itemView.findViewById(R.id.btn_like);
        tvNrLikes = itemView.findViewById(R.id.tv_nr_likes);

        //likesRef.keepSynced(true);
    }

    private void setNrLikesTvOnClickListener(final String postId, final String commentId){
        tvNrLikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatabaseReference thisCommentLikesRef = commentLikesRef.child(postId).child(commentId);
                navigationHelper.setRef(thisCommentLikesRef);
                ctx.startActivity(new Intent(ctx, UserNamesAndPhotosListActivity.class));
                if(ctx instanceof Activity){
                    ((Activity) ctx).finish();
                    Log.i(TAG, "PostViewHolder: finish()'ed an activity");
                    ((Activity) ctx).overridePendingTransition(0, 0);
                }
            }
        });
    }

    public void onBindViewHolder(@NonNull final Comment comment, final String postId, final String commentId){

        setRigidData(comment);
        setLikeBtn(postId, commentId);
        setNrLikesTv(postId, commentId);
        setNrLikesTvOnClickListener(postId, commentId);

        civCommentUserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAuth.getCurrentUser().getUid().equals(comment.getUid()) ){
                    Log.i(TAG, "Comment uid and current user's uid equal");
                    ctx.startActivity(new Intent(ctx, ProfileActivity.class));
                    if(ctx instanceof Activity){
                        ((Activity) ctx).finish();
                        ((Activity) ctx).overridePendingTransition(0, 0);
                    }
                } else{
                    ctx.startActivity(new Intent(ctx, HomeActivity.class));
                    if(ctx instanceof Activity){
                        ((Activity) ctx).finish();
                        Log.i(TAG, "finish()'ed activity CommentsActivity");
                        ((Activity) ctx).overridePendingTransition(0, 0);
                    }
                    NavigationHelper navigationHelper = NavigationHelper.getInstance();
                    navigationHelper.setGoToDiffUsersFrag(true);
                    navigationHelper.setDiffUsersId(comment.getUid());
                }
            }
        });

        tvCommentCreatorDisplayName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAuth.getCurrentUser().getUid().equals(comment.getUid()) ){
                    Log.i(TAG, "Comment uid and current user's uid equal");
                    ctx.startActivity(new Intent(ctx, ProfileActivity.class));
                    if(ctx instanceof Activity){
                        ((Activity) ctx).finish();
                        ((Activity) ctx).overridePendingTransition(0, 0);
                    }
                } else{
                    ctx.startActivity(new Intent(ctx, HomeActivity.class));
                    if(ctx instanceof Activity){
                        ((Activity) ctx).finish();
                        Log.i(TAG, "finish()'ed activity CommentsActivity");
                        ((Activity) ctx).overridePendingTransition(0, 0);
                    }
                    NavigationHelper navigationHelper = NavigationHelper.getInstance();
                    navigationHelper.setGoToDiffUsersFrag(true);
                    navigationHelper.setDiffUsersId(comment.getUid());
                }
            }
        });

        btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                processingLikeAction = true;

                final DatabaseReference thisCommentLikesRef = commentLikesRef.child(postId).child(commentId);

                thisCommentLikesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(processingLikeAction) {
                            if (dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {
                                // remove user's like
                                thisCommentLikesRef.child(mAuth.getCurrentUser().getUid()).removeValue();
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
                                        thisCommentLikesRef.child(mAuth.getCurrentUser().getUid()).setValue(userNameAndPhotoUrl);
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

    public void setRigidData(final Comment comment){
        try{
            Picasso.get().load(comment.getPhotoUrl()).placeholder(R.drawable.profile_pic_placeholder).into(civCommentUserPhoto);
        } catch(Exception e){
            e.printStackTrace();
            civCommentUserPhoto.setImageResource(R.drawable.profile_pic_placeholder);
        }
        tvCommentCreatorDisplayName.setText(comment.getCreatorDisplayName());
        setCommentTimeCreated(comment.getTimeCreated());
        tvCommentText.setText(comment.getText());
    }

    public void setLikeBtn(final String postId, final String commentId){
        commentLikesRef.child(postId).child(commentId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(mAuth.getUid())){
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

    public void setNrLikesTv(final String postId, final String commentId){
        DatabaseReference thisCommentLikesRef = commentLikesRef.child(postId).child(commentId);

        thisCommentLikesRef.addValueEventListener(new ValueEventListener() {
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

    public void setCommentTimeCreated(String commentTimeCreated){
        tvCommentTimeCreated.setText(timeHelper.getHowLongAgoCreatedFriendlyFormat(commentTimeCreated));
    }

}
