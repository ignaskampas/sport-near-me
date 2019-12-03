package com3001.cw.ik00157.sportnearme.utilities;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;

import com3001.cw.ik00157.sportnearme.R;
import com3001.cw.ik00157.sportnearme.activities.AddPostActivity;
import com3001.cw.ik00157.sportnearme.activities.ChatsAndContactsActivity;
import com3001.cw.ik00157.sportnearme.activities.HomeActivity;
import com3001.cw.ik00157.sportnearme.activities.ProfileActivity;
import com3001.cw.ik00157.sportnearme.activities.SignInActivity;
import com3001.cw.ik00157.sportnearme.fragments.NewsFeedFragment;
import com3001.cw.ik00157.sportnearme.fragments.ProfileFragment;

public class BottomNavBarHelper {

    private static final String TAG = "BOTTOM_NAV_BAR_HELPER";

    // this can also be used to figure out which activity is using this class
    private int currentNavBarIconId;
    private FragmentManager fm;
    private Context ctx;
    private BottomNavigationView bottomNav;

    // Change this method name to initialise dependencies for top nav bar?
    public void initialiseTopNavBar(Context ctx, FragmentManager fm){
        this.ctx = ctx;
        this.fm = fm;
    }

    // Change this method name to initialise dependencies?
    public void setUpBottomNavBar(Context ctx, FragmentManager fm, View v, int currentNavBarIconId){
        this.currentNavBarIconId = currentNavBarIconId;
        this.ctx = ctx;
        this.fm = fm;
        bottomNav = (BottomNavigationView) v;
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        if(currentNavBarIconId == R.id.nav_news_feed) {
            // causes the home fragment to be shown when the user goes to Home Activity.
            this.fm.beginTransaction().replace(R.id.fragment_container,
                    new NewsFeedFragment()).commit();
            bottomNav.setSelectedItemId(R.id.nav_news_feed);
        }
        if(currentNavBarIconId == R.id.nav_search) {
            // causes the home fragment to be shown when the user goes to Home Activity.
            this.fm.beginTransaction().replace(R.id.fragment_container,
                    new ProfileFragment()).commit();
            bottomNav.setSelectedItemId(R.id.nav_search);
        }
        if(currentNavBarIconId == R.id.nav_chat) {
            // causes the home fragment to be shown when the user goes to Home Activity.
//            this.fm.beginTransaction().replace(R.id.fragment_container,
//                    new ChatFragment()).commit();
            bottomNav.setSelectedItemId(R.id.nav_chat);
        }
    }

    public void setSelectedItemId(int id){
        this.bottomNav.setSelectedItemId(id);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    switch(item.getItemId()){
                        case R.id.nav_chat:
                            // clicked on nav bar's chat icon
                            if(currentNavBarIconId == R.id.nav_chat){
                                // currently in chat activity and clicked on chat icon
                                break;
                            } else {
                                ctx.startActivity(new Intent(ctx, ChatsAndContactsActivity.class));
                                Log.i(TAG, "startActivity called to ChatsAndContactsAcitivity");
                                // checks if context is an instance of Activity. It makes sure that
                                // the method is really there
                                if(ctx instanceof  Activity){
                                    ((Activity) ctx).finish();
                                    ((Activity) ctx).overridePendingTransition(0, 0);
                                }
                                break;
                            }

                        case R.id.nav_news_feed:
                            // clicked on nav bar's news feed icon
                            if (currentNavBarIconId == R.id.nav_news_feed){
                                // currently on HomeActivity
                                selectedFragment = new NewsFeedFragment();
                                fm.beginTransaction().replace(R.id.fragment_container,
                                        selectedFragment).commit();
                                Log.i(TAG, "Made fragment transaction to news feed FRAGMENT");
                                break;
                            } else {
                                // currently on ProfileActivity
                                ctx.startActivity(new Intent(ctx, HomeActivity.class));
                                Log.i(TAG, "startActivity called to HomeActivity");
                                // checks if context is an instance of Activity. It makes sure that
                                // the method is really there
                                if(ctx instanceof  Activity){
                                    ((Activity) ctx).finish();
                                    ((Activity) ctx).overridePendingTransition(0, 0);
                                }
                                break;
                            }

                        case R.id.nav_search:
                            // clicked on nav bar's profile icon
                            if(currentNavBarIconId == R.id.nav_search){
                                // currently on ProfileActivity and clicked on profile icon

                                selectedFragment = new ProfileFragment();
                                fm.beginTransaction().replace(R.id.fragment_container,
                                        selectedFragment).commit();
                                Log.i(TAG, "Made fragment transaction to profile FRAGMENT");
                                break;
                            }else{
                                // currently in not chat activity and clicked on profile icon
                                ctx.startActivity(new Intent(ctx, ProfileActivity.class));
                                Log.i(TAG, "startActivity called to ProfileActivity");
                                if(ctx instanceof  Activity){
                                    ((Activity) ctx).finish();
                                    ((Activity) ctx).overridePendingTransition(0, 0);
                                }
                                break;
                            }
                    }
                    // true means to select the clicked item.
                    return true;
                }
            };

    public boolean onCreateOptionsMenu(Menu menu, MenuInflater menuInflater){
        MenuInflater inflater = menuInflater;
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onCreateOptionsMenuWithoutAddPost(Menu menu, MenuInflater menuInflater){
        MenuInflater inflater = menuInflater;
        inflater.inflate(R.menu.top_menu_without_add_post, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item, Activity activity, boolean superFromCallingACtivity){
        switch (item.getItemId()){
            case R.id.action_add:
                ctx.startActivity(new Intent(ctx, AddPostActivity.class));
                return true;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                LoginManager.getInstance().logOut();
                activity.finish();
                activity.startActivity(new Intent(activity, SignInActivity.class));
                return true;
            default:
                return superFromCallingACtivity;
        }
    }

    public boolean onOptionsItemSelectedWithoutAddPost(MenuItem item, Activity activity, boolean superFromCallingACtivity){
        switch (item.getItemId()){
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                LoginManager.getInstance().logOut();
                activity.finish();
                activity.startActivity(new Intent(activity, SignInActivity.class));
                return true;
            default:
                return superFromCallingACtivity;
        }
    }
}
