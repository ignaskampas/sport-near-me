package com3001.cw.ik00157.sportnearme.utilities;

import android.support.v4.app.FragmentManager;
import android.util.Log;

public class BackstackHelper {

    private FragmentManager fm;

    public BackstackHelper(FragmentManager fm){
        this.fm = fm;
    }

    public void logCurrentBackstack(String TAG){
        int num = fm.getBackStackEntryCount();
        String backStackEntryNames = "";

        for (int i = num-1; i>=0; i--){
            FragmentManager.BackStackEntry backStackEntry = fm.getBackStackEntryAt(i);
            backStackEntryNames += backStackEntry.getName() + "\n";
        }
        Log.d(TAG, "onBackStackChanged:" + "\n" + "Backstack entry count: " + num + "\n" +
                "Backstack entries:\n" +
                backStackEntryNames);
    }
}
