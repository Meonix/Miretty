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
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.github.meonix.chatapp.model.ContactsModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {
    private View PrivateChatView;
    private RecyclerView chatList;
    private DatabaseReference ChatRef,UserRef;
    private FirebaseAuth mAuth;
    private String currentUserID;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        PrivateChatView = inflater.inflate(R.layout.fragment_chats2, container, false);
        mAuth = FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        ChatRef=FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        UserRef= FirebaseDatabase.getInstance().getReference().child("Users");
        chatList = PrivateChatView.findViewById(R.id.chats_list);
        chatList.setLayoutManager(new LinearLayoutManager(getContext()));
        return PrivateChatView;
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<ContactsModel> options =new FirebaseRecyclerOptions.Builder<ContactsModel>()
                .setQuery(ChatRef,ContactsModel.class)
                .build();
        FirebaseRecyclerAdapter<ContactsModel,ChatsViewHolder> adapter
                =new FirebaseRecyclerAdapter<ContactsModel, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull ContactsModel model) {
                    final String usersIDs = getRef(position).getKey();

                     final String[] Image = {"default_image"};
                    UserRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists())
                            {
                                if(dataSnapshot.hasChild("image"))
                                {
                                     Image[0] = dataSnapshot.child("image").getValue().toString();
                                    Picasso.get().load(Image[0]).into(holder.profileImageView);
                                }
                                final String Name=dataSnapshot.child("name").getValue().toString();
                                final String Status = dataSnapshot.child("status").getValue().toString();
                                holder.userName.setText(Name);
                                holder.userStatus.setText("Last seen: "+ "\n"+"date"+"time");

                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent chatItem= new Intent(getContext(),ChatActivity.class);
                                        chatItem.putExtra("visit_user_id",usersIDs);
                                        chatItem.putExtra("visit_user_name",Name);
                                        chatItem.putExtra("userImage", Image[0]);
                                        startActivity(chatItem);
                                    }
                                });
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);
                return new ChatsViewHolder(view);
            }
        };

        chatList.setAdapter(adapter);
        adapter.startListening();

    }




    public static class ChatsViewHolder extends RecyclerView.ViewHolder
    {
        private CircleImageView profileImageView;
        private TextView userStatus,userName;
        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.users_profile_image);
            userName =itemView.findViewById(R.id.user_profile_name);
            userStatus=itemView.findViewById(R.id.user_status);
        }
    }
}
