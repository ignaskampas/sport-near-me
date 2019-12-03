package com3001.cw.ik00157.sportnearme.utilities;

import com.google.firebase.database.DatabaseReference;

public class NavigationHelper {

    private static NavigationHelper navigationHelper = null;
    private boolean goToDiffUsersFrag = false;
    private String diffUsersId = "";
    private DatabaseReference ref;

    private NavigationHelper(){
    }

    public static NavigationHelper getInstance(){
        if(navigationHelper == null){
            navigationHelper = new NavigationHelper();
        }
        return navigationHelper;
    }

    public boolean getGoToDiffUsersFrag(){
        return goToDiffUsersFrag;
    }

    public void setGoToDiffUsersFrag(boolean goToDiffUsersFrag){
        this.goToDiffUsersFrag = goToDiffUsersFrag;
    }

    public String getDiffUsersId() {
        return diffUsersId;
    }

    public void setDiffUsersId(String diffUsersId) {
        this.diffUsersId = diffUsersId;
    }

    public DatabaseReference getRef() {
        return ref;
    }

    public void setRef(DatabaseReference ref) {
        this.ref = ref;
    }
}
