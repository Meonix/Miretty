package com.github.meonix.chatapp.adapter;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.meonix.chatapp.R;
import com.github.meonix.chatapp.model.MessagesChatModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class messagePrivateChatAdapter extends RecyclerView.Adapter<messagePrivateChatAdapter.MessageViewHolder> {
    private List<MessagesChatModel> userMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    public messagePrivateChatAdapter(List<MessagesChatModel> userMessageList) {
        this.userMessageList = userMessageList;
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        CircleImageView avatarRevceiver;
        CardView receiverMessageCard, senderMessageCard;
        TextView PrivateMessageTime, PrivateMessageDate, receiverPrivateMessage;
        TextView senderPrivateMessage;

        MessageViewHolder(@NonNull View itemview) {
            super(itemview);
            receiverMessageCard = itemview.findViewById(R.id.receiver_message_card);
            senderMessageCard = itemview.findViewById(R.id.sender_message_card);
            avatarRevceiver = itemview.findViewById(R.id.private_message_image);

            PrivateMessageTime = itemview.findViewById(R.id.private_message_time);
            PrivateMessageDate = itemview.findViewById(R.id.private_message_date);
            receiverPrivateMessage = itemview.findViewById(R.id.receiver_private_message);

            senderPrivateMessage = itemview.findViewById(R.id.sender_private_message);
        }
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.private_message_layout, viewGroup, false);
        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int i) {
        String messageSenderID = mAuth.getCurrentUser().getUid();
        MessagesChatModel messages = userMessageList.get(i);
        String fromUserID = messages.getFrom_uid();
        String timeOfMessage = messages.getTime();
        String dayOfMessage = messages.getDate();
        String fromMessage = messages.getTime();
        String fromMessageType = messages.getType();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("image")) {
                    String receiverImage = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile).into(messageViewHolder.avatarRevceiver);
                }
                messageViewHolder.receiverMessageCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if( messageViewHolder.PrivateMessageTime.getVisibility()==View.INVISIBLE) {
                            messageViewHolder.PrivateMessageTime.setVisibility(View.VISIBLE);
                            messageViewHolder.PrivateMessageDate.setVisibility(View.VISIBLE);
                        }
                        else{
                            messageViewHolder.PrivateMessageTime.setVisibility(View.INVISIBLE);
                            messageViewHolder.PrivateMessageDate.setVisibility(View.INVISIBLE);
                        }
                    }
                });
                messageViewHolder.senderMessageCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if( messageViewHolder.PrivateMessageTime.getVisibility()==View.INVISIBLE) {
                            messageViewHolder.PrivateMessageTime.setVisibility(View.VISIBLE);
                            messageViewHolder.PrivateMessageDate.setVisibility(View.VISIBLE);
                        }
                        else{
                            messageViewHolder.PrivateMessageTime.setVisibility(View.INVISIBLE);
                            messageViewHolder.PrivateMessageDate.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if (fromMessageType.equals("text")) {
            messageViewHolder.receiverMessageCard.setVisibility(View.INVISIBLE);
            messageViewHolder.avatarRevceiver.setVisibility(View.INVISIBLE);
            messageViewHolder.senderMessageCard.setVisibility(View.INVISIBLE);

            messageViewHolder.PrivateMessageTime.setVisibility(View.INVISIBLE);
            messageViewHolder.PrivateMessageDate.setVisibility(View.INVISIBLE);

            if (fromUserID.equals(messageSenderID)) {
                messageViewHolder.senderMessageCard.setVisibility(View.VISIBLE);
                messageViewHolder.senderMessageCard.setBackgroundResource(R.drawable.sender_message_layout);
                messageViewHolder.senderPrivateMessage.setTextColor(Color.WHITE);
                messageViewHolder.senderPrivateMessage.setText(messages.getMessage());
                messageViewHolder.PrivateMessageTime.setText(messages.getTime());
                messageViewHolder.PrivateMessageDate.setText(messages.getDate());

            } else {

                messageViewHolder.PrivateMessageTime.setText(messages.getTime());
                messageViewHolder.PrivateMessageDate.setText(messages.getDate());
                messageViewHolder.senderMessageCard.setVisibility(View.INVISIBLE);
                messageViewHolder.receiverMessageCard.setVisibility(View.VISIBLE);

                messageViewHolder.avatarRevceiver.setVisibility(View.VISIBLE);

                messageViewHolder.receiverMessageCard.setBackgroundResource(R.drawable.receiver_message_layout);
                messageViewHolder.receiverPrivateMessage.setTextColor(Color.BLACK);
                messageViewHolder.receiverPrivateMessage.setText(messages.getMessage());
            }
        }
    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }


}
