package com3001.cw.ik00157.sportnearme.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import com3001.cw.ik00157.sportnearme.R;
import com3001.cw.ik00157.sportnearme.activities.ConvoActivity;
import com3001.cw.ik00157.sportnearme.activities.HomeActivity;
import com3001.cw.ik00157.sportnearme.models.UserNameAndPhotoUrl;
import de.hdodenhof.circleimageview.CircleImageView;

public class ContactViewHolder extends RecyclerView.ViewHolder{

    private final String TAG;
    private final Context ctx;
    private Button btnMessage;
    private CircleImageView civUserPhoto;
    private TextView tvDisplayName;

    public ContactViewHolder(View itemView, final Context ctx, final String TAG) {
        super(itemView);

        this.ctx = ctx;
        this.TAG = TAG;

        //likesRef.keepSynced(true);

        civUserPhoto = itemView.findViewById(R.id.user_photo);
        tvDisplayName = itemView.findViewById(R.id.display_name);
        btnMessage = itemView.findViewById(R.id.btn_message);
    }

    public void onBindViewHolder(@NonNull final UserNameAndPhotoUrl userNameAndPhotoUrl, final String contactId){
        try{
            Picasso.get().load(userNameAndPhotoUrl.getPhotoUrl()).placeholder(R.drawable.profile_pic_placeholder).into(civUserPhoto);
        }catch (Exception e){
            e.printStackTrace();
            civUserPhoto.setImageResource(R.drawable.profile_pic_placeholder);
        }
        tvDisplayName.setText(userNameAndPhotoUrl.getDisplayName());
        btnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent convoActivityIntent = new Intent(ctx, ConvoActivity.class);
                convoActivityIntent.putExtra("diffUsersId", contactId);
                ctx.startActivity(convoActivityIntent);
                if(ctx instanceof Activity){
                    ((Activity) ctx).finish();
                    Log.i(TAG, "finish()'ed activity ChatsAndContactsActivity");
                }
            }
        });
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ctx.startActivity(new Intent(ctx, HomeActivity.class));
                if(ctx instanceof Activity){
                    ((Activity) ctx).finish();
                    Log.i(TAG, "finish()'ed activity ChatsAndContactsActivity");
                    ((Activity) ctx).overridePendingTransition(0, 0);
                }
                NavigationHelper navigationHelper = NavigationHelper.getInstance();
                navigationHelper.setGoToDiffUsersFrag(true);
                navigationHelper.setDiffUsersId(contactId);
            }
        });
    }

}
