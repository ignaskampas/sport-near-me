package com3001.cw.ik00157.sportnearme.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import com3001.cw.ik00157.sportnearme.R;
import com3001.cw.ik00157.sportnearme.models.User;
import com3001.cw.ik00157.sportnearme.utilities.RSAKeyGenerator;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SIGN_IN_ACTIVITY";
    private CallbackManager mCallbackManager;
    private FirebaseAuth mAuth;
    private LoginButton loginButton;
    private DatabaseReference database;
    private FirebaseStorage mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mCallbackManager = CallbackManager.Factory.create();
        loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions("email", "public_profile");
        database = FirebaseDatabase.getInstance().getReference();

        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // ...
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            loginButton.setVisibility(View.GONE);

                            database.child("users").addListenerForSingleValueEvent(new ValueEventListener(){
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())){
                                        // User has logged in before.
                                        Log.i(TAG, "User is already registered");
                                        mainIntent();
                                    } else{
                                        // User has not logged in before.
                                        Log.i(TAG, "Registering a new user");

                                        // Store profile pic in firebase storage
                                        String highResolutionPhotoUrl = "https://graph.facebook.com/" + getFacebookUserId() + "/picture?type=large";
                                        new WriteNewUser().execute(new TaskParams(highResolutionPhotoUrl));
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.w(TAG, "Unable to read data snapshot");
                                }
                            });

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private static String getFacebookUserId(){
        String facebookUserId = "";
        for (UserInfo data : FirebaseAuth.getInstance().getCurrentUser().getProviderData()){
            if (data.getProviderId().equals("facebook.com")){
                facebookUserId = data.getUid();
            }
        }
        return facebookUserId;
    }

    class WriteNewUser extends AsyncTask<TaskParams, Void, Bitmap> {

        TaskParams taskParams;
        @Override
        protected Bitmap doInBackground(TaskParams... taskParams) {
            this.taskParams = taskParams[0];
            URL imageURL = null;
            try {
                imageURL = new URL(taskParams[0].getUrl());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.taskParams.incrementNrAttemts();
            return bitmap;
        }

        // invoked on the UI thread rather than the background thread
        protected void onPostExecute(Bitmap b){

            FirebaseUser firebaseUser = mAuth.getCurrentUser();
            final User user = new User(firebaseUser.getUid(),
                    firebaseUser.getDisplayName(),
                    mAuth.getCurrentUser().getEmail(),
                    mAuth.getCurrentUser().getPhoneNumber());

            if(b != null){
                storeProfilePicAndWriteNewUser(b).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        if(task.isSuccessful()){
                            Uri photoUri = task.getResult();
                            String photoUrl = photoUri.toString();

                            user.setPhotoUrl(photoUrl);
                        } else {
                            // Handle failures
                            Log.w(TAG, "Failed to retrieve photo URL. Storing user in the database without a photo URL");
                        }

                        writeNewUser(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.i(TAG, "Wrote new user to firebase database");
                                try {
                                    generateAndSaveRSAKeys();
                                    mainIntent();
                                } catch (InvalidKeySpecException e) {
                                    e.printStackTrace();
                                    Toast.makeText(SignInActivity.this, "Failed to register", Toast.LENGTH_SHORT).show();
                                } catch (NoSuchAlgorithmException e) {
                                    e.printStackTrace();
                                    Toast.makeText(SignInActivity.this, "Failed to register", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Failed to store the user in firebase");
                                e.printStackTrace();
                            }
                        });
                    }
                });;
            } else if(this.taskParams.getNrAttemts() < 2){
                String lowResolutionPhotoUrl = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString();
                this.taskParams.setUrl(lowResolutionPhotoUrl);
                new WriteNewUser().execute(this.taskParams);
            }

        }

    }

    private Task<Uri> storeProfilePicAndWriteNewUser(Bitmap bitmap){
        final StorageReference picStorageRef = profilePicStorageRef();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = picStorageRef.putBytes(data);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if(!task.isSuccessful()){
                    throw task.getException();
                }
                return picStorageRef.getDownloadUrl();
            }
        });
        return urlTask;

    }

    private static class TaskParams{
        private int nrAttemts = 0;
        private String url;

        TaskParams(String url){
            this.url = url;
        }

        public int getNrAttemts() {
            return nrAttemts;
        }

        public void incrementNrAttemts(){
            this.nrAttemts += 1;
        }

        public String getUrl(){
            return this.url;
        }

        public void setUrl(String url){
            this.url = url;
        }
    }

    private void mainIntent(){
        Intent mainIntent = new Intent(SignInActivity.this, HomeActivity.class);
        startActivity(mainIntent);
        finish();
    }


    private StorageReference profilePicStorageRef(){
        return mStorage.getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("profilePic");
    }

    private void writeNewUserPhotoUrl(String photoUrl){
        database.child("users").child(mAuth.getCurrentUser().getUid()).child("photoUrl").setValue(photoUrl);
    }

    private Task<Void> writeNewUser(User user){
        return database.child("users").child(mAuth.getCurrentUser().getUid()).setValue(user);
    }

    private void generateAndSaveRSAKeys() throws InvalidKeySpecException, NoSuchAlgorithmException {
        RSAKeyGenerator rsaKeyGenerator = new RSAKeyGenerator();
        String publicKeyModulus = String.valueOf(rsaKeyGenerator.getPublicKeyModulus());
        final String publicKeyExponent = String.valueOf(rsaKeyGenerator.getPublicKeyExponent());
        String privateKeyModulus = String.valueOf(rsaKeyGenerator.getPrivateKeyModulus());
        String privateKeyExponent = String.valueOf(rsaKeyGenerator.getPrivateKeyExponent());

        database.child("rsaKeys").child(mAuth.getCurrentUser().getUid()).child("publicKeyModulus").setValue(publicKeyModulus).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d(TAG, "Added user's rsa public key's modulus to firebase database");
                } else {
                    Log.e(TAG, "Failed to add user's rsa public key's modulus to firebase database");
                }
            }
        });
        database.child("rsaKeys").child(mAuth.getCurrentUser().getUid()).child("publicKeyExponent").setValue(publicKeyExponent).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d(TAG, "Added user's rsa public key's exponent to firebase database");
                } else {
                    Log.e(TAG, "Failed to add user's rsa public key's exponent to firebase database");
                }
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("rsaKeys-" + mAuth.getCurrentUser().getUid(), MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("privateKeyModulus", privateKeyModulus);
        editor.putString("privateKeyExponent", privateKeyExponent);
        editor.apply();
    }

}
