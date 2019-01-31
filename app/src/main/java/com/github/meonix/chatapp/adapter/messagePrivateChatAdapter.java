package com.github.meonix.chatapp.adapter;

import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

import java.io.IOException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class messagePrivateChatAdapter extends RecyclerView.Adapter<messagePrivateChatAdapter.MessageViewHolder> {
    private List<MessagesChatModel> userMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private MediaPlayer   mPlayer = null;
    private boolean mStartPlaying;

    class MessageViewHolder extends RecyclerView.ViewHolder {
        CircleImageView avatarRevceiver;
        CardView receiverMessageCard, senderMessageCard;
        TextView PrivateMessageTime, PrivateMessageDate, receiverPrivateMessage;
        TextView senderPrivateMessage;
        ImageButton audioOfSender,audioOfReceiver;

        MessageViewHolder(@NonNull View itemview) {
            super(itemview);
            receiverMessageCard = itemview.findViewById(R.id.receiver_message_card);
            senderMessageCard = itemview.findViewById(R.id.sender_message_card);
            avatarRevceiver = itemview.findViewById(R.id.private_message_image);
            audioOfSender=itemview.findViewById(R.id.audioOfSender);
            audioOfReceiver=itemview.findViewById(R.id.audioOfReceiver);

            PrivateMessageTime = itemview.findViewById(R.id.private_message_time);
            PrivateMessageDate = itemview.findViewById(R.id.private_message_date);
            receiverPrivateMessage = itemview.findViewById(R.id.receiver_private_message);

            senderPrivateMessage = itemview.findViewById(R.id.sender_private_message);
        }
    }

    public messagePrivateChatAdapter(List<MessagesChatModel> userMessageList) {
        this.userMessageList = userMessageList;
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
        final MessagesChatModel messages = userMessageList.get(i);
        String fromUserID = messages.getFrom_uid();
        String timeOfMessage = messages.getTime();
        String dayOfMessage = messages.getDate();
        String fromMessage = messages.getTime();
        String fromMessageType = messages.getType();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

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

        mStartPlaying = true;
        messageViewHolder.audioOfSender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    onPlay(mStartPlaying,messages.getMessage());
                    if (mStartPlaying) {
                        messageViewHolder.audioOfSender.setImageResource(R.drawable.pausebtn);
                    } else {
                        messageViewHolder.audioOfSender.setImageResource(R.drawable.continuebtn);
                    }
                    mStartPlaying = !mStartPlaying;
//                    MediaPlayer player = new MediaPlayer();
//                    player.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                    player.setDataSource(messages.getMessage());
//                    player.prepare();
//                    player.start();
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
        });
        messageViewHolder.audioOfReceiver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    onPlay(mStartPlaying,messages.getMessage());
                    if (mStartPlaying) {
                        messageViewHolder.audioOfReceiver.setImageResource(R.drawable.pausebtn);
                    } else {
                        messageViewHolder.audioOfReceiver.setImageResource(R.drawable.continuebtn);
                    }
                    mStartPlaying = !mStartPlaying;
//                    MediaPlayer player = new MediaPlayer();
//                    player.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                    player.setDataSource(messages.getMessage());
//                    player.prepare();
//                    player.start();
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
        });

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("image")) {
                    String receiverImage = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile).into(messageViewHolder.avatarRevceiver);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if (fromMessageType.equals("text")) {
            messageViewHolder.receiverMessageCard.setVisibility(View.INVISIBLE);
            messageViewHolder.avatarRevceiver.setVisibility(View.INVISIBLE);
            messageViewHolder.senderMessageCard.setVisibility(View.INVISIBLE);
            messageViewHolder.audioOfSender.setVisibility(View.GONE);
            messageViewHolder.audioOfReceiver.setVisibility(View.GONE);

            messageViewHolder.PrivateMessageTime.setVisibility(View.INVISIBLE);
            messageViewHolder.PrivateMessageDate.setVisibility(View.INVISIBLE);

            if (fromUserID.equals(messageSenderID)) {
                messageViewHolder.senderMessageCard.setVisibility(View.VISIBLE);
                messageViewHolder.senderMessageCard.setBackgroundResource(R.drawable.sender_message_layout);
                messageViewHolder.senderPrivateMessage.setVisibility(View.VISIBLE);
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

                messageViewHolder.receiverPrivateMessage.setVisibility(View.VISIBLE);
                messageViewHolder.receiverPrivateMessage.setTextColor(Color.BLACK);
                messageViewHolder.receiverPrivateMessage.setText(messages.getMessage());
            }
        }
        else if(fromMessageType.equals("audio"))
        {
            messageViewHolder.receiverMessageCard.setVisibility(View.INVISIBLE);
            messageViewHolder.avatarRevceiver.setVisibility(View.INVISIBLE);
            messageViewHolder.senderMessageCard.setVisibility(View.INVISIBLE);
            messageViewHolder.audioOfSender.setVisibility(View.INVISIBLE);
            messageViewHolder.audioOfReceiver.setVisibility(View.INVISIBLE);
            messageViewHolder.senderPrivateMessage.setVisibility(View.GONE);
            messageViewHolder.receiverPrivateMessage.setVisibility(View.GONE);

            messageViewHolder.PrivateMessageTime.setVisibility(View.INVISIBLE);
            messageViewHolder.PrivateMessageDate.setVisibility(View.INVISIBLE);
            if (fromUserID.equals(messageSenderID)) {
                messageViewHolder.senderMessageCard.setVisibility(View.VISIBLE);
                messageViewHolder.senderMessageCard.setBackgroundResource(R.drawable.sender_message_layout);
                messageViewHolder.audioOfSender.setVisibility(View.VISIBLE);

                messageViewHolder.PrivateMessageTime.setText(messages.getTime());
                messageViewHolder.PrivateMessageDate.setText(messages.getDate());

            } else {

                messageViewHolder.PrivateMessageTime.setText(messages.getTime());
                messageViewHolder.PrivateMessageDate.setText(messages.getDate());
                messageViewHolder.senderMessageCard.setVisibility(View.INVISIBLE);
                messageViewHolder.receiverMessageCard.setVisibility(View.VISIBLE);
                messageViewHolder.audioOfReceiver.setVisibility(View.VISIBLE);

                messageViewHolder.avatarRevceiver.setVisibility(View.VISIBLE);

                messageViewHolder.receiverMessageCard.setBackgroundResource(R.drawable.receiver_message_layout);
                messageViewHolder.avatarRevceiver.setVisibility(View.VISIBLE);

            }
        }
    }


    private void onPlay(boolean start,String mFileName) {
        if (start) {
            startPlaying(mFileName);
        }
        else {
            stopPlaying();
        }
    }
    private void startPlaying(String mFileName) {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {

        }
    }
    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }


}
