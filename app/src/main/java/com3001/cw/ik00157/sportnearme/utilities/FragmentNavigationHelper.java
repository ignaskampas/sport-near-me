package com3001.cw.ik00157.sportnearme.utilities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com3001.cw.ik00157.sportnearme.R;

public class FragmentNavigationHelper {

    private FragmentActivity fragmentActivity;

    public FragmentNavigationHelper(FragmentActivity fragmentActivity){
        this.fragmentActivity = fragmentActivity;
    }

    public void changeFragInContainerAddToBackstack(Fragment frag, Bundle bundle,  int viewId, String backstackEntryName){
        Fragment fragment = frag;
        frag.setArguments(bundle);
        fragmentActivity.getSupportFragmentManager().beginTransaction().replace(viewId,
                fragment, null).addToBackStack(backstackEntryName).commit();
    }

}
