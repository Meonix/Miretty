package com.github.meonix.chatapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.github.meonix.chatapp.model.ContactsModel;
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


/**
 * A simple {@link Fragment} subclass.*/
public class RequestsFragment extends Fragment {

    private View RequestsFragmentView;
    private RecyclerView myRequestsList;
    private DatabaseReference ChatRequestRef,UserRef,ContactsRef;
    private FirebaseAuth mAuth;  //use mAuth to take current user
    private String currentUserID;
    public RequestsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RequestsFragmentView = inflater.inflate(R.layout.fragment_requests, container, false);

        myRequestsList = (RecyclerView) RequestsFragmentView.findViewById(R.id.chat_request_list);
        myRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));

        ContactsRef  = FirebaseDatabase.getInstance().getReference().child("Contacts");
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();  //get current UserID
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        return RequestsFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<ContactsModel> options = new FirebaseRecyclerOptions.Builder<ContactsModel>()
                .setQuery(ChatRequestRef.child(currentUserID),ContactsModel.class)
                .build();
        FirebaseRecyclerAdapter<ContactsModel,RequestsViewHolder> adapter = new FirebaseRecyclerAdapter<ContactsModel, RequestsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, final int position, @NonNull ContactsModel model) {
                        final String visit_user_id = getRef(position).getKey();

                        final  String list_user_id = getRef(position).getKey();

                        DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();

                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                 if(dataSnapshot.exists())
                                 {
                                     String type = dataSnapshot.getValue().toString();
                                     if(type.equals("received"))
                                     {
                                         holder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);
                                         holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.VISIBLE);
                                        UserRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.hasChild("image"))
                                                {
                                                    final  String requestUserName = dataSnapshot.child("name").getValue().toString();

                                                    final  String requestUserStatus = dataSnapshot.child("status").getValue().toString();
                                                    final  String requestProfileImage = dataSnapshot.child("image").getValue().toString();

                                                    holder.userName.setText(requestUserName);
                                                    holder.userStatus.setText(requestUserStatus);
                                                    Picasso.get().load(requestProfileImage).into(holder.profileImage);
                                                }
                                                else {
                                                    final  String requestUserName = dataSnapshot.child("name").getValue().toString();
                                                    final  String requestUserStatus = dataSnapshot.child("status").getValue().toString();

                                                    holder.userName.setText(requestUserName);
                                                    holder.userStatus.setText(requestUserStatus);
                                                }
                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {

                                                        Intent profileIntent = new Intent(getActivity(),ProfileActivity.class);
                                                        profileIntent.putExtra("visit_user_id",visit_user_id); //Send this ID to the ProfileActivity
                                                        startActivity(profileIntent);
                                                    }
                                                });
                                                holder.AcceptButton.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                                ContactsRef.child(currentUserID).child(list_user_id)
                                                                        .child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if(task.isSuccessful()){
                                                                            ContactsRef.child(list_user_id).child(currentUserID)
                                                                                    .child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful()){
                                                                                            ChatRequestRef.child(currentUserID).child(list_user_id)
                                                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                    if(task.isSuccessful())
                                                                                                    {
                                                                                                        ChatRequestRef.child(list_user_id).child(currentUserID)
                                                                                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                            @Override
                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                if(task.isSuccessful())
                                                                                                                {
                                                                                                                    Toast.makeText(getContext(),"Add Friend successfully",Toast.LENGTH_SHORT).show();
                                                                                                                }
                                                                                                            }
                                                                                                        });
                                                                                                    }
                                                                                                }
                                                                                            });
                                                                                    }
                                                                                }
                                                                            });
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                });
                                                holder.CancelButton.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        ChatRequestRef.child(currentUserID).child(list_user_id)
                                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful())
                                                                {
                                                                    ChatRequestRef.child(list_user_id).child(currentUserID)
                                                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful())
                                                                            {
                                                                                Toast.makeText(getContext(),"The request has been rejected",Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        });
                                                    }
                                                });

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                     }
                                     else{
                                         holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.VISIBLE);
                                         UserRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                             @Override
                                             public void onDataChange(DataSnapshot dataSnapshot) {
                                                 if(dataSnapshot.hasChild("image"))
                                                 {
                                                     final  String requestUserName = dataSnapshot.child("name").getValue().toString();

                                                     final  String requestUserStatus = dataSnapshot.child("status").getValue().toString();
                                                     final  String requestProfileImage = dataSnapshot.child("image").getValue().toString();

                                                     holder.userName.setText(requestUserName);
                                                     holder.userStatus.setText(requestUserStatus);
                                                     Picasso.get().load(requestProfileImage).into(holder.profileImage);
                                                 }
                                                 else {
                                                     final  String requestUserName = dataSnapshot.child("name").getValue().toString();
                                                     final  String requestUserStatus = dataSnapshot.child("status").getValue().toString();

                                                     holder.userName.setText(requestUserName);
                                                     holder.userStatus.setText(requestUserStatus);
                                                 }
                                                 holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                     @Override
                                                     public void onClick(View v) {

                                                         Intent profileIntent = new Intent(getActivity(),ProfileActivity.class);
                                                         profileIntent.putExtra("visit_user_id",visit_user_id); //Send this ID to the ProfileActivity
                                                         startActivity(profileIntent);
                                                     }
                                                 });




                                                 holder.CancelButton.setOnClickListener(new View.OnClickListener() {
                                                     @Override
                                                     public void onClick(View v) {
                                                         ChatRequestRef.child(currentUserID).child(list_user_id)
                                                                 .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                             @Override
                                                             public void onComplete(@NonNull Task<Void> task) {
                                                                 if(task.isSuccessful())
                                                                 {
                                                                     ChatRequestRef.child(list_user_id).child(currentUserID)
                                                                             .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                         @Override
                                                                         public void onComplete(@NonNull Task<Void> task) {
                                                                             if(task.isSuccessful())
                                                                             {
                                                                                 Toast.makeText(getContext(),"The request has been rejected",Toast.LENGTH_SHORT).show();
                                                                             }
                                                                         }
                                                                     });
                                                                 }
                                                             }
                                                         });
                                                     }
                                                 });

                                             }

                                             @Override
                                             public void onCancelled(DatabaseError databaseError) {

                                             }
                                         });
                                     }
                                 }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);
                        RequestsViewHolder holder= new RequestsViewHolder(view);
                        return holder;
                    }
                };
                    myRequestsList.setAdapter(adapter);
                    adapter.startListening();

    }
    public static class RequestsViewHolder extends RecyclerView.ViewHolder {
        TextView userName,userStatus;
        CircleImageView profileImage;
        Button AcceptButton,CancelButton;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            AcceptButton = itemView.findViewById(R.id.request_accept_btn);
            CancelButton = itemView.findViewById(R.id.request_cancel_btn);
        }
    }
}


