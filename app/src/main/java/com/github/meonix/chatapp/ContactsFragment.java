package com.github.meonix.chatapp;


import android.net.Uri;
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
public class ContactsFragment extends Fragment {

    private View ContactsView;
    private RecyclerView mycontactlist;
    private DatabaseReference ContactsRef,UserRef;
    private FirebaseAuth mAuth;
    private String currentUserID;
    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ContactsView = inflater.inflate(R.layout.fragment_contacts, container, false);

        mycontactlist = (RecyclerView) ContactsView.findViewById(R.id.contact_list);
        mycontactlist.setLayoutManager(new LinearLayoutManager(getContext()));
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();


        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        return  ContactsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options=new FirebaseRecyclerOptions.Builder<ContactsModel>()
                .setQuery(ContactsRef,ContactsModel.class)
                .build();
        FirebaseRecyclerAdapter<ContactsModel,ContactsViewHolder> adapter = new FirebaseRecyclerAdapter<ContactsModel, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull ContactsModel model) {
                String userID = getRef(position).getKey(); //Take each all the key of Contacts tree

                UserRef.child(userID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild("image"))
                        {
                            String profileiamge = dataSnapshot.child("image").getValue().toString();
                            String userName = dataSnapshot.child("name").getValue().toString();
                            String userStatus= dataSnapshot.child("status").getValue().toString();

                            holder.username.setText(userName);
                            holder.userStatus.setText(userStatus);
                            Picasso.get().load(profileiamge).into(holder.profileImage);
                        }
                        else{
                            String userName = dataSnapshot.child("name").getValue().toString();
                            String userStatus= dataSnapshot.child("status").getValue().toString();
                            holder.username.setText(userName);
                            holder.userStatus.setText(userStatus);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view =LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);
                ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                return  viewHolder;
            }
        };
        mycontactlist.setAdapter(adapter);
        adapter.startListening();
    }
    public class ContactsViewHolder extends RecyclerView.ViewHolder {

        TextView username,userStatus;
        CircleImageView profileImage;
        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            username =itemView.findViewById(R.id.user_profile_name);
            userStatus=itemView.findViewById(R.id.user_status);
            profileImage=itemView.findViewById(R.id.users_profile_image);
        }
    }


}

