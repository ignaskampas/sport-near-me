package com3001.cw.ik00157.sportnearme.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com3001.cw.ik00157.sportnearme.R;
import com3001.cw.ik00157.sportnearme.models.Message;
import com3001.cw.ik00157.sportnearme.models.User;
import com3001.cw.ik00157.sportnearme.utilities.MessageAdapter;
import com3001.cw.ik00157.sportnearme.utilities.RSADecryptionCipher;
import com3001.cw.ik00157.sportnearme.utilities.RSAEncryptionCipher;
import com3001.cw.ik00157.sportnearme.utilities.TimeHelper;
import de.hdodenhof.circleimageview.CircleImageView;

public class ConvoActivity extends AppCompatActivity {

    private final static String TAG = "CONVO_ACTIVITY";
    private TextView tvChatBarDisplayName;
    private CircleImageView civChatBarUserPhoto;

    private EditText etInputMessage;
    private Button btnSendMessage;

    private DatabaseReference database;
    private DatabaseReference diffUsersRef;

    private FirebaseAuth mAuth;
    private FirebaseUser fireUser;
    private User diffUser, user;

    private String messageSenderId, messageReceiverId;
    private final List<Message> messageList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView rvMessageList;
    private RSADecryptionCipher rsaDecryptionCipher;
    private RSAEncryptionCipher rsaEncryptionCipherCurrentUser;
    private RSAEncryptionCipher rsaEncryptionCipherDiffUser;
    private TimeHelper timeHelper;
    private String privateKeyModulus;
    private String privateKeyExponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_convo);

        getSupportActionBar().hide();
        mAuth = FirebaseAuth.getInstance();
        fireUser = mAuth.getCurrentUser();

        SharedPreferences sharedPreferencesRSAKeys = getSharedPreferences("rsaKeys-" + mAuth.getCurrentUser().getUid(), MODE_PRIVATE);
        privateKeyModulus = sharedPreferencesRSAKeys.getString("privateKeyModulus", "");
        privateKeyExponent = sharedPreferencesRSAKeys.getString("privateKeyExponent", "");

        try {
            rsaDecryptionCipher = new RSADecryptionCipher(privateKeyModulus, privateKeyExponent);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        Intent intent = getIntent();
        String diffUsersId = intent.getStringExtra("diffUsersId");

        diffUser = new User();
        diffUser.setUid(diffUsersId);
        user = new User();
        user.setUid(fireUser.getUid());
        messageSenderId = user.getUid();
        messageReceiverId = diffUser.getUid();

        database = FirebaseDatabase.getInstance().getReference();
        diffUsersRef = database.child("users").child(messageReceiverId);

        database.child("rsaKeys").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    rsaEncryptionCipherCurrentUser = new RSAEncryptionCipher(dataSnapshot.child("publicKeyModulus").getValue().toString(), dataSnapshot.child("publicKeyExponent").getValue().toString());
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        database.child("rsaKeys").child(diffUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    rsaEncryptionCipherDiffUser = new RSAEncryptionCipher(dataSnapshot.child("publicKeyModulus").getValue().toString(), dataSnapshot.child("publicKeyExponent").getValue().toString());
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        inflateStaticViews();

        setTvSendMessageOnClickLis();

        messageAdapter = new MessageAdapter(messageList);
        rvMessageList = findViewById(R.id.rv_messages_list);
        linearLayoutManager = new LinearLayoutManager(this);
        rvMessageList.setLayoutManager(linearLayoutManager);
        rvMessageList.setAdapter(messageAdapter);
        timeHelper = new TimeHelper(TAG);
    }

    @Override
    protected void onStart() {
        super.onStart();

        database.child("conversations").child(messageSenderId).child(messageReceiverId).child("messages")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Message message = dataSnapshot.getValue(Message.class);
                        try {
                            String messageTextDecrypted = rsaDecryptionCipher.decrypt(message.getMessage());
                            message.setMessage(messageTextDecrypted);
                        } catch (BadPaddingException e) {
                            e.printStackTrace();
                        } catch (IllegalBlockSizeException e) {
                            e.printStackTrace();
                        }
                        messageList.add(message);

                        messageAdapter.notifyDataSetChanged();

                        // rvMessageList.getAdapter().getItemCount() gets the number of messages that there
                        // are and so this whole next line cause rv to scroll to the last message.
                        rvMessageList.smoothScrollToPosition(rvMessageList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void setTvSendMessageOnClickLis(){
        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void sendMessage(){
        String message = etInputMessage.getText().toString().trim();
        if(message.isEmpty()){
            Toast.makeText(this, "Please write a message to send", Toast.LENGTH_SHORT).show();
        } else if(message.length() > 242){
            Toast.makeText(this, "The message size cannot exceed 242 characters. Current message length is: " + message.length() + " characters", Toast.LENGTH_SHORT).show();
        } else if(!(rsaEncryptionCipherCurrentUser.cipherIsSetup() && rsaEncryptionCipherDiffUser.cipherIsSetup())) {
                Toast.makeText(ConvoActivity.this, "An error has occured. Try to resend the message soon", Toast.LENGTH_SHORT).show();
        } else {
            String messageTimeCreated = timeHelper.getTimestamp();
            String messageTimeFromFixedDate = timeHelper.getTimeFromFixedDate(messageTimeCreated);

            database.child("conversations").child(messageSenderId).child(messageReceiverId).child("lastMessageTimeCreated").setValue(messageTimeCreated);
            database.child("conversations").child(messageSenderId).child(messageReceiverId).child("lastMessageTimeFromFixedDate").setValue(messageTimeFromFixedDate);
            database.child("conversations").child(messageReceiverId).child(messageSenderId).child("lastMessageTimeCreated").setValue(messageTimeCreated);
            database.child("conversations").child(messageReceiverId).child(messageSenderId).child("lastMessageTimeFromFixedDate").setValue(messageTimeFromFixedDate);


            // ref where the messages for current user talking to diff user are stored
            String messageSenderRef = "conversations/" + messageSenderId + "/" + messageReceiverId + "/messages"  ;
            // ref where the messages for diff user talking to current user are stored
            String messageReceiverRef = "conversations/" + messageReceiverId + "/" + messageSenderId + "/messages";
            // ref of the message that is about to be sent
            DatabaseReference messageKeyRef = database.child("messages").child(messageSenderId).child(messageReceiverId).push();
            String messageId = messageKeyRef.getKey();

            Map messageBodySender = new HashMap();
            Map messageBodyReceiver = new HashMap();
            messageBodySender.put("messageTimeCreated", messageTimeCreated);
            messageBodyReceiver.put("messageTimeCreated", messageTimeCreated);
            messageBodySender.put("messageTimeFromFixedDate", messageTimeFromFixedDate);
            messageBodyReceiver.put("messageTimeFromFixedDate", messageTimeFromFixedDate);
            messageBodySender.put("message", rsaEncryptionCipherCurrentUser.encrypt(message));
            messageBodyReceiver.put("message", rsaEncryptionCipherDiffUser.encrypt(message));
            messageBodySender.put("from", messageSenderId);
            messageBodyReceiver.put("from", messageSenderId);

            Map messageBodies = new HashMap();
            messageBodies.put(messageSenderRef + "/" + messageId, messageBodySender);
            messageBodies.put(messageReceiverRef + "/" + messageId, messageBodyReceiver);

            database.updateChildren(messageBodies).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                if(task.isSuccessful()){
                    etInputMessage.setText("");
                } else{
                    Toast.makeText(ConvoActivity.this, "The message did not send", Toast.LENGTH_SHORT).show();
                }
                }
            });
        }
    }

    private void inflateStaticViews(){
        tvChatBarDisplayName = findViewById(R.id.chat_bar_display_name);
        civChatBarUserPhoto = findViewById(R.id.chat_bar_user_photo);
        btnSendMessage = findViewById(R.id.btn_send_message);
        etInputMessage = findViewById(R.id.et_input_message);

        setChatBarUserPhoto();
        setChatBarDisplayName();
    }

    private void setChatBarUserPhoto(){
        diffUsersRef.child("photoUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    diffUser.setPhotoUrl(dataSnapshot.getValue().toString());
                    try{
                        Picasso.get().load(diffUser.getPhotoUrl()).placeholder(R.drawable.profile_pic_placeholder).into(civChatBarUserPhoto);
                    }catch(Exception e){
                        e.printStackTrace();
                        civChatBarUserPhoto.setImageResource(R.drawable.profile_pic_placeholder);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "setChatBarUserPhoto: onCancelled was called. databaseError: " + databaseError);
            }
        });
    }
    private void setChatBarDisplayName(){
        diffUsersRef.child("displayName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    diffUser.setDisplayName(dataSnapshot.getValue().toString());
                    Log.i(TAG, "Different user's display name retrieved: " + diffUser.getDisplayName());
                    tvChatBarDisplayName.setText(diffUser.getDisplayName());
                } else {
                    Log.e(TAG, "Different user's display name is not retrieving");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "setChatBarDisplayName: onCancelled was called. databaseError: " + databaseError);
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, ChatsAndContactsActivity.class));
        finish();
        super.onBackPressed();
    }
}
