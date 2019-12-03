package com3001.cw.ik00157.sportnearme.activities;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;

import com3001.cw.ik00157.sportnearme.R;

public class NoGooglePlayServicesActivity extends AppCompatActivity {

    Button btnTryAgain;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_google_play_services);

        btnTryAgain = findViewById(R.id.btn_try_again);

        btnTryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isGooglePlayServicesAvailable(NoGooglePlayServicesActivity.this)){
                    // Do nothing
                } else if(mAuth.getCurrentUser() == null){
                    // User is not logged in
                    Intent signInIntent = new Intent(NoGooglePlayServicesActivity.this, SignInActivity.class);
                    startActivity(signInIntent);
                    finish();
                }
                else{
                    // User is logged in and user's device has google play services
                    Intent mainIntent = new Intent(NoGooglePlayServicesActivity.this, HomeActivity.class);
                    startActivity(mainIntent);
                    finish();
                }
            }
        });
    }

    public boolean isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if(status != ConnectionResult.SUCCESS) {
            if(googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 2404).show();
            }
            return false;
        }
        return true;
    }
}
