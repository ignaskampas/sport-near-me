package com3001.cw.ik00157.sportnearme.activities.popupwindows;

import android.app.ActionBar;
import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Locale;

import com3001.cw.ik00157.sportnearme.R;

public class PostMoreOptionsPopup extends Activity {

    private DatabaseReference postRef;
    private DatabaseReference postLatRef;
    private DatabaseReference postLongRef;

    private String postId;
    private Button btnCancel;

    private TextView tvPostAddress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_window_post_more_options);

        // dm contains the width and height of the screen
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        getWindow().setLayout((int)(width*.8), ActionBar.LayoutParams.WRAP_CONTENT);

        postId = getIntent().getStringExtra("postId");
        tvPostAddress = findViewById(R.id.tv_post_address);

        postRef = FirebaseDatabase.getInstance().getReference().getRef().child("posts").child(postId);
        postLatRef = postRef.child("latitude");
        postLongRef = postRef.child("longitude");

        postLatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshotLat) {
                postLongRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshotLong) {
                        double latitude = Double.valueOf(dataSnapshotLat.getValue().toString());
                        double longitude = Double.valueOf(dataSnapshotLong.getValue().toString());
                        displayPostAddress(latitude, longitude);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btnCancel = findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void displayPostAddress(double latitude, double longitude){
        List<Address> addresses;

        Geocoder geocoder = new Geocoder(PostMoreOptionsPopup.this, Locale.getDefault());

        try{
            addresses = geocoder.getFromLocation(latitude, longitude, 1);

            String houseNumber = addresses.get(0).getFeatureName(); // gets house number
            String streetName = addresses.get(0).getThoroughfare();
            String area = addresses.get(0).getLocality();
            String city = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalcode = addresses.get(0).getPostalCode();

            String fullAddress = "";

            if(houseNumber != null){
                fullAddress += houseNumber + " ";
            }
            if(streetName != null){
                fullAddress += streetName;
            }
            if(area != null){
                fullAddress += "\n" + area;
            }
            if(city != null){
                fullAddress += "\n" + city;
            }
            if(country != null){
                fullAddress += "\n" + country;
            }
            if(postalcode != null){
                fullAddress += "\n" + postalcode;
            }

            tvPostAddress.setText(fullAddress);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
