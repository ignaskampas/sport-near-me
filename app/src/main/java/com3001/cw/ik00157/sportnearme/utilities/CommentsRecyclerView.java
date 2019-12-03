package com3001.cw.ik00157.sportnearme.utilities;

import android.app.Activity;
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
import com3001.cw.ik00157.sportnearme.models.Comment;


public class CommentsRecyclerView {

    private final String TAG;
    private LinearLayoutManager layoutManager;
    final private Context ctx;
    private RecyclerView rvCommentsList;
    private Query query;
    private Activity activity;
    private String postId;

    public CommentsRecyclerView(Query query,
                                final Activity activity,
                                final Context ctx,
                                final String postId,
                                final String TAG) {

        this.query = query;
        this.activity = activity;
        this.ctx = ctx;
        this.postId = postId;
        this.TAG = TAG;
    }

    public void displayRV(RecyclerView rvCommentsList){
        rvCommentsList = activity.findViewById(R.id.rv_comments_list);

        layoutManager = new LinearLayoutManager(this.ctx);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        rvCommentsList.setLayoutManager(layoutManager);

        //dbCommentLikesRef.keepSynced(true);
        //dbPostRef.keepSynced(true);

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Comment>()
                        .setQuery( query,
                                Comment.class)
                        .build();

        final FirebaseRecyclerAdapter<Comment, CommentViewHolder> adapter
                = new FirebaseRecyclerAdapter<Comment, CommentViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull final CommentViewHolder holder, int position, @NonNull final Comment comment) {
                final String commentId = getRef(position).getKey();
                holder.onBindViewHolder(comment, postId, commentId);
            }

            @NonNull
            @Override
            public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_row, parent, false);
                CommentViewHolder viewHolder = new CommentViewHolder(view, ctx, TAG);
                return viewHolder;
            }
        };

        rvCommentsList.setAdapter(adapter);
        adapter.startListening();
    }

}
