package com3001.cw.ik00157.sportnearme;

import android.Manifest;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.util.ArrayList;

import com3001.cw.ik00157.sportnearme.models.UsersLastLocation;

import static android.os.Looper.getMainLooper;

public class FusedLocationProvider {

    private final static String TAG = "FUSED_LOCATION_PROVIDER";

    private static FusedLocationProvider fusedLocationProvider = null;
    private static FusedLocationProviderClient fusedLocationProviderClient = null;
    private static LocationRequest locationRequest;
    private UsersLastLocation usersLastLocation;
    private static Context ctx;
    private static LocationCallback locationCallback;

    private FusedLocationProvider(){
        usersLastLocation = UsersLastLocation.getInstance();
    }

    public static FusedLocationProvider getInstance(){
        if(fusedLocationProvider == null){
            fusedLocationProvider = new FusedLocationProvider();
        }
        else{
            removeLocationUpdates();
        }
        return fusedLocationProvider;
    }

    private void requestLocationUpdates(){

        if(ContextCompat.checkSelfPermission(this.ctx,
                Manifest.permission.ACCESS_FINE_LOCATION) == PermissionChecker.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PermissionChecker.PERMISSION_GRANTED)
        {
            // Just in case the method to requestLocationUpdates for this instance of
            // FusedLocationProvider has already been called, removeLocationUpdates(), so that
            // location updates would not be called in multiple places
            removeLocationUpdates();
            this.fusedLocationProviderClient = new FusedLocationProviderClient(this.ctx);
            locationRequest = new LocationRequest();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setFastestInterval(2000);
            locationRequest.setInterval(4000);

            locationCallback = new LocationCallback(){
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);

                    usersLastLocation.setLatitude(locationResult.getLastLocation().getLatitude());
                    usersLastLocation.setLongitude(locationResult.getLastLocation().getLongitude());
                    Log.e(TAG, "lat: " + locationResult.getLastLocation().getLatitude()
                            + " long: " + locationResult.getLastLocation().getLongitude());
                }
            };
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
        } else{
            callPermissions(this.ctx);
        }

    }

    public void requestLocationUpdates(Context ctx, LocationCallback locationCallback){

        if(ContextCompat.checkSelfPermission(ctx,
                Manifest.permission.ACCESS_FINE_LOCATION) == PermissionChecker.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PermissionChecker.PERMISSION_GRANTED)
        {
            this.locationCallback = locationCallback;
            // Just in case the method to requestLocationUpdates for this instance of
            // FusedLocationProvider has already been called, removeLocationUpdates(), so that
            // location updates would not be called in multiple places
            removeLocationUpdates();
            this.fusedLocationProviderClient = new FusedLocationProviderClient(ctx);
            locationRequest = new LocationRequest();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setFastestInterval(2000);
            locationRequest.setInterval(4000);

            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
        } else{
            //callPermissions(ctx);
        }

    }

    public void requestLocationUpdates(LocationCallback locationCallback){

        if(ContextCompat.checkSelfPermission(this.ctx,
                Manifest.permission.ACCESS_FINE_LOCATION) == PermissionChecker.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PermissionChecker.PERMISSION_GRANTED)
        {
            this.locationCallback = locationCallback;
            // Just in case the method to requestLocationUpdates for this instance of
            // FusedLocationProvider has already been called, removeLocationUpdates(), so that
            // location updates would not be called in multiple places
            removeLocationUpdates();
            this.fusedLocationProviderClient = new FusedLocationProviderClient(this.ctx);
            locationRequest = new LocationRequest();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setFastestInterval(2000);
            locationRequest.setInterval(4000);

            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
        } else{
            callPermissions(this.ctx);
        }

    }

    public void callPermissions(Context ctx){
        this.ctx =ctx;
        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        Permissions.check(ctx/*context*/, permissions, "Location permissions are required to get your location."/*rationale*/, null/*options*/, new PermissionHandler() {
            @Override
            public void onGranted() {
                requestLocationUpdates();
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                super.onDenied(context, deniedPermissions);
            }
        });
    }

    public static void removeLocationUpdates(){
        if(fusedLocationProviderClient != null){
            fusedLocationProviderClient.removeLocationUpdates(locationCallback); // nepadaro kad fusedLocationProviderClient butu null
            Log.i(TAG, "After removing location updates fusedLocationProviderClient == null: " + String.valueOf(fusedLocationProviderClient == null)); // this becomes false
            Log.i(TAG, "After removing location updates locationCallback == null: " + String.valueOf(locationCallback == null)); // this becomes false
        }
    }
}
