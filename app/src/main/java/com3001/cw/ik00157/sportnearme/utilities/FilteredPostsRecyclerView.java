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
import com3001.cw.ik00157.sportnearme.models.Post;

public class FilteredPostsRecyclerView {

    private final String TAG;
    private LinearLayoutManager layoutManager;
    private FragmentNavigationHelper fragmentNavigationHelper;
    final private Context ctx;
    final private FragmentActivity fragmentActivity;
    private RecyclerView rvPostList;
    private View newsFeedView;
    private Query query;

    public FilteredPostsRecyclerView(Query query,
                                     final FragmentActivity fragmentActivity,
                                     final Context ctx,
                                     View newsFeedView,
                                     final String TAG) {

        this.query = query;
        this.fragmentActivity = fragmentActivity;
        this.ctx = ctx;
        this.newsFeedView = newsFeedView;
        this.TAG = TAG;
    }

    public void displayRV(){
        rvPostList = newsFeedView.findViewById(R.id.rv_post_list);

        layoutManager = new LinearLayoutManager(this.ctx);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        rvPostList.setLayoutManager(layoutManager);

        fragmentNavigationHelper = new FragmentNavigationHelper(this.fragmentActivity);

        //dbPostRef.keepSynced(true);

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Post>()
                        .setQuery( query,
                                Post.class)
                        .build();

        final FirebaseRecyclerAdapter<Post, FilteredPostsViewHolder> adapter
                = new FirebaseRecyclerAdapter<Post, FilteredPostsViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull final FilteredPostsViewHolder holder, int position, @NonNull final Post post) {
                final String postId = getRef(position).getKey();
                holder.onBindViewHolder(postId);
            }

            @NonNull
            @Override
            public FilteredPostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_row, parent, false);
                FilteredPostsViewHolder viewHolder = new FilteredPostsViewHolder(view, fragmentActivity, ctx, fragmentNavigationHelper, TAG);
                return viewHolder;
            }
        };

        rvPostList.setAdapter(adapter);
        adapter.startListening();
    }

}
