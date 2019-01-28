package com.github.meonix.chatapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.meonix.chatapp.adapter.messagePrivateChatAdapter;
import com.github.meonix.chatapp.model.MessagesChatModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private String messageReceiverID, messageReceiverName, messageuserImage, messageSenderID, currentDate, currentTime;
    private TextView userName, userLastSeen;
    private CircleImageView userImage;
    private DatabaseReference RootRef;
    private Toolbar ChatToolbar;
    private FirebaseAuth mAuth;
    private ImageButton sendMessagebutton;
    private EditText messageInputText;
    private final List<MessagesChatModel> messagelist = new ArrayList<>();
    private messagePrivateChatAdapter messageAdapter;
    private RecyclerView private_message_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        messageuserImage = getIntent().getExtras().get("userImage").toString();
        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();

        RootRef = FirebaseDatabase.getInstance().getReference();
        InitializeFields();


        private_message_image.setAdapter(messageAdapter);

        userName.setText(messageReceiverName);
        Picasso.get().load(messageuserImage).placeholder(R.drawable.profile).into(userImage);

        sendMessagebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
            }
        });


        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        private_message_image.setLayoutManager(linearLayoutManager);
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


        userImage = findViewById(R.id.custom_profile_image);
        private_message_image = findViewById(R.id.private_message);
        userName = findViewById(R.id.custom_profile_name);
        userLastSeen = findViewById(R.id.user_last_seen);
        sendMessagebutton = findViewById(R.id.send_message_btn);
        messageInputText = findViewById(R.id.input_message);
        messageAdapter = new messagePrivateChatAdapter(messagelist);

    }

    @Override
    protected void onStart() {
        super.onStart();
        messagelist.clear();
        RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                MessagesChatModel message = dataSnapshot.getValue(MessagesChatModel.class);
                messagelist.add(message);
                messageAdapter.notifyDataSetChanged();
                private_message_image.post(new Runnable() {
                    @Override
                    public void run() {
                        private_message_image.smoothScrollToPosition(private_message_image.getAdapter().getItemCount());
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
}
