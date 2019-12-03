package com3001.cw.ik00157.sportnearme.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import com3001.cw.ik00157.sportnearme.R;
import com3001.cw.ik00157.sportnearme.models.User;
import com3001.cw.ik00157.sportnearme.utilities.ChatsRecyclerView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private static final String TAG = "CHATS_FRAGMENT";
    private View chatsView;
    private ChatsRecyclerView chatsRecyclerView;
    private User user;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        chatsView = inflater.inflate(R.layout.fragment_chats, container, false);
        user = new User();
        user.setUid(FirebaseAuth.getInstance().getCurrentUser().getUid());
        return chatsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        showChats();
    }


    private void showChats(){
        Query query = FirebaseDatabase.getInstance().getReference().child("conversations").child(user.getUid()).orderByChild("lastMessageTimeFromFixedDate");
        chatsRecyclerView = new ChatsRecyclerView(query,
                getActivity(),
                getContext(),
                chatsView,
                TAG);
        chatsRecyclerView.displayRV();
    }
}
