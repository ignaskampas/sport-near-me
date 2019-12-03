package com3001.cw.ik00157.sportnearme.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
import com3001.cw.ik00157.sportnearme.activities.popupwindows.PostMoreOptionsPopup;
import com3001.cw.ik00157.sportnearme.models.Comment;
import com3001.cw.ik00157.sportnearme.models.UserNameAndPhotoUrl;
import com3001.cw.ik00157.sportnearme.utilities.CommentsRecyclerView;
import com3001.cw.ik00157.sportnearme.utilities.NavigationHelper;
import com3001.cw.ik00157.sportnearme.utilities.TimeHelper;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsActivity extends AppCompatActivity {

    private final String TAG = "COMMENTS_ACTIVITY";
    private FirebaseAuth mAuth;
    private DatabaseReference database, likesRef, eventsAndMembersRef, dbCommentsRef;
    private static DatabaseReference dbPostRef, newCommentRef;
    private TimeHelper timeHelper;
    private CircleImageView civPostUserPhoto;
    private TextView tvPostCreatorDisplayName, tvPostTimeCreated, tvPostBody, tvNrLikes, tvNrEventMembers, tvPostSport;
    private ImageButton btnLike, btnGoingToEvent, btnMoreOptions;
    private boolean processingLikeAction, processingGoingToEventAction = false;

    private String postId;
    private ImageButton btnPostRowComment;
    private Button btnSubmitComment;
    private EditText etNewComment;
    private ProgressDialog progressDialog;
    private CommentsRecyclerView commentsRecyclerView;
    private RecyclerView rvCommentsList;
    private NavigationHelper navigationHelper;

    private void setTvNrLikesOnClickListener(final String postId){
        tvNrLikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatabaseReference postLikesRef = likesRef.child(postId);
                navigationHelper.setRef(postLikesRef);
                startActivity(new Intent(CommentsActivity.this, UserNamesAndPhotosListActivity.class));
                finish();
                Log.i(TAG, "finish()'ed CommentsActivity");
                overridePendingTransition(0, 0);
            }
        });
    }

    private void setTvNrEventMembersOnClickListener(final String postId){
        tvNrEventMembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference eventMembersRef = eventsAndMembersRef.child(postId);
                navigationHelper.setRef(eventMembersRef);
                startActivity(new Intent(CommentsActivity.this, UserNamesAndPhotosListActivity.class));
                finish();
                Log.i(TAG, "finish()'ed CommentsActivity");
                overridePendingTransition(0, 0);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        postId = getIntent().getStringExtra("postId");

        btnPostRowComment = findViewById(R.id.btn_comment);
        btnPostRowComment.setVisibility(View.GONE);

        this.timeHelper = new TimeHelper(this.TAG);
        navigationHelper = NavigationHelper.getInstance();
        mAuth = FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance().getReference();
        likesRef = database.child("likes");
        dbPostRef = database.child("posts");
        eventsAndMembersRef = database.child("eventsAndMembers");
        dbCommentsRef = FirebaseDatabase.getInstance().getReference().child("comments").child(postId);
        progressDialog = new ProgressDialog(this);

        civPostUserPhoto = findViewById(R.id.post_user_photo);
        tvPostBody = findViewById(R.id.post_body);
        tvPostCreatorDisplayName = findViewById(R.id.post_creator_display_name);
        tvPostTimeCreated = findViewById(R.id.post_time_created);
        tvPostSport = findViewById(R.id.post_sport);
        btnLike = findViewById(R.id.btn_like);
        tvNrLikes = findViewById(R.id.tv_nr_likes);
        btnGoingToEvent = findViewById(R.id.btn_going_to_event);
        tvNrEventMembers = findViewById(R.id.tv_nr_event_members);
        btnMoreOptions = findViewById(R.id.btn_more_options);
        btnSubmitComment = findViewById(R.id.btn_submit_comment);
        etNewComment = findViewById(R.id.et_new_comment);
        rvCommentsList = findViewById(R.id.rv_comments_list);

        setRigidData(postId);
        setLikeBtn(postId);
        setNrLikesTv(postId);
        setGoingToEventBtn(postId);
        setNrEventMembers(postId);
        setTvNrLikesOnClickListener(postId);
        setTvNrEventMembersOnClickListener(postId);

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
                                        if (dataSnapshot.exists()) {
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

                    }
                });
            }
        });

        btnMoreOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Clicked on post's more options button");
                Intent moreOptionsPopUp = new Intent(CommentsActivity.this, PostMoreOptionsPopup.class);
                moreOptionsPopUp.putExtra("postId", postId);
                startActivity(moreOptionsPopUp);
            }
        });

        btnSubmitComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newCommentText = etNewComment.getText().toString().trim();
                if(newCommentText.equals("")){
                    Toast.makeText(CommentsActivity.this, "The comment cannot be empty", Toast.LENGTH_LONG).show();
                } else if(newCommentText.length() > 2000){
                    Toast.makeText(CommentsActivity.this, "The comment cannot exceed 2000 characters. Currently the comment has " + newCommentText.length() + " characters", Toast.LENGTH_LONG).show();
                } else {
                    submitComment(newCommentText);
                }
            }
        });

        showComments();
    }

    private void showComments(){
        Query query = FirebaseDatabase.getInstance().getReference().child("comments").child(postId);
        commentsRecyclerView = new CommentsRecyclerView(query,
                this,
                this,
                postId,
                TAG);
        commentsRecyclerView.displayRV(rvCommentsList);
    }

    private void submitComment(final String newCommentText){
        progressDialog.setMessage("Submitting the comment..");
        progressDialog.show();
        final FirebaseUser user = mAuth.getCurrentUser();
        DatabaseReference userRef = database.child("users").child(user.getUid());
        DatabaseReference userPhotoUrlRef = userRef.child("photoUrl");
        userPhotoUrlRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String photoUrl;
                if(dataSnapshot.exists()){
                    photoUrl = dataSnapshot.getValue().toString();
                }
                else {
                    photoUrl = "";
                }

                Comment newComment = new Comment();
                newComment.setPhotoUrl(photoUrl);
                newComment.setText(newCommentText);
                newComment.setCreatorDisplayName(mAuth.getCurrentUser().getDisplayName());
                String timeCreated = timeHelper.getTimestamp();
                newComment.setTimeCreated(timeCreated);
                newComment.setUid(mAuth.getCurrentUser().getUid());

                newCommentRef = dbCommentsRef.push();
                newCommentRef.setValue(newComment);
                etNewComment.setText("");
                progressDialog.dismiss();
                rvCommentsList.smoothScrollToPosition(rvCommentsList.getAdapter().getItemCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });
    }

    public void setRigidData(final String postId){
        dbPostRef.child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                try{
                    Picasso.get().load(dataSnapshot.child("photoUrl").getValue().toString()).placeholder(R.drawable.profile_pic_placeholder).into(civPostUserPhoto);
                } catch(Exception e){
                    e.printStackTrace();
                    civPostUserPhoto.setImageResource(R.drawable.profile_pic_placeholder);
                }
                tvPostCreatorDisplayName.setText(dataSnapshot.child("creatorDisplayName").getValue().toString());
                setPostTimeCreated(dataSnapshot.child("timeCreated").getValue().toString());
                setPostSport(dataSnapshot.child("sport").getValue().toString());
                tvPostBody.setText(dataSnapshot.child("body").getValue().toString());

                civPostUserPhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(mAuth.getCurrentUser().getUid().equals(dataSnapshot.child("uid").getValue().toString()) ){
                            Log.i(TAG, "Post uid and current user's uid equal");
                            startActivity(new Intent(CommentsActivity.this, ProfileActivity.class));
                            finish();
                            overridePendingTransition(0, 0);
                        } else{
                            Log.i(TAG, "Post uid and current user's uid do not equal");
                            startActivity(new Intent(CommentsActivity.this, HomeActivity.class));
                            finish();
                            Log.i(TAG, "finish()'ed activity CommentsActivity");
                            overridePendingTransition(0, 0);
                            NavigationHelper navigationHelper = NavigationHelper.getInstance();
                            navigationHelper.setGoToDiffUsersFrag(true);
                            navigationHelper.setDiffUsersId(dataSnapshot.child("uid").getValue().toString());
                        }
                    }
                });
                tvPostCreatorDisplayName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(mAuth.getCurrentUser().getUid().equals(dataSnapshot.child("uid").getValue().toString()) ){
                            Log.i(TAG, "Post uid and current user's uid equal");
                            startActivity(new Intent(CommentsActivity.this, ProfileActivity.class));
                            finish();
                            overridePendingTransition(0, 0);
                        } else{
                            Log.i(TAG, "Post uid and current user's uid do not equal");
                            startActivity(new Intent(CommentsActivity.this, HomeActivity.class));
                            finish();
                            Log.i(TAG, "finish()'ed activity CommentsActivity");
                            overridePendingTransition(0, 0);
                            NavigationHelper navigationHelper = NavigationHelper.getInstance();
                            navigationHelper.setGoToDiffUsersFrag(true);
                            navigationHelper.setDiffUsersId(dataSnapshot.child("uid").getValue().toString());
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void setPostTimeCreated(String postTimeCreated){
        tvPostTimeCreated.setText(timeHelper.getHowLongAgoCreatedFriendlyFormat(postTimeCreated));
    }

    public void setPostSport(String sport){
        if(sport != null){
            tvPostSport.setText(" -  " + sport);
        }
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

    public void setGoingToEventBtn(final String postId){
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

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, HomeActivity.class));
        super.onBackPressed();
    }
}
