package com.github.meonix.chatapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    //Create extreme type variable and
    private String receiverUserID,senderUserID,Current_State;
    private CircleImageView userProfileImage;
    private TextView userProfileName,userProfileStatus;
    private Button SendMessageRequestButton,DeclineMessageRequestButton;
    private Toolbar mToolbar;
    private ImageView backGround_visit_image;

    private FirebaseAuth mAuth;
    private DatabaseReference UserRef,ChatRequestRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();

        senderUserID=mAuth.getCurrentUser().getUid();  //take user ID of user in the database


        backGround_visit_image=(ImageView) findViewById(R.id.background_visit_image);
        userProfileImage = (CircleImageView) findViewById(R.id.visit_profile_image);
        userProfileName =(TextView) findViewById(R.id.visit_user_name);
        userProfileStatus=(TextView) findViewById(R.id.visit_profile_status);
        SendMessageRequestButton=(Button) findViewById(R.id.send_message_request_button);
        mToolbar=(Toolbar) findViewById(R.id.visit_friends_toolbar);

        Current_State = "new";

        ReTriveUserInfo();
        //This is combined with android:parentActivityName=".FindFriendsActivity" in the AndroidManifest.xml
        // we  will have position to return to FindFriendsActivity in the toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        

    }

    private void ReTriveUserInfo() {
        UserRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //check if userID exists and image also exists
                // we load data form database and change data in app
                if((dataSnapshot.exists()) && (dataSnapshot.hasChild("image")) &&(dataSnapshot.hasChild("BackGround_Image")))
                {
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();
                    getSupportActionBar().setTitle(userName);                //set Title is the name of user
                    String Background = dataSnapshot.child("BackGround_Image").getValue().toString();

//                    Picasso.get().load(Background).placeholder(R.drawable.profile).fit().into(backGround_visit_image);
                    Picasso.get().load(Background).placeholder(R.drawable.profile).into(backGround_visit_image);
                    Picasso.get().load(userImage).placeholder(R.drawable.profile).into(userProfileImage);

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequest();

                }
                else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("image")) && !(dataSnapshot.hasChild("BackGround_Image")))
                {
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();
                    getSupportActionBar().setTitle(userName);                //set Title is the name of user
                    Picasso.get().load(userImage).placeholder(R.drawable.profile).into(userProfileImage);

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequest();
                }
                //When user don't have profile picture but user ID exists
                // We do not change them's profile picture and we set them's name and status
                else
                {
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();
                    getSupportActionBar().setTitle(userName);//set Title is the name of user
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequest();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void ManageChatRequest() {
        //The code below is used to when current user click user received the request button will be change Cancel Chat Request
        ChatRequestRef.child(senderUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(receiverUserID)) //check if uri of user receiver,which is child of Chat Request and has been exists
                        {
                             String request_type = dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();
                             if(request_type.equals("sent"))
                             {
                                 Current_State = "request_send";
                                 SendMessageRequestButton.setText("Cancel Chat Request");
                             }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        //check if userID of current user not equal to userID (which is current user is looking for)
        if(!senderUserID.equals(receiverUserID))
        {
            SendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SendMessageRequestButton.setEnabled(false);
                    if(Current_State.equals("new"))
                    {
                        SendChatRequest();
                    }
                    if(Current_State.equals("request_send"))
                    {
                        CancelChatRequest();
                    }
                }
            });

        }
        //if current userID find friend and They choose their profile we have to Visible button send message
        else
        {
            SendMessageRequestButton.setVisibility(View.VISIBLE);
        }

    }

    private void CancelChatRequest() {
        //Remove the Chat Request tree in the database when user click cancel request
        ChatRequestRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            ChatRequestRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                SendMessageRequestButton.setEnabled(true);
                                                Current_State="new";
                                                SendMessageRequestButton.setText("Send Message");
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    private void SendChatRequest() {
        //senderUserID is uid of current user (senderUserID=mAuth.getCurrentUser().getUid();)
        // And write this uid to Chat Requests Tree (the tree data at database)
        //receiverUserID is uid of current user looking for (receiverUserID = getIntent().getExtras().get("visit_user_id").toString();)
        //And then write uid of current user looking for to Chat Request Tree (that's child of current user's uid
        ChatRequestRef.child(senderUserID).child(receiverUserID)
                .child("request_type").setValue("sent")     //write "sent" in the child of uid ()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                                 if(task.isSuccessful())
                                 {
                                     //When write uid current user and write child of this uid,we write another tree (tree of user receive the request)
                                     // That's the same the Tree of current's uid
                                     ChatRequestRef.child(receiverUserID).child(senderUserID)
                                             .child("request_type").setValue("received")//write received in the tree of user receive the request
                                             .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                 @Override
                                                 public void onComplete(@NonNull Task<Void> task) {
                                                     {
                                                         if(task.isSuccessful())
                                                         {
                                                             SendMessageRequestButton.setEnabled(true);
                                                             Current_State="request_send";
                                                             SendMessageRequestButton.setText("Cancel Chat Request");
                                                         }
                                                     }
                                                 }
                                             });

                                 }
                    }
                });
    }
}
