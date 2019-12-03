package com3001.cw.ik00157.sportnearme.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import com3001.cw.ik00157.sportnearme.R;
import com3001.cw.ik00157.sportnearme.utilities.NavigationHelper;
import com3001.cw.ik00157.sportnearme.utilities.UserNamesAndPhotosListRV;

public class UserNamesAndPhotosListActivity extends AppCompatActivity {

    private static final String TAG = "UserNamesAndPhotosList";
    private NavigationHelper navigationHelper;
    private DatabaseReference ref;
    private UserNamesAndPhotosListRV userNamesAndPhotosListRV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_names_and_photos_list);

        navigationHelper = navigationHelper.getInstance();
        ref = navigationHelper.getRef();
        navigationHelper.setRef(null);
        showList(ref);
    }

    private void showList(DatabaseReference ref){
        Query query = ref;
        userNamesAndPhotosListRV = new UserNamesAndPhotosListRV(query,
                this,
                this,
                TAG);
        userNamesAndPhotosListRV.displayRV();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, HomeActivity.class));
        overridePendingTransition(0,0);
        finish();
        Log.i(TAG, "onBackPressed: finish()'ed UserNamesAndPhotosListActivity");
        super.onBackPressed();
    }
}
