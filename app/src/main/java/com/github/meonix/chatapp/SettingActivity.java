package com.github.meonix.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {

    private Button UdateAccountSettings;
    private ImageView backgroundProfileImage;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;
    private Uri UriImagebackground;
    private Toolbar mToolbar;
    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private static final int GalleryPick = 1;
    private static final int MyPick = 2 ;
    private FirebaseStorage storage;
    private StorageReference UserProfileImageRef,UserBackGroundImage;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        storage=FirebaseStorage.getInstance();
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        UserBackGroundImage =   FirebaseStorage.getInstance().getReference().child("BackGround Images");

        InitializeFields();

        userName.setVisibility(View.INVISIBLE);


        UdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSetting();
            }
        });


        RetrieveUserInto();

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);
            }
        });

        backgroundProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent GIntent = new Intent();
                GIntent.setAction(Intent.ACTION_GET_CONTENT);
                GIntent.setType("image/*");
                startActivityForResult(GIntent, MyPick);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        RetrieveUserInto();
    }

    private void InitializeFields() {
        UdateAccountSettings = (Button) findViewById(R.id.update_settings_buttton);
        userName = (EditText) findViewById(R.id.set_user_name);
        mToolbar=(Toolbar) findViewById(R.id.setting_toolbar);
        userStatus = (EditText) findViewById(R.id.set_profile_status);
        userProfileImage = (CircleImageView) findViewById(R.id.set_profile_image);
        backgroundProfileImage=(ImageView)  findViewById(R.id.background_profile_image);
        loadingBar = new ProgressDialog(this);

    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MyPick && resultCode== RESULT_OK && data != null && data.getData()!= null)
        {
            UriImagebackground = data.getData();
            loadingBar.setTitle("Set BackGround Image");
            loadingBar.setMessage("Please wait ,your  backGround image is updating....");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            if(UriImagebackground!=null)
            {
                StorageReference red = UserBackGroundImage.child(currentUserID+".jpg");
                red.putFile(UriImagebackground).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(SettingActivity.this, "Background Image uploaded Successfully...", Toast.LENGTH_SHORT).show();
                            final String downloadUrl= task.getResult().getDownloadUrl().toString();
                            RootRef.child("Users").child(currentUserID).child("BackGround_Image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        Toast.makeText(SettingActivity.this,"Image save in Database Successfully.....",Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                    else
                                    {
                                        String message = task.getException().toString();
                                        Toast.makeText(SettingActivity.this, "Error" + message, Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                }
                            });
                        }
                        else{
                            String message = task.getException().toString();
                            Toast.makeText(SettingActivity.this, "Error :" + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null) {

            Uri ImageUri = data.getData();
            CropImage.activity(ImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON).start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                CropImage.ActivityResult result = CropImage.getActivityResult(data);

                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please wait ,your  profile image is updating....");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri = result.getUri();

                StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SettingActivity.this, "Profile Image uploaded Successfully...", Toast.LENGTH_SHORT).show();
                            final String downloadUrl= task.getResult().getDownloadUrl().toString();
                            RootRef.child("Users").child(currentUserID).child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())
                                        {
                                            Toast.makeText(SettingActivity.this,"Image save in Database Successfully.....",Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                        else
                                        {
                                            String message = task.getException().toString();
                                            Toast.makeText(SettingActivity.this, "Error" + message, Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                    }
                            });
                        }
                        else{
                            String message = task.getException().toString();
                            Toast.makeText(SettingActivity.this, "Error :" + message, Toast.LENGTH_SHORT).show();
                        }
                    }

                });
            }

        }


    }

    private void UpdateSetting() {
        String setUserName = userName.getText().toString();
        String setStatus = userStatus.getText().toString();
        if (TextUtils.isEmpty(setUserName)) {
            Toast.makeText(this, "Please write your user name first....", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setStatus)) {
            Toast.makeText(this, "Please write your status....", Toast.LENGTH_SHORT).show();
        } else {
            RootRef.child("Users").child(currentUserID).child("name").setValue(setUserName).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        SendUserToMainActivity();
                        Toast.makeText(SettingActivity.this, "Name Updated Successfully..", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        String message = task.getException().toString();
                        Toast.makeText(SettingActivity.this, "Error" + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            RootRef.child("Users").child(currentUserID).child("status").setValue(setStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        SendUserToMainActivity();
                        Toast.makeText(SettingActivity.this, "status Updated Successfully..", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        String message = task.getException().toString();
                        Toast.makeText(SettingActivity.this, "Error" + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }


    private void RetrieveUserInto() {
        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")) && (dataSnapshot.hasChild("image")) && (dataSnapshot.hasChild("BackGround_Image")))
                {
                    String retriveUserName = dataSnapshot.child("name").getValue().toString();
                    String retriveStatus = dataSnapshot.child("status").getValue().toString();
                    String retriveProfileImage = dataSnapshot.child("image").getValue().toString();
                    String retriveBackground = dataSnapshot.child("BackGround_Image").getValue().toString();

                    userName.setText(retriveUserName);
                    userStatus.setText(retriveStatus);
                    Picasso.get().load(retriveProfileImage).into(userProfileImage);
                    Picasso.get().load(retriveBackground).into(backgroundProfileImage);
                }
                else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))
                        && (dataSnapshot.hasChild("image")) && !(dataSnapshot.hasChild("BackGround_Image")))
                {
                    String retriveUserName = dataSnapshot.child("name").getValue().toString();
                    String retriveStatus = dataSnapshot.child("status").getValue().toString();
                    String retriveProfileImage = dataSnapshot.child("image").getValue().toString();

                    userName.setText(retriveUserName);
                    userStatus.setText(retriveStatus);
                    Picasso.get().load(retriveProfileImage).into(userProfileImage);
                }
                else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))
                        && !(dataSnapshot.hasChild("image")) && (dataSnapshot.hasChild("BackGround_Image")))
                {
                    String retriveUserName = dataSnapshot.child("name").getValue().toString();
                    String retriveStatus = dataSnapshot.child("status").getValue().toString();
                    String retriveBackground = dataSnapshot.child("BackGround_Image").getValue().toString();

                    userName.setText(retriveUserName);
                    userStatus.setText(retriveStatus);
                    Picasso.get().load(retriveBackground).into(backgroundProfileImage);
                }

                else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")) && !(dataSnapshot.hasChild("image")) && !(dataSnapshot.hasChild("BackGround_Image"))) {
                    String retriveUserName = dataSnapshot.child("name").getValue().toString();
                    String retriveStatus = dataSnapshot.child("status").getValue().toString();

                    userName.setText(retriveUserName);
                    userStatus.setText(retriveStatus);
                }
                else {
                    userName.setVisibility(View.VISIBLE);
                    Toast.makeText(SettingActivity.this, "Please set & update your profile information.....", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(mainIntent);
        finish();
    }

}
