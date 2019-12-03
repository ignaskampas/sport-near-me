package com3001.cw.ik00157.sportnearme.utilities;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import com3001.cw.ik00157.sportnearme.R;
import com3001.cw.ik00157.sportnearme.models.UserNameAndPhotoUrl;

public class UserNamesAndPhotosListRV {

    private final String TAG;
    private LinearLayoutManager layoutManager;
    final private Context ctx;
    private RecyclerView rv;
    private Query query;
    private Activity activity;

    public UserNamesAndPhotosListRV(Query query,
                                    final Activity activity,
                                    final Context ctx,
                                    final String TAG) {

        this.query = query;
        this.activity = activity;
        this.ctx = ctx;
        this.TAG = TAG;
    }

    public void displayRV(){
        rv = activity.findViewById(R.id.rv_user_names_and_photos_list);

        layoutManager = new LinearLayoutManager(this.ctx);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        rv.setLayoutManager(layoutManager);

        //dbLikesRef.keepSynced(true);
        //dbPostRef.keepSynced(true);

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<UserNameAndPhotoUrl>()
                        .setQuery( query,
                                UserNameAndPhotoUrl.class)
                        .build();

        final FirebaseRecyclerAdapter<UserNameAndPhotoUrl, UserNameAndPhotoViewHolder> adapter
                = new FirebaseRecyclerAdapter<UserNameAndPhotoUrl, UserNameAndPhotoViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull final UserNameAndPhotoViewHolder holder, int position, @NonNull final UserNameAndPhotoUrl userNameAndPhotoUrl) {
                final String userId = getRef(position).getKey();
                holder.onBindViewHolder(userNameAndPhotoUrl, userId);
            }

            @NonNull
            @Override
            public UserNameAndPhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_name_and_photo_url, parent, false);
                UserNameAndPhotoViewHolder viewHolder = new UserNameAndPhotoViewHolder(view, ctx, TAG);
                return viewHolder;
            }
        };

        rv.setAdapter(adapter);
        adapter.startListening();
    }

}
