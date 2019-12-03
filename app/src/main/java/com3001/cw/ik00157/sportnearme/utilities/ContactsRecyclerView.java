package com3001.cw.ik00157.sportnearme.utilities;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.Query;

import com3001.cw.ik00157.sportnearme.R;
import com3001.cw.ik00157.sportnearme.models.UserNameAndPhotoUrl;

public class ContactsRecyclerView {

    private final String TAG;
    private LinearLayoutManager layoutManager;
    final private Context ctx;
    private RecyclerView rvContactsList;
    private View contactsView;
    private Query query;

    public ContactsRecyclerView(Query query,
                                final Context ctx,
                                View contactsView,
                                final String TAG) {

        this.query = query;
        this.ctx = ctx;
        this.contactsView = contactsView;
        this.TAG = TAG;
    }

    public void displayRV(){
        rvContactsList = contactsView.findViewById(R.id.rv_contacts_list);

        layoutManager = new LinearLayoutManager(this.ctx);

        rvContactsList.setLayoutManager(layoutManager);

        //dbLikesRef.keepSynced(true);
        //dbPostRef.keepSynced(true);

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<UserNameAndPhotoUrl>()
                        .setQuery( query,
                                UserNameAndPhotoUrl.class)
                        .build();

        final FirebaseRecyclerAdapter<UserNameAndPhotoUrl, ContactViewHolder> adapter
                = new FirebaseRecyclerAdapter<UserNameAndPhotoUrl, ContactViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull final ContactViewHolder holder, int position, @NonNull final UserNameAndPhotoUrl userNameAndPhotoUrl) {
                final String contactId = getRef(position).getKey();
                holder.onBindViewHolder(userNameAndPhotoUrl, contactId);
            }

            @NonNull
            @Override
            public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_row, parent, false);
                ContactViewHolder viewHolder = new ContactViewHolder(view, ctx, TAG);
                return viewHolder;
            }
        };

        rvContactsList.setAdapter(adapter);
        adapter.startListening();
    }

}
