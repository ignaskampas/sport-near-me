package com3001.cw.ik00157.sportnearme.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.util.ArrayList;

import com3001.cw.ik00157.sportnearme.FusedLocationProvider;
import com3001.cw.ik00157.sportnearme.R;
import com3001.cw.ik00157.sportnearme.activities.FilterOptionsActivity;
import com3001.cw.ik00157.sportnearme.models.UsersLastLocation;
import com3001.cw.ik00157.sportnearme.utilities.BackstackHelper;
import com3001.cw.ik00157.sportnearme.utilities.FilteredPostsRecyclerView;
import com3001.cw.ik00157.sportnearme.utilities.FragmentNavigationHelper;
import com3001.cw.ik00157.sportnearme.utilities.NavigationHelper;
import com3001.cw.ik00157.sportnearme.utilities.PostRecyclerView;

import static android.content.Context.MODE_PRIVATE;

public class NewsFeedFragment extends Fragment implements FragmentManager.OnBackStackChangedListener{

    private static final String TAG = "NEWS_FEED_FRAGMENT";

    private static String SHARED_PREFS;
    private static final String FILTER_APPLIED = "filterApplied";
    private static final String FILTER_BY_LOCATION = "filterByLocation";
    private static final String FILTER_BY_SPORT = "filterBySport";
    private static final String SPORT = "sport";
    private static final String RADIUS = "radius";
    private static final String LENGTH_UNIT = "lengthUnit";
    private String userId;
    private boolean debugDisplayPosts = true;
    private boolean filteredPostsAlreadyDisplayed = false;

    private View newsFeedView;
    private Button btnFilterOptions;
    private PostRecyclerView postRecyclerView;
    private FilteredPostsRecyclerView filteredPostsRecyclerView;
    private TextView tvLocationNotPermittedExplanation;

    private FragmentManager fm;
    private FragmentNavigationHelper fragmentNavigationHelper;
    private BackstackHelper backstackHelper;
    private SharedPreferences sharedPreferences;
    private DatabaseReference database;
    private DatabaseReference usersFilteredPostsRef;
    private DatabaseReference postsRef;
    private UsersLastLocation usersLastLocation;
    private FusedLocationProvider fusedLocationProvider;
    private GeoQuery geoQuery;
    private GeoFire gfPostGeoLocation;
    private String sport;
    private NavigationHelper navigationHelper;
    private FirebaseAuth mAuth;

    public NewsFeedFragment(){
        // Required empty public constructor
    }

    // onCreateView is called before onStart
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        newsFeedView = inflater.inflate(R.layout.fragment_news_feed, container, false);

