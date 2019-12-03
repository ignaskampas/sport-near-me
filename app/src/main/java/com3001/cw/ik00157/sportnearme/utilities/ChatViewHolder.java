package com3001.cw.ik00157.sportnearme.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import com3001.cw.ik00157.sportnearme.R;
import com3001.cw.ik00157.sportnearme.activities.ConvoActivity;
import com3001.cw.ik00157.sportnearme.activities.HomeActivity;
import com3001.cw.ik00157.sportnearme.models.UserNameAndPhotoUrl;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatViewHolder extends RecyclerView.ViewHolder{

    private final String TAG;

    private FirebaseAuth mAuth;
    private FragmentNavigationHelper fragmentNavigationHelper;
    private DatabaseReference database;
    private final Context ctx;
    private final FragmentActivity fragmentActivity;
    private TimeHelper timeHelper;


    private CircleImageView civUserPhoto;
    private TextView tvDisplayName;

    public ChatViewHolder(View itemView, final FragmentActivity fragmentActivity, final Context ctx, FragmentNavigationHelper fragmentNavigationHelper, final String TAG) {
        super(itemView);

        this.fragmentActivity = fragmentActivity;
        this.ctx = ctx;
        this.fragmentNavigationHelper = fragmentNavigationHelper;
        this.TAG = TAG;

        this.timeHelper = new TimeHelper(this.TAG);
        mAuth = FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance().getReference();

        //likesRef.keepSynced(true);

        civUserPhoto = itemView.findViewById(R.id.user_photo);
        tvDisplayName = itemView.findViewById(R.id.display_name);
    }

    public void onBindViewHolder(final String diffUserId){
        database.child("users").child(diffUserId).child("displayName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    tvDisplayName.setText(dataSnapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        database.child("users").child(diffUserId).child("photoUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    try{
                        Picasso.get().load(dataSnapshot.getValue().toString()).placeholder(R.drawable.profile_pic_placeholder).into(civUserPhoto);
                    }catch (Exception e){
                        e.printStackTrace();
                        civUserPhoto.setImageResource(R.drawable.profile_pic_placeholder);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent convoActivityIntent = new Intent(ctx, ConvoActivity.class);
                convoActivityIntent.putExtra("diffUsersId", diffUserId);
                ctx.startActivity(convoActivityIntent);
                if(ctx instanceof Activity){
                    ((Activity) ctx).finish();
                    Log.i(TAG, "finish()'ed activity ChatsAndContactsActivity");
                }
            }
        });
    }

}
