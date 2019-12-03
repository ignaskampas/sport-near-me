package com3001.cw.ik00157.sportnearme.activities;

import android.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com3001.cw.ik00157.sportnearme.R;
import com3001.cw.ik00157.sportnearme.utilities.BottomNavBarHelper;

public class ProfileActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener{

    private static final String TAG = "PROFILE_ACTIVITY";
    private static BottomNavBarHelper bottomNavBarHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        bottomNavBarHelper = new BottomNavBarHelper();
        bottomNavBarHelper.setUpBottomNavBar(ProfileActivity.this, getSupportFragmentManager(), findViewById(R.id.bottom_nav), R.id.nav_search);
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

    }
}