        mAuth = FirebaseAuth.getInstance();
        SHARED_PREFS = "filterOptions-" + mAuth.getCurrentUser().getUid();
        fm = getFragmentManager();
        fm.addOnBackStackChangedListener(this);
        backstackHelper = new BackstackHelper(fm);
        btnFilterOptions = newsFeedView.findViewById(R.id.btn_filter_options);
        btnFilterOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), FilterOptionsActivity.class));
            }
        });

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        return newsFeedView;
    }

    @Override
    public void onStart() {
        super.onStart();

        navigationHelper = NavigationHelper.getInstance();
        if(navigationHelper.getGoToDiffUsersFrag()){
            Log.i(TAG, "Go to diff user's frag");
            Log.i(TAG, "onStart: navigatedHelper.getDiffUsersId(): " + navigationHelper.getDiffUsersId());

            fragmentNavigationHelper = new FragmentNavigationHelper(getActivity());
            Bundle bundle = new Bundle();
            bundle.putString("uid", navigationHelper.getDiffUsersId());
            fragmentNavigationHelper.changeFragInContainerAddToBackstack(new DifferentUsersProfileFragment(),
                    bundle,
                    R.id.fragment_container,
                    "replace fragment_container fragment with DifferentUsersProfileFragment");

            // navigationHelper's diff user's data is deleting in onStop
        } else{
            Log.i(TAG, "Don't go to diff user's frag. Stay on NewsFeedFrag");

            tvLocationNotPermittedExplanation = newsFeedView.findViewById(R.id.tv_location_not_permitted_explanation);
            tvLocationNotPermittedExplanation.setText("");
            tvLocationNotPermittedExplanation.setVisibility(View.GONE);

            if(debugDisplayPosts){
                sharedPreferences = this.getActivity().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                boolean filterByLocation = sharedPreferences.getBoolean(FILTER_BY_LOCATION, false);
                boolean userAppliedFiltering = sharedPreferences.getBoolean(FILTER_APPLIED, false);
                final boolean filterBySport = sharedPreferences.getBoolean(FILTER_BY_SPORT, false);
                sport = sharedPreferences.getString(SPORT, "");
                Log.i(TAG, "From shared prefs: filterBySport: " + filterBySport + ", sport: " + sport);

                if(userAppliedFiltering && filterByLocation){

                    if(locationPermissionIsGranted()) {
                        tvLocationNotPermittedExplanation.setText("");
                        tvLocationNotPermittedExplanation.setVisibility(View.GONE);

                        final UsersLastLocation usersLastLocation = UsersLastLocation.getInstance();
                        fusedLocationProvider = FusedLocationProvider.getInstance();

                        LocationCallback locationCallback = new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                super.onLocationResult(locationResult);

                                usersLastLocation.setLatitude(locationResult.getLastLocation().getLatitude());
                                usersLastLocation.setLongitude(locationResult.getLastLocation().getLongitude());
                                Log.e(TAG, "lat: " + locationResult.getLastLocation().getLatitude()
                                        + " long: " + locationResult.getLastLocation().getLongitude());

                                if(!filteredPostsAlreadyDisplayed){
                                    showFilteredPosts();
                                    filteredPostsAlreadyDisplayed = true;
                                }

                                geoQuery.setCenter(new GeoLocation(usersLastLocation.getLatitude(), usersLastLocation.getLongitude()));
                            }
                        };

                        fusedLocationProvider.requestLocationUpdates(getContext(), locationCallback);
                    }
                    else{
                        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
                        Permissions.check(getContext()/*context*/, permissions, "Location permissions are required to get your location."/*rationale*/, null/*options*/, new PermissionHandler() {
                            @Override
                            public void onGranted() {

                                tvLocationNotPermittedExplanation.setText("");
                                tvLocationNotPermittedExplanation.setVisibility(View.GONE);

                                final UsersLastLocation usersLastLocation = UsersLastLocation.getInstance();
                                fusedLocationProvider = FusedLocationProvider.getInstance();

                                LocationCallback locationCallback = new LocationCallback() {
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        super.onLocationResult(locationResult);

                                        usersLastLocation.setLatitude(locationResult.getLastLocation().getLatitude());
                                        usersLastLocation.setLongitude(locationResult.getLastLocation().getLongitude());
                                        Log.e(TAG, "lat: " + locationResult.getLastLocation().getLatitude()
                                                + " long: " + locationResult.getLastLocation().getLongitude());

                                        if(!filteredPostsAlreadyDisplayed){
                                            // will filter by location and maybe by sport (if user chose to filter by sport)
                                            showFilteredPosts();
                                            filteredPostsAlreadyDisplayed = true;
                                        }

                                        geoQuery.setCenter(new GeoLocation(usersLastLocation.getLatitude(), usersLastLocation.getLongitude()));
                                    }
                                };

                                fusedLocationProvider.requestLocationUpdates(getContext(), locationCallback);
                            }

                            @Override
                            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                                if(filterBySport){
                                    tvLocationNotPermittedExplanation.setText("Displaying posts filtered by sport only. To see posts filtered by location as well grant the app location permissions.");
                                    tvLocationNotPermittedExplanation.setVisibility(View.VISIBLE);
                                    showPostsFilteredBySport();
                                } else {
                                    tvLocationNotPermittedExplanation.setText("Displaying all posts. To see posts filtered by location grant the app location permissions.");
                                    tvLocationNotPermittedExplanation.setVisibility(View.VISIBLE);
                                    showAllPosts();
                                }

                                super.onDenied(context, deniedPermissions);
                            }
                        });
                    }
                }else {
                    if(filterBySport){
                        showPostsFilteredBySport();
                    } else {
                        showAllPosts();
                    }
                }
            } else {
                // Don't display the posts
            }
        }

    }

    private boolean locationPermissionIsGranted(){
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PermissionChecker.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PermissionChecker.PERMISSION_GRANTED){
            return true;
        } else{
            return false;
        }
    }

    @Override
    public void onStop() {
        if(navigationHelper.getGoToDiffUsersFrag()){
            navigationHelper.setGoToDiffUsersFrag(false);
            navigationHelper.setDiffUsersId("");
        } else{
            tvLocationNotPermittedExplanation.setText("");
            tvLocationNotPermittedExplanation.setVisibility(View.GONE);
            fusedLocationProvider.removeLocationUpdates();
            filteredPostsAlreadyDisplayed = false;
            if(geoQuery != null){
                geoQuery.removeAllListeners();
            }
        }
        super.onStop();
    }

    private void showAllPosts(){
        Query query = FirebaseDatabase.getInstance().getReference().child("posts");
        postRecyclerView = new PostRecyclerView(query,
                getActivity(),
                getContext(),
                newsFeedView,
                TAG);
        postRecyclerView.displayRV();
    }

    private void showPostsFilteredBySport(){
        Query query = FirebaseDatabase.getInstance().getReference().child("posts")
                .orderByChild("sport").equalTo(sharedPreferences.getString(SPORT, ""));
        postRecyclerView = new PostRecyclerView(query,
                getActivity(),
                getContext(),
                newsFeedView,
                TAG);
        postRecyclerView.displayRV();
    }

    private double convertToKm(int iLength, String length_unit){
        double dLength = Double.valueOf(iLength);
        if (length_unit.equals("kilometers")){
            return dLength;
        } else {
            return dLength*1.6;
        }
    }

    private void setLocationGeoQuery(double latitude, double longitude, double dRadiusInKm){
        geoQuery = gfPostGeoLocation.queryAtLocation(new GeoLocation(
                latitude, longitude), dRadiusInKm);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(final String key, GeoLocation location) {
                Log.i(TAG, "onKeyEntered: key: " + key + ", location: " + location.latitude + ", " + location.longitude);

                // First check if the post is of the right sport if the user wants this
                // filtering

                usersFilteredPostsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    // I think this is called once when the key is added to the table and then
                    // called again when timeFromFixedDate is added.
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(!dataSnapshot.hasChild(key)){
                            addPostIdAndTimeToUsersFilteredPosts(key);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onKeyExited(String key) {
                // if post exits the desired location, remove the post from user's filtered posts
                usersFilteredPostsRef.child(key).removeValue();
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void setLocationAndSportGeoQuery(double latitude, double longitude, double dRadiusInKm){
        geoQuery = gfPostGeoLocation.queryAtLocation(new GeoLocation(
                latitude, longitude), dRadiusInKm);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(final String key, GeoLocation location) {
                Log.i(TAG, "onKeyEntered: key: " + key + ", location: " + location.latitude + ", " + location.longitude);

                // First check if the post is of the right sport if the user wants this
                // filtering

                usersFilteredPostsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    // I think this is called once when the key is added to the table and then
                    // called again when timeFromFixedDate is added.
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(!dataSnapshot.hasChild(key)){
                            postsRef.child(key).child("sport").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()){
                                        if(dataSnapshot.getValue().toString().equals(sport)){
                                            addPostIdAndTimeToUsersFilteredPosts(key);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onKeyExited(String key) {
                // if post exits the desired location, remove the post from user's filtered posts
                usersFilteredPostsRef.child(key).removeValue();
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    // will filter by location and maybe by sport (if user chose to filter by sport)
    private void showFilteredPosts(){
        database = FirebaseDatabase.getInstance().getReference();
        Log.i(TAG, "userId: " + userId);
        usersFilteredPostsRef = database.child("filteredPosts").child(userId);
        usersFilteredPostsRef.removeValue();
        postsRef = database.child("posts");

        DatabaseReference dbPostGeoLocation = FirebaseDatabase.getInstance().getReference("geoLocations").child("posts");
        gfPostGeoLocation = new GeoFire(dbPostGeoLocation);

        usersLastLocation = UsersLastLocation.getInstance();

        double latitude = usersLastLocation.getLatitude();
        double longitude = usersLastLocation.getLongitude();

        if(latitude != 10000 && longitude != 10000){

            int iRadius = sharedPreferences.getInt(RADIUS, 0);
            if(iRadius == 0){
                Log.e(TAG, "Shared preferences did not retrieve a radius or stored it with a value of 0");
            }
            String lengthUnit = sharedPreferences.getString(LENGTH_UNIT, "kilometers");
            Log.i(TAG, "User saved radius in " + lengthUnit);
            double dRadiusInKm = convertToKm(iRadius, lengthUnit);
            Log.i(TAG, "User's chosen radius preference: " + dRadiusInKm + " km. User's current lat: " + latitude + ", long: " + longitude);
                    //.getDouble(RADIUS, false);

            if(sharedPreferences.getBoolean(FILTER_BY_SPORT, false) == false){
                setLocationGeoQuery(latitude, longitude, dRadiusInKm);
            } else{
                setLocationAndSportGeoQuery(latitude, longitude, dRadiusInKm);
            }

            Query query = FirebaseDatabase.getInstance().getReference().child("filteredPosts").child(userId).orderByChild("timeFromFixedDate");
            filteredPostsRecyclerView = new FilteredPostsRecyclerView(query,
                    getActivity(),
                    getContext(),
                    newsFeedView,
                    TAG);
            filteredPostsRecyclerView.displayRV();
        } else{
            Log.e(TAG, "App has not retrieved user's location. Can't display filtered posts");
        }

    }

    private void addPostIdAndTimeToUsersFilteredPosts(final String postId){

        // Store the postId  -> timeFromFixedDate in filteredPosts -> user_id
        postsRef.child(postId).child("timeFromFixedDate").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    usersFilteredPostsRef.child(postId).child("timeFromFixedDate").setValue(dataSnapshot.getValue().toString());
                    Log.i(TAG, "Added post with postId: " + postId + "to users filtered posts table");
                }
                else{
                    // This part is probably called when first only the key of the post is stored,
                    // and then again when timeFromFixedDate is stored.
                    //Log.i(TAG, "Failed to add post with postId: " + postId + "to users filtered posts table. timeFromFixedDate does not exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onBackStackChanged() {
        backstackHelper.logCurrentBackstack(TAG);
    }
}
