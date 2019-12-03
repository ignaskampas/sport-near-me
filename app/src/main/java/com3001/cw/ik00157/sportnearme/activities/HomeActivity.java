package com3001.cw.ik00157.sportnearme.activities;

import android.app.FragmentManager;
import android.content.Intent;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import com3001.cw.ik00157.sportnearme.R;
import com3001.cw.ik00157.sportnearme.fragments.BaseFragment;
import com3001.cw.ik00157.sportnearme.utilities.BottomNavBarHelper;

public class HomeActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener{

    private FragmentManager fm;
    private FirebaseAuth mAuth;
    private static final String TAG = "HOME_ACTIVITY";
    private static BottomNavBarHelper bottomNavBarHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if(!isLoggedIn()){
            startSignInIntent();
        } else{
            Log.d(TAG, "onCreate: user is logged in");
            fm = getFragmentManager();
            fm.addOnBackStackChangedListener(this);

            bottomNavBarHelper = new BottomNavBarHelper();
            bottomNavBarHelper.setUpBottomNavBar(HomeActivity.this, getSupportFragmentManager(), findViewById(R.id.bottom_nav), R.id.nav_news_feed);
        }

    }

    public static BottomNavBarHelper getBottomNavBarHelper(){
        return bottomNavBarHelper;
    }

    public void startSignInIntent(){
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    private boolean isLoggedIn(){
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        return currentUser != null;
    }

    private FirebaseUser getCurrentUser(){
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return bottomNavBarHelper.onCreateOptionsMenu(menu, getMenuInflater());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return bottomNavBarHelper.onOptionsItemSelected(item, this, super.onOptionsItemSelected(item));
    }

    @Override
    public void onBackStackChanged() {
        Log.d(TAG, "onBackStackChanged:");
        int num = fm.getBackStackEntryCount();
        String backStackEntryNames = "";

        for (int i = num-1; i>=0; i--){
            FragmentManager.BackStackEntry backStackEntry = fm.getBackStackEntryAt(i);
            backStackEntryNames += backStackEntry.getName() + "\n";
        }
        Log.d(TAG, "Backstack entry count: " + num + "\n" +
                "Backstack entries:\n" +
                backStackEntryNames);
    }

    @Override
    public void onBackPressed() {
        callFragmentsOnBackPressed();
        super.onBackPressed();
    }

    private void callFragmentsOnBackPressed(){
        List<Fragment> frags = getSupportFragmentManager().getFragments();
        for(Fragment f : frags){
            if(f != null && f instanceof BaseFragment){
                ((BaseFragment)f).onBackPressed();
            }
        }
    }
}
