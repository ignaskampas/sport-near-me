package com3001.cw.ik00157.sportnearme.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import com3001.cw.ik00157.sportnearme.R;
import com3001.cw.ik00157.sportnearme.models.User;
import com3001.cw.ik00157.sportnearme.utilities.ContactsRecyclerView;
import com3001.cw.ik00157.sportnearme.utilities.PostRecyclerView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment {

    private static final String TAG = "CONTACTS_FRAGMENT";
    private User user;
    private View contactsView;
    private ContactsRecyclerView contactsRecyclerView;

    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        contactsView = inflater.inflate(R.layout.fragment_contacts, container, false);
        user = new User();
        user.setUid(FirebaseAuth.getInstance().getCurrentUser().getUid());
        return contactsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        showContacts();
    }

    private void showContacts(){
        Query query = FirebaseDatabase.getInstance().getReference().child("contacts").child(user.getUid()).orderByChild("displayName");
        contactsRecyclerView = new ContactsRecyclerView(query,
                getContext(),
                contactsView,
                TAG);
        contactsRecyclerView.displayRV();
    }
}
