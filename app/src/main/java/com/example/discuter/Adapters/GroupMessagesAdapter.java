package com.example.discuter.Adapters;

import static com.example.discuter.Activities.ChatActivity.recieverImage;
import static com.example.discuter.Activities.ChatActivity.senderImage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.discuter.ModelClass.Messages;
import com.example.discuter.R;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupMessagesAdapter extends RecyclerView.Adapter {
    Context GroupChatActvity;
    ArrayList<Messages>chatList;
    int ITEM_SEND=1,ITEM_RECIEVE=2;

    public GroupMessagesAdapter(Context chatActvity, ArrayList<Messages> chatList) {
        GroupChatActvity = chatActvity;
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==ITEM_SEND)
        {
            View view = LayoutInflater.from(GroupChatActvity).inflate(R.layout.sender_group_message_layout,parent,false);
            return new SenderViewHolder(view);
        }
        else
        {
            View view = LayoutInflater.from(GroupChatActvity).inflate(R.layout.reciever_group_message_layout,parent,false);
            return new RecieverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Messages messages=chatList.get(position);


        if(holder.getClass()== SenderViewHolder.class)
        {
            SenderViewHolder senderViewHolder=(SenderViewHolder) holder;

            if(!messages.getMessage().equals("") && !messages.getImageMessageLink().equals(""))
            {
                senderViewHolder.sender_groupmessage.setVisibility(View.VISIBLE);
                senderViewHolder.sender_groupmessage.setText(messages.getMessage());
                senderViewHolder.sender_image_groupmessage.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getImageMessageLink()).placeholder(R.drawable.placeholder).into(senderViewHolder.sender_image_groupmessage);
            }
            else if(messages.getMessage().equals("") && !messages.getImageMessageLink().equals(""))
            {
                senderViewHolder.sender_image_groupmessage.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getImageMessageLink()).placeholder(R.drawable.placeholder).into(senderViewHolder.sender_image_groupmessage);
                senderViewHolder.sender_groupmessage.setVisibility(View.GONE);
            }
            else if(!messages.getMessage().equals("") && messages.getImageMessageLink().equals(""))
            {
                senderViewHolder.sender_groupmessage.setVisibility(View.VISIBLE);
                senderViewHolder.sender_groupmessage.setText(messages.getMessage());
                senderViewHolder.sender_image_groupmessage.setVisibility(View.GONE);
            }
            senderViewHolder.sender_message_id.setText("You");
            Picasso.get().load(messages.getSenderProfileLink()).placeholder(R.drawable.default_profile).into(senderViewHolder.profile_image_sender_groupchat_item);

        }
        else
        {
            RecieverViewHolder recieverViewHolder=(RecieverViewHolder) holder;

            if(!messages.getMessage().equals("") && !messages.getImageMessageLink().equals(""))
            {
                recieverViewHolder.reciever_groupmessage.setVisibility(View.VISIBLE);
                recieverViewHolder.reciever_groupmessage.setText(messages.getMessage());
                recieverViewHolder.reciever_image_groupmessage.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getImageMessageLink()).placeholder(R.drawable.placeholder).into(recieverViewHolder.reciever_image_groupmessage);
            }
            else if(messages.getMessage().equals("") && !messages.getImageMessageLink().equals("")) {
                recieverViewHolder.reciever_image_groupmessage.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getImageMessageLink()).placeholder(R.drawable.placeholder).into(recieverViewHolder.reciever_image_groupmessage);
                recieverViewHolder.reciever_groupmessage.setVisibility(View.GONE);

            }
            else if(!messages.getMessage().equals("") && messages.getImageMessageLink().equals(""))
            {
                recieverViewHolder.reciever_groupmessage.setVisibility(View.VISIBLE);
                recieverViewHolder.reciever_groupmessage.setText(messages.getMessage());
                recieverViewHolder.reciever_image_groupmessage.setVisibility(View.GONE);
            }


            recieverViewHolder.reciever_message_id.setText(messages.getSenderName());
            Picasso.get().load(messages.getSenderProfileLink()).placeholder(R.drawable.default_profile).into(recieverViewHolder.profile_image_reciever_groupchat_item);

        }
    }

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
        CircleImageView profile_image_sender_groupchat_item;
        TextView sender_groupmessage,sender_message_id;
        ImageView sender_image_groupmessage,sender_reaction_group;
        LinearLayout groupMessageContainerSender;
        ImageView groupmessageViewStatus;
        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);

            profile_image_sender_groupchat_item=itemView.findViewById(R.id.profile_image_sender_groupchat_item);
            sender_message_id=itemView.findViewById(R.id.sender_message_id);
            sender_groupmessage=itemView.findViewById(R.id.sender_groupmessage);
            sender_image_groupmessage=itemView.findViewById(R.id.sender_image_groupmessage);
            groupMessageContainerSender=itemView.findViewById(R.id.groupMessageContainerSender);
            sender_reaction_group=itemView.findViewById(R.id.sender_reaction_group);
            groupmessageViewStatus=itemView.findViewById(R.id.groupmessageViewStatus);
        }
    }

    class RecieverViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profile_image_reciever_groupchat_item;
        TextView reciever_groupmessage,reciever_message_id;
        ImageView reciever_image_groupmessage,reciever_reactiongroup;
        LinearLayout groupMessageContainerReciever;
        public RecieverViewHolder(@NonNull View itemView) {
            super(itemView);

            profile_image_reciever_groupchat_item=itemView.findViewById(R.id.profile_image_reciever_groupchat_item);
            reciever_message_id=itemView.findViewById(R.id.reciever_message_id);
            reciever_groupmessage=itemView.findViewById(R.id.reciever_groupmessage);
            reciever_image_groupmessage=itemView.findViewById(R.id.reciever_image_groupmessage);
            groupMessageContainerReciever=itemView.findViewById(R.id.messageContainerReciever);
            reciever_reactiongroup=itemView.findViewById(R.id.reciever_reaction);
        }
    }
}
