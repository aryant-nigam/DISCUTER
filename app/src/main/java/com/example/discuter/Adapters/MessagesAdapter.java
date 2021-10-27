package com.example.discuter.Adapters;

import static com.example.discuter.Activities.ChatActivity.recieverImage;
import static com.example.discuter.Activities.ChatActivity.senderImage;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.discuter.ModelClass.Messages;
import com.example.discuter.R;
import com.example.discuter.UtilityClasses.CheckNetworkAvailibility;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter {
    Context chatActivity;
    ArrayList<Messages>chatList;
    HashMap<Long,String> messageKeys;
    String senderRoom,recieverRoom;
    int ITEM_SEND=1,ITEM_RECIEVE=2;

    public MessagesAdapter(Context chatActivity, ArrayList<Messages> chatList, HashMap<Long,String>messageKeys,String senderRoom,String recieverRoom) {
        this.chatActivity = chatActivity;
        this.chatList = chatList;
        this.messageKeys=messageKeys;
        this.senderRoom = senderRoom;
        this.recieverRoom=recieverRoom;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==ITEM_SEND)
        {
            View view = LayoutInflater.from(chatActivity).inflate(R.layout.sender_message_layout,parent,false);
            return new SenderViewHolder(view);
        }
        else
        {
            View view = LayoutInflater.from(chatActivity).inflate(R.layout.reciever_message_layout,parent,false);
            return new RecieverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Messages messages=chatList.get(position);

        int reactions[]=new int[]{
                R.drawable.emoji_like,
                R.drawable.emoji_laugh,
                R.drawable.emoji_love,
                R.drawable.emoji_party,
                R.drawable.emoji_shocked,
                R.drawable.emoji_angry,
                R.drawable.emoji_crying
        };


        ReactionsConfig config = new ReactionsConfigBuilder(chatActivity)
                .withReactions(reactions)
                .build();

        ReactionPopup popup = new ReactionPopup(chatActivity, config, (pos) -> {
            if(holder.getClass()==RecieverViewHolder.class)
            {
                RecieverViewHolder recieverViewHolder=(RecieverViewHolder) holder;
                if(pos!=-1) {
                    if(CheckNetworkAvailibility.isConnectionAvailable(chatActivity))
                    {
                        Log.d("Ary", String.valueOf(messageKeys.get(messages.getTimeStamp())));
                        messages.setReaction(pos);
                        DatabaseReference MESSAGE = FirebaseDatabase.getInstance().getReference().child("Chats").child(senderRoom).child("Messages").child(String.valueOf(messageKeys.get(messages.getTimeStamp())));
                        MESSAGE.setValue(messages).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                notifyDataSetChanged();
                                Log.d("Ary", senderRoom+"\n"+recieverRoom);
                                DatabaseReference RMESSAGE = FirebaseDatabase.getInstance().getReference().child("Chats").child(recieverRoom).child("Messages");
                                RMESSAGE.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for(DataSnapshot ss: snapshot.getChildren())
                                        {
                                            Messages temp=ss.getValue(Messages.class);
                                            if(temp.getTimeStamp().compareTo(messages.getTimeStamp())==0)
                                            {
                                                temp.setReaction(pos);
                                                RMESSAGE.child(ss.getKey()).setValue(temp).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(chatActivity, "Failed to put reaction", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(chatActivity, "Failed to put reaction", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(chatActivity, "Failed to put reaction", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    //updateReactionInDB(messages,pos);
                }
                //recieverViewHolder.reciever_reaction.setVisibility(View.VISIBLE);
            }
            return true;
        });

        if(holder.getClass()== SenderViewHolder.class)
        {
            SenderViewHolder senderViewHolder=(SenderViewHolder) holder;
            /*senderViewHolder.messageContainerSender.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v,event);
                    return false;
                }
            });*/

            if(messages.getMessageViewStatus())
                senderViewHolder.messageViewStatus.setImageResource(R.drawable.seen);
            if(!messages.getMessage().equals("") && !messages.getImageMessageLink().equals(""))
            {
                senderViewHolder.senderMessage.setVisibility(View.VISIBLE);
                senderViewHolder.senderMessage.setText(messages.getMessage());
                senderViewHolder.senderImageMessage.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getImageMessageLink()).placeholder(R.drawable.placeholder).into(senderViewHolder.senderImageMessage);
            }
            else if(messages.getMessage().equals("") && !messages.getImageMessageLink().equals("")) {
                senderViewHolder.senderImageMessage.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getImageMessageLink()).placeholder(R.drawable.placeholder).into(senderViewHolder.senderImageMessage);
                senderViewHolder.senderMessage.setVisibility(View.GONE);
            }
            else if(!messages.getMessage().equals("") && messages.getImageMessageLink().equals(""))
            {
                senderViewHolder.senderMessage.setVisibility(View.VISIBLE);
                senderViewHolder.senderMessage.setText(messages.getMessage());
                senderViewHolder.senderImageMessage.setVisibility(View.GONE);
            }
            if(messages.getReaction()!=-1) {
                senderViewHolder.sender_reaction.setVisibility(View.VISIBLE);
                senderViewHolder.sender_reaction.setImageResource(reactions[messages.getReaction()]);
            }
            else
            {
                senderViewHolder.sender_reaction.setVisibility(View.GONE);
            }
            Picasso.get().load(senderImage).into(senderViewHolder.senderImage);
        }
        else
        {
            RecieverViewHolder recieverViewHolder=(RecieverViewHolder) holder;

            recieverViewHolder.messageContainerReciever.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v,event);
                    return false;
                }
            });

            if(!messages.getMessage().equals("") && !messages.getImageMessageLink().equals(""))
            {
                recieverViewHolder.recieverMessage.setVisibility(View.VISIBLE);
                recieverViewHolder.recieverMessage.setText(messages.getMessage());
                recieverViewHolder.recieverImageMessage.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getImageMessageLink()).placeholder(R.drawable.placeholder).into(recieverViewHolder.recieverImageMessage);
            }
            else if(messages.getMessage().equals("") && !messages.getImageMessageLink().equals("")) {
                recieverViewHolder.recieverImageMessage.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getImageMessageLink()).placeholder(R.drawable.placeholder).into(recieverViewHolder.recieverImageMessage);
                recieverViewHolder.recieverMessage.setVisibility(View.GONE);
            }
            else if(!messages.getMessage().equals("") && messages.getImageMessageLink().equals(""))
            {
                recieverViewHolder.recieverMessage.setVisibility(View.VISIBLE);
                recieverViewHolder.recieverMessage.setText(messages.getMessage());
                recieverViewHolder.recieverImageMessage.setVisibility(View.GONE);
            }
            if(messages.getReaction()!=-1) {
                recieverViewHolder.reciever_reaction.setVisibility(View.VISIBLE);
                recieverViewHolder.reciever_reaction.setImageResource(reactions[messages.getReaction()]);
            }
            else
            {
                recieverViewHolder.reciever_reaction.setVisibility(View.GONE);
            }

            Picasso.get().load(recieverImage).into(recieverViewHolder.recieverImage);
        }
    }

    /*private void updateReactionInDB(Messages msg,int i) {
        if(CheckNetworkAvailibility.isConnectionAvailable(chatActivity))
        {
            Log.d("Ary", String.valueOf(messageKeys.get(msg.getTimeStamp())));
            msg.setReaction(i);
            DatabaseReference MESSAGE = FirebaseDatabase.getInstance().getReference().child("Chats").child(senderRoom).child("Messages").child(String.valueOf(messageKeys.get(msg.getTimeStamp())));
            MESSAGE.setValue(msg).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    notifyDataSetChanged();
                    DatabaseReference RMESSAGE = FirebaseDatabase.getInstance().getReference().child("Chats").child(recieverRoom).child("Messages");
                    RMESSAGE.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot ss: snapshot.getChildren())
                            {
                                Messages temp=ss.getValue(Messages.class);
                                if(temp.getTimeStamp()==msg.getTimeStamp())
                                {
                                    temp.setReaction(i);
                                    FirebaseDatabase.getInstance().getReference().child("Chats").child(msg.getSenderId()+senderId).child(ss.getKey()).setValue(temp).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) { }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(chatActivity, "Failed to put reaction", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    Log.d("Ary", ss.getKey());
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(chatActivity, "Failed to put reaction", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(chatActivity, "Failed to put reaction", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }*/

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Messages messages=chatList.get(position);
        if(FirebaseAuth.getInstance().getCurrentUser().getUid().equals(messages.getSenderId()))
            return ITEM_SEND;
        else
            return ITEM_RECIEVE;

    }

    class SenderViewHolder extends RecyclerView.ViewHolder {
        CircleImageView senderImage;
        TextView senderMessage;
        ImageView senderImageMessage,sender_reaction;
        LinearLayout messageContainerSender;
        ImageView messageViewStatus;
        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);

            senderImage=itemView.findViewById(R.id.profile_image_sender_chat_item);
            senderMessage=itemView.findViewById(R.id.sender_message);
            senderImageMessage=itemView.findViewById(R.id.sender_image_message);
            messageContainerSender=itemView.findViewById(R.id.messageContainerSender);
            sender_reaction=itemView.findViewById(R.id.sender_reaction);
            messageViewStatus=itemView.findViewById(R.id.messageViewStatus);
        }
    }

    class RecieverViewHolder extends RecyclerView.ViewHolder {
        CircleImageView recieverImage;
        TextView recieverMessage;
        ImageView recieverImageMessage,reciever_reaction;
        LinearLayout messageContainerReciever;
        public RecieverViewHolder(@NonNull View itemView) {
            super(itemView);

            recieverImage=itemView.findViewById(R.id.profile_image_reciever_chat_item);
            recieverMessage=itemView.findViewById(R.id.reciever_message);
            recieverImageMessage=itemView.findViewById(R.id.reciever_image_message);
            messageContainerReciever=itemView.findViewById(R.id.messageContainerReciever);
            reciever_reaction=itemView.findViewById(R.id.reciever_reaction);
        }
    }
}
