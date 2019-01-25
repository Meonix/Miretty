package com.github.meonix.chatapp.adapter;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.meonix.chatapp.R;
import com.github.meonix.chatapp.model.MessageModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterMessage extends RecyclerView.Adapter<AdapterMessage.ViewHolder> {
    private List<MessageModel> messageList;
    private FirebaseAuth mAuth;
    private DatabaseReference UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
    class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView cvAvatar;
        View layout;
        TextView message_time, message_user, message_text,message_date;

        ViewHolder(View v) {
            super(v);
            this.layout = v;
            cvAvatar = v.findViewById(R.id.cvAvatar);
            message_text = v.findViewById(R.id.message_text);
            message_time = v.findViewById(R.id.message_time);
            message_user = v.findViewById(R.id.message_user);
            message_date = v.findViewById(R.id.message_date);
        }
    }

    public AdapterMessage(List<MessageModel> messageList) {
        this.messageList = messageList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
//        View v = inflater.inflate(R.layout.item_chat_message, parent, false);
        View view =LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        mAuth = FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {
        String time = messageList.get(position).getTime();
        viewHolder.message_user.setText(messageList.get(position).getName());
        viewHolder.message_text.setText(messageList.get(position).getMessage());
        viewHolder.message_date.setText(messageList.get(position).getDate());
        viewHolder.message_time.setText(time);
        // TODO: set avater
        String uid = messageList.get(position).getUid();

        UserRef.child(messageList.get(position).getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if((dataSnapshot.exists()) && (dataSnapshot.hasChild("image"))) {
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(userImage).placeholder(R.drawable.profile).into(viewHolder.cvAvatar);
                }
                else
                {
                    Picasso.get().load(R.drawable.profile).placeholder(R.drawable.profile).into(viewHolder.cvAvatar);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


//        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
////            storageReference.child("Profile Images/" + uid + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
////                @Override
////                public void onSuccess(Uri uri) {
////                    Picasso.get().load(uri).into(viewHolder.cvAvatar);
////                }
////            }).addOnFailureListener(new OnFailureListener() {
////                @Override
////                public void onFailure(@NonNull Exception exception) {
////                    // Handle any errors
////                }
////            });
    }
    @Override
    public int getItemCount() {
        return messageList.size();
    }

}