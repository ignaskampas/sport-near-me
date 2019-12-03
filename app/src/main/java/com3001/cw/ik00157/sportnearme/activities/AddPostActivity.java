package com3001.cw.ik00157.sportnearme.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import com3001.cw.ik00157.sportnearme.models.Post;
import com3001.cw.ik00157.sportnearme.R;
import com3001.cw.ik00157.sportnearme.utilities.TimeHelper;

public class AddPostActivity extends AppCompatActivity {

    private static final String TAG = "AddPostActivity";

    private EditText etAddressNr, etAddressStreet, etAddressArea, etAddressCity, etAddressCountry, etAddressPostCode;
    private String post_id, sport;
    private EditText etPostText, etSport;
    private Button btnSubmit;
    private DatabaseReference dbPostsRef;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private DatabaseReference database;
    private TimeHelper timeHelper;
    private DatabaseReference newPostRef;
    private String postBody;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        database = FirebaseDatabase.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();
        dbPostsRef = FirebaseDatabase.getInstance().getReference().child("posts");


        etPostText = findViewById(R.id.post_text);
        etSport = findViewById(R.id.et_sport);
        etAddressNr = findViewById(R.id.address_nr);
        etAddressStreet = findViewById(R.id.address_street);
        etAddressArea = findViewById(R.id.address_area);
        etAddressCity = findViewById(R.id.address_city);
        etAddressCountry = findViewById(R.id.address_country);
        etAddressPostCode = findViewById(R.id.address_post_code);
        btnSubmit = findViewById(R.id.btn_submit);

        progressDialog = new ProgressDialog(this);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trySubmitPost();
            }
        });


    }

    private void writeNewPost(Post post){
        newPostRef.setValue(post);
    }

    private void trySubmitPost(){

        postBody = etPostText.getText().toString().trim();
        sport = etSport.getText().toString().trim().toLowerCase();
        String nr = etAddressNr.getText().toString().trim();
        String street = etAddressStreet.getText().toString().trim();
        String area = etAddressArea.getText().toString().trim();
        String city = etAddressCity.getText().toString().trim();
        String country = etAddressCountry.getText().toString().trim();
        String postCode = etAddressPostCode.getText().toString().trim();

        if(TextUtils.isEmpty(postBody)){
            Toast.makeText(this, "Post not saved. Event's description must be filled", Toast.LENGTH_LONG).show();
        } else if(sport.equals("")){
            Toast.makeText(this, "Post not saved. The event's sport must be specified", Toast.LENGTH_LONG).show();
        } else if (street.equals("") || postCode.equals("")){
            Toast.makeText(this, "Post not saved. The address street and post code must be filled", Toast.LENGTH_LONG).show();
        } else {
            progressDialog.setMessage("Submitting the post..");
            progressDialog.show();

            TaskParams taskParams = new TaskParams();
            taskParams.setNr(nr);
            taskParams.setStreet(street);
            taskParams.setArea(area);
            taskParams.setCity(city);
            taskParams.setCountry(country);
            taskParams.setPostCode(postCode);

            new AddressToLocationCoordinates().execute(taskParams);
        }
    }

    class AddressToLocationCoordinates extends AsyncTask<TaskParams, Void, Address> {

        @Override
        protected Address doInBackground(TaskParams... taskParams) {

            String nr = taskParams[0].getNr();
            String street = taskParams[0].getStreet();
            String area = taskParams[0].getArea();
            String city = taskParams[0].getCity();
            String country = taskParams[0].getCountry();
            String postCode = taskParams[0].getPostCode();

            Log.i(TAG, "AddressToLocationCoordinates: doInBackground:\n" +
                    "nr: " + nr + ", street: " + street + ", area: " + area + ", city: " + city + ", country: " + country + ", postCode: " + postCode);

            if(street.equals("") || postCode.equals("")){
                Log.i(TAG, "AddressToLocationCoordinates: doInBackground: the location street or postcode were strings with nothing in them");
                return null;
            }
            String strAddress = "";
            strAddress += nr + " " + street;
            if(!area.equals("")){
                strAddress += ", " + area;
            }
            if(!city.equals("")){
                strAddress += ", " + city;
            }
            if(!country.equals("")){
                strAddress += ", " + strAddress;
            }
            strAddress += ", " + postCode;

            Geocoder geocoder = new Geocoder(AddPostActivity.this);
            List<Address> address;
            Address location = null;

            try {
                address = geocoder.getFromLocationName(strAddress, 5);
                if(address == null){
                    return null;
                } else {
                    location = address.get(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            return location;
        }

        // invoked on the UI thread rather than the background thread
        protected void onPostExecute(final Address address){

            if(address == null)
            {
                Log.i(TAG, "AddressToLocationCoordinates: onPostExecute: Failed to convert address to location coordinates");
                progressDialog.dismiss();
                Toast.makeText(AddPostActivity.this, "Post not saved. The address is not valid", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "AddressToLocationCoordinates: onPostExecute: From address to coordinates: lat: " + address.getLatitude() + ", long: " + address.getLongitude());
                submitPost(address);
            }

        }
    }

    private void submitPost(final Address address){
        timeHelper = new TimeHelper(TAG);
        final FirebaseUser user = mAuth.getCurrentUser();
        DatabaseReference userRef = database.child("users").child(user.getUid());
        DatabaseReference userPhotoUrlRef = userRef.child("photoUrl");

        userPhotoUrlRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String photoUrl;
                if(dataSnapshot.exists()){
                    photoUrl = dataSnapshot.getValue().toString();
                }
                else {
                    photoUrl = "";
                }

                final Post newPost = new Post(user.getUid(), postBody, user.getDisplayName(), photoUrl);
                String timeCreated = timeHelper.getTimestamp();
                newPost.setTimeCreated(timeCreated);
                newPost.setTimeFromFixedDate(timeHelper.getTimeFromFixedDate(timeCreated));

                newPostRef = dbPostsRef.push();
                post_id = newPostRef.getKey();

                DatabaseReference dbPostGeoLocation = FirebaseDatabase.getInstance().getReference("geoLocations").child("posts");
                GeoFire gfPostGeoLocation = new GeoFire(dbPostGeoLocation);

//                usersLastLocation = UsersLastLocation.getInstance();
//                final double usersLat = usersLastLocation.getLatitude();
//                final double usersLong = usersLastLocation.getLongitude();

                gfPostGeoLocation.setLocation(post_id, new GeoLocation(address.getLatitude(), address.getLongitude()), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        if (error != null) {
                            Log.i(TAG, "There was an error saving the location to GeoFire: " + error);
                            progressDialog.dismiss();
                        } else {
                            Log.i(TAG, "Post's location has been saved under the geoLcoations node");
                            newPost.setLatitude(String.valueOf(address.getLatitude()));
                            newPost.setLongitude(String.valueOf(address.getLongitude()));
                            newPost.setSport(sport);
                            writeNewPost(newPost);

                            progressDialog.dismiss();
                            startActivity(new Intent(AddPostActivity.this, HomeActivity.class));
                            finish();
                        }
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i(TAG, "User photo url ref addValueEventListener cancelled");
                progressDialog.dismiss();
            }
        });
    }

    private static class TaskParams{

        private String nr;
        private String street;
        private String area;
        private String city;
        private String country;
        private String postCode;

        TaskParams(){
        }

        public String getNr() {
            return nr;
        }

        public void setNr(String nr) {
            this.nr = nr;
        }

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public String getArea() {
            return area;
        }

        public void setArea(String area) {
            this.area = area;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getPostCode() {
            return postCode;
        }

        public void setPostCode(String postCode) {
            this.postCode = postCode;
        }
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }

}
