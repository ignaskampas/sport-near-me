package com3001.cw.ik00157.sportnearme.utilities;

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
import com.google.firebase.database.Query;

import com3001.cw.ik00157.sportnearme.R;
import com3001.cw.ik00157.sportnearme.models.UserNameAndPhotoUrl;

public class ChatsRecyclerView {

    private final String TAG;
    private LinearLayoutManager layoutManager;
    private FragmentNavigationHelper fragmentNavigationHelper;
    final private Context ctx;
    final private FragmentActivity fragmentActivity;
    private RecyclerView rvChatsList;
    private View chatsView;
    private Query query;

    public ChatsRecyclerView(Query query,
                             final FragmentActivity fragmentActivity,
                             final Context ctx,
                             View chatsView,
                             final String TAG) {

        this.query = query;
        this.fragmentActivity = fragmentActivity;
        this.ctx = ctx;
        this.chatsView = chatsView;
        this.TAG = TAG;
    }

    public void displayRV(){
        rvChatsList = chatsView.findViewById(R.id.rv_chats_list);

        layoutManager = new LinearLayoutManager(this.ctx);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        rvChatsList.setLayoutManager(layoutManager);

        fragmentNavigationHelper = new FragmentNavigationHelper(this.fragmentActivity);

        //dbLikesRef.keepSynced(true);
        //dbPostRef.keepSynced(true);

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<UserNameAndPhotoUrl>()
                        .setQuery( query,
                                UserNameAndPhotoUrl.class)
                        .build();

        final FirebaseRecyclerAdapter<UserNameAndPhotoUrl, ChatViewHolder> adapter
                = new FirebaseRecyclerAdapter<UserNameAndPhotoUrl, ChatViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull final ChatViewHolder holder, int position, @NonNull final UserNameAndPhotoUrl userNameAndPhotoUrl) {
                final String chatId = getRef(position).getKey();
                holder.onBindViewHolder(chatId);
            }

            @NonNull
            @Override
            public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_row, parent, false);
                ChatViewHolder viewHolder = new ChatViewHolder(view, fragmentActivity, ctx, fragmentNavigationHelper, TAG);
                return viewHolder;
            }
        };

        rvChatsList.setAdapter(adapter);
        adapter.startListening();
    }

}
