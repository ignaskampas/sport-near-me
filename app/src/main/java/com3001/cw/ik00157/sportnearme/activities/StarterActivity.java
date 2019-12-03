package com3001.cw.ik00157.sportnearme.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;

public class StarterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        if(!isGooglePlayServicesAvailable(this)){
            Intent noGooglePlayServicesIntent = new Intent(this, NoGooglePlayServicesActivity.class);
            startActivity(noGooglePlayServicesIntent);
            finish();
        }
        else if(mAuth.getCurrentUser() == null){
            // User is not logged in
            Intent signInIntent = new Intent(this, SignInActivity.class);
            startActivity(signInIntent);
            finish();
        }
        else{
            // User is logged in and user's device has google play services
            Intent mainIntent = new Intent(this, HomeActivity.class);
            startActivity(mainIntent);
            finish();
        }
    }

    public boolean isGooglePlayServicesAvailable(Context context){
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        return resultCode == ConnectionResult.SUCCESS;
    }
}
