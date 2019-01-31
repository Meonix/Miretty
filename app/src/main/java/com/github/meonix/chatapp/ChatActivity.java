package com.github.meonix.chatapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.meonix.chatapp.adapter.messagePrivateChatAdapter;
import com.github.meonix.chatapp.model.MessagesChatModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private String messageReceiverID, messageReceiverName, messageuserImage, messageSenderID, currentDate, currentTime,currentUser;
    private static String mFileName=null;
    private TextView userName, userLastSeen;
    private CircleImageView userImage;
    private DatabaseReference RootRef,AudioRef,MessageAudioRef;
    private Toolbar ChatToolbar;
    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    private ImageButton sendMessagebutton,audioMessageButton;
    private EditText messageInputText;
    private final List<MessagesChatModel> messagelist = new ArrayList<>();
    private messagePrivateChatAdapter messageAdapter;
    private RecyclerView private_message_chat;
    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private MediaRecorder mRecorder = null;
    private boolean permissionToRecordAccepted = false;
    private ProgressDialog mProgress;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    private File mOutputFile=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        messageuserImage = getIntent().getExtras().get("userImage").toString();

        mAuth = FirebaseAuth.getInstance();
        mStorage=FirebaseStorage.getInstance().getReference().child("Audio Messages");
        messageSenderID = mAuth.getCurrentUser().getUid();
        currentUser=messageSenderID;
        mProgress=new ProgressDialog(this);

        RootRef = FirebaseDatabase.getInstance().getReference();
        InitializeFields();
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);


        private_message_chat.setAdapter(messageAdapter);

        userName.setText(messageReceiverName);
        Picasso.get().load(messageuserImage).placeholder(R.drawable.profile).into(userImage);

        sendMessagebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
                private_message_chat.post(new Runnable() {
                    @Override
                    public void run() {
                        private_message_chat.smoothScrollToPosition(private_message_chat.getAdapter().getItemCount());
                        private_message_chat.scrollToPosition(private_message_chat.getAdapter().getItemCount()-1);
                    }
                });
            }
        });
        messageInputText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                private_message_chat.post(new Runnable() {
                    @Override
                    public void run() {
                        private_message_chat.smoothScrollToPosition(private_message_chat.getAdapter().getItemCount());
                        private_message_chat.scrollToPosition(private_message_chat.getAdapter().getItemCount()-1);
                    }
                });
            }
        });

        audioMessageButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){

                    startRecording();

                }
                else if (event.getAction() == MotionEvent.ACTION_UP){

                    stopRecording();

                }
                return false;
            }
        });


        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        private_message_chat.setLayoutManager(linearLayoutManager);

        messagelist.clear();
        RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                MessagesChatModel message = dataSnapshot.getValue(MessagesChatModel.class);
                messagelist.add(message);
                messageAdapter.notifyDataSetChanged();
                private_message_chat.post(new Runnable() {
                    @Override
                    public void run() {
                        private_message_chat.smoothScrollToPosition(private_message_chat.getAdapter().getItemCount());
                        private_message_chat.scrollToPosition(private_message_chat.getAdapter().getItemCount()-1);
                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }
    private void startRecording() {
        AudioRef = RootRef.child("Messages")
                .child(messageSenderID).child(messageReceiverID).push();
        String messagePushID = AudioRef.getKey();
        mFileName = getExternalCacheDir().getAbsolutePath()+"/"+messagePushID+".mp3";
        mRecorder = new MediaRecorder();

        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        uploadAudio();
    }


    private void uploadAudio() {
        mProgress.setMessage("Sending Your Audio....");
        mProgress.show();
        AudioRef = RootRef.child("Messages")
                .child(messageSenderID).child(messageReceiverID).push();
        final String messagePushID = AudioRef.getKey();
        StorageReference filepath = mStorage.child(currentUser).child(messagePushID+".mp3");
        final Uri uri = Uri.fromFile(new File(mFileName));

        filepath.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                //delete audio file is saved in storage
                File file = new File(uri.getPath());
                file.delete();
                if(file.exists()){
                    try {
                        file.getCanonicalFile().delete();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(file.exists()){
                        getApplicationContext().deleteFile(file.getName());
                    }
                }

                
                final String AudiodownloadUrl= task.getResult().getDownloadUrl().toString();
                MessageAudioRef= RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).child(messagePushID);

                String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;
                Calendar calForDate = Calendar.getInstance();
                SimpleDateFormat currentDateFormat = new SimpleDateFormat("MM dd,yyy");
                currentDate = currentDateFormat.format(calForDate.getTime());

                Calendar calForTime = Calendar.getInstance();
                SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm ss a");
                currentTime = currentTimeFormat.format(calForTime.getTime());
                Map messageTextBody = new HashMap();
                messageTextBody.put("from_uid", messageSenderID);
                messageTextBody.put("message", AudiodownloadUrl);
                messageTextBody.put("date", currentDate);
                messageTextBody.put("time", currentTime);
                messageTextBody.put("type", "audio");
                Map messageBodyDetails = new HashMap();
                messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);
                RootRef.updateChildren(messageBodyDetails);
                Toast.makeText(getApplicationContext(),"done",Toast.LENGTH_SHORT).show();
                mProgress.dismiss();
            }
        });
    }
    @Override
    public void onStop() {
        super.onStop();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
    }

    private void SendMessage() {
        String messageTExt = messageInputText.getText().toString();
        if (TextUtils.isEmpty(messageTExt)) {
            Toast.makeText(this, "write your message ....", Toast.LENGTH_SHORT).show();
        } else {
            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                    .child(messageSenderID).child(messageReceiverID).push();
            String messagePushID = userMessageKeyRef.getKey();
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MM dd,yyy");
            currentDate = currentDateFormat.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm ss a");
            currentTime = currentTimeFormat.format(calForTime.getTime());
            Map messageTextBody = new HashMap();
            messageTextBody.put("from_uid", messageSenderID);
            messageTextBody.put("message", messageTExt);
            messageTextBody.put("date", currentDate);
            messageTextBody.put("time", currentTime);
            messageTextBody.put("type", "text");
            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);
            RootRef.updateChildren(messageBodyDetails);
            messageInputText.setText("");
        }
    }

    private void InitializeFields() {
        ChatToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolbar);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionbar.setCustomView(actionBarView);

        audioMessageButton = findViewById(R.id.audio_message_btn);
        userImage = findViewById(R.id.custom_profile_image);
        private_message_chat = findViewById(R.id.private_message);
        userName = findViewById(R.id.custom_profile_name);
        userLastSeen = findViewById(R.id.user_last_seen);
        sendMessagebutton = findViewById(R.id.send_message_btn);
        messageInputText = findViewById(R.id.input_message);
        messageAdapter = new messagePrivateChatAdapter(messagelist);

    }

    @Override
    protected void onStart() {
        super.onStart();
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        private_message_chat.setLayoutManager(linearLayoutManager);
        private_message_chat.post(new Runnable() {
            @Override
            public void run() {
                private_message_chat.smoothScrollToPosition(private_message_chat.getAdapter().getItemCount());
                private_message_chat.scrollToPosition(private_message_chat.getAdapter().getItemCount()-1);
            }
        });
    }
}