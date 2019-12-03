package com3001.cw.ik00157.sportnearme.activities;

import android.app.FragmentManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com3001.cw.ik00157.sportnearme.R;
import com3001.cw.ik00157.sportnearme.utilities.BottomNavBarHelper;
import com3001.cw.ik00157.sportnearme.utilities.TabsAccessorAdapter;

public class ChatsAndContactsActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener{

    private static final String TAG = "CHATS_AND_CONTACTS_ACTIVITY";
    private static BottomNavBarHelper bottomNavBarHelper;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private TabsAccessorAdapter tabsAccessorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats_and_contacts);

        bottomNavBarHelper = new BottomNavBarHelper();
        bottomNavBarHelper.setUpBottomNavBar(ChatsAndContactsActivity.this,
                getSupportFragmentManager(),
                findViewById(R.id.bottom_nav),
                R.id.nav_chat);

        viewPager = findViewById(R.id.tabs_pager);
        tabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabsAccessorAdapter);

        tabLayout = findViewById(R.id.chats_and_contacts_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return bottomNavBarHelper.onCreateOptionsMenuWithoutAddPost(menu, getMenuInflater());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return bottomNavBarHelper.onOptionsItemSelectedWithoutAddPost(item, this, super.onOptionsItemSelected(item));
    }

    @Override
    public void onBackStackChanged() {

    }
}
