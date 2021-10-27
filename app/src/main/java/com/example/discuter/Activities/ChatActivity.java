package com.example.discuter.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.discuter.Adapters.MessagesAdapter;
import com.example.discuter.ModelClass.Messages;
import com.example.discuter.R;
import com.example.discuter.UtilityClasses.CheckNetworkAvailibility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String recieverName,recieverProfile,recieverUid,senderUid,senderName;
    private CircleImageView profile;
    private TextView name;
    private FirebaseAuth authenticate;
    public static String senderImage,recieverImage;
    public EditText messageEntry;
    public ImageView sendMessage,imageAddedNotify_Image,imageAddedNotify_cancelBtn;
    private TextView imageAddedNotify_Text;
    private String SenderRoom,RecieverRoom;
    private RecyclerView chats;
    private ArrayList<Messages>chatsList;
    private Uri imageUri=null;
    private LinearLayout imageAddedNotification;
    private Animation popupAnimation;
    private Dialog sendAttachmentProgressDialog;
    private ScrollView scrollChats;
    private ValueEventListener valueEventListener;
    private DatabaseReference UPDATESEENSTATUS ;
    private HashMap<Long,String> messageKeys;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_activity);
        initialiseViews();
        UPDATESEENSTATUS=FirebaseDatabase.getInstance().getReference().child("Chats").child(RecieverRoom).child("Messages");

        MessagesAdapter messagesAdapter=new MessagesAdapter(ChatActivity.this,chatsList, messageKeys,SenderRoom,RecieverRoom);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        chats.setLayoutManager(linearLayoutManager);
        chats.setAdapter(messagesAdapter);

        DatabaseReference USER= FirebaseDatabase.getInstance().getReference().child("User").child(senderUid);
        USER.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                senderImage=snapshot.child("profileLink").getValue().toString();
                senderName=snapshot.child("username").getValue().toString();
                recieverImage=recieverProfile;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference CHATS= FirebaseDatabase.getInstance().getReference().child("Chats").child(SenderRoom).child("Messages");
        CHATS.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatsList.clear();
                messageKeys.clear();
                for(DataSnapshot ss:snapshot.getChildren())
                {
                    Messages msgobj=ss.getValue(Messages.class);
                    chatsList.add(msgobj);
                    messageKeys.put(msgobj.getTimeStamp(),ss.getKey());
                }
                messagesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        updateSeenStatusfetchPhase();


        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                imageAddedNotification.setVisibility(View.GONE);
                String msg= messageEntry.getText().toString();
                if(CheckNetworkAvailibility.isConnectionAvailable(ChatActivity.this))
                {
                    if(!msg.isEmpty() && imageUri != null)
                    {
                        sendCaptionedImageMessage();
                    //scrollChats.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                    else if (!msg.isEmpty())
                    {
                        sendTextMessage(msg);
                        //scrollChats.scrollTo(0,scrollChats.getMaxScrollAmount());
                    }
                    else if (imageUri != null)
                    {
                        sendImageMessage("");
                        //scrollChats.fullScroll(ScrollView.FOCUS_DOWN);
                    }

                }
                else
                {
                    Snackbar snackbar = Snackbar.make(v, "NETWORK UNAVAILABLE", Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    });
                    snackbar.setBackgroundTint(Color.parseColor("#93999C"));
                    snackbar.setTextColor(Color.parseColor("#222D32"));
                    snackbar.setActionTextColor(Color.parseColor("#171F24"));
                    snackbar.show();
                }
            }
        });
        sendMessage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                PopupMenu attachmentMenu=new PopupMenu(ChatActivity.this,sendMessage);
                try {
                    Field[] fields = attachmentMenu.getClass().getDeclaredFields();
                    for (Field field : fields) {
                        if ("mPopup".equals(field.getName())) {
                            field.setAccessible(true);
                            Object menuPopupHelper = field.get(attachmentMenu);
                            Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                            Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                            setForceIcons.invoke(menuPopupHelper, true);
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                attachmentMenu.getMenuInflater().inflate(R.menu.send_utilities,attachmentMenu.getMenu());

                attachmentMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId())
                        {
                            case R.id.imageAttachment:
                                openImageGallery();
                                break;
                        }
                        return true;
                    }
                });
                attachmentMenu.show();
                return true;
            }
        });

    }

    private void updateSeenStatusfetchPhase() {

        //DatabaseReference UPDATESEENSTATUS= FirebaseDatabase.getInstance().getReference().child("Chats").child(RecieverRoom).child("Messages");
        valueEventListener=UPDATESEENSTATUS.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    for(DataSnapshot ss:snapshot.getChildren())
                    {
                        Messages Rmessage=ss.getValue(Messages.class);
                        if(Rmessage.getMessageViewStatus()==false&&Rmessage.getSenderId().equals(recieverUid)){
                            Rmessage.setMessageViewStatus(true);
                            updateSeenStatusUpdatePhase(ss.getKey(),Rmessage);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    private void updateSeenStatusUpdatePhase(String key,Messages unseenMsg){
        DatabaseReference UPDATESEENSTATUS= FirebaseDatabase.getInstance().getReference().child("Chats").child(RecieverRoom).child("Messages").child(key);
        UPDATESEENSTATUS.setValue(unseenMsg).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void sendCaptionedImageMessage()
    {
        String msg=messageEntry.getText().toString();
        messageEntry.setText("");
        createProgressDialog();
        Calendar calendar = Calendar.getInstance();
        StorageReference CHATIMAGE = FirebaseStorage.getInstance().getReference().child("Chats").child(String.valueOf(calendar.getTimeInMillis()));
        CHATIMAGE.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    CHATIMAGE.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Messages messages = new Messages(msg, senderUid, new Date().getTime(), uri.toString(),false,senderImage,senderName,-1);

                            FirebaseDatabase.getInstance().getReference().child("Chats").child(SenderRoom).child("Messages").push().setValue(messages)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            FirebaseDatabase.getInstance().getReference().child("Chats").child(RecieverRoom).child("Messages").
                                                    push().setValue(messages).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    imageUri=null;
                                                    sendAttachmentProgressDialog.dismiss();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d("failure", e.getMessage());
                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("failure", e.getMessage());
                                }
                            });

                        }
                    });
                } else {
                    Toast.makeText(ChatActivity.this, "Failed to send image", Toast.LENGTH_SHORT).show();
                    sendAttachmentProgressDialog.dismiss();
                }
            }
        });

    }

    private void sendTextMessage(String msg)
    {
        messageEntry.setText("");
        Messages messages = new Messages(msg, senderUid, new Date().getTime(), "",false,senderImage,senderName,-1);
        FirebaseDatabase.getInstance().getReference().child("Chats").child(SenderRoom).child("Messages").push().setValue(messages)
        .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                FirebaseDatabase.getInstance().getReference().child("Chats").child(RecieverRoom).child("Messages").
                        push().setValue(messages).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("failure", e.getMessage());
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("failure", e.getMessage());
            }
        });
    }

    private void sendImageMessage(String msg)
    {
        createProgressDialog();
        Calendar calendar = Calendar.getInstance();
        StorageReference CHATIMAGE = FirebaseStorage.getInstance().getReference().child("Chats").child(String.valueOf(calendar.getTimeInMillis()));
        CHATIMAGE.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    CHATIMAGE.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Messages messages = new Messages(msg, senderUid, new Date().getTime(), uri.toString(),false,senderImage,senderName,-1);
                            updateChats(messages);
                            imageUri=null;
                        }
                    });
                } else {
                    Toast.makeText(ChatActivity.this, "Failed to send image", Toast.LENGTH_SHORT).show();
                    sendAttachmentProgressDialog.dismiss();
                }
            }
        });
    }

    private void updateChats(Messages messages)
    {
        FirebaseDatabase.getInstance().getReference().child("Chats").child(SenderRoom).child("Messages").push().setValue(messages)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        FirebaseDatabase.getInstance().getReference().child("Chats").child(RecieverRoom).child("Messages").
                                push().setValue(messages).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                sendAttachmentProgressDialog.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                imageUri = null;
                                Log.d("failure", e.getMessage());
                                sendAttachmentProgressDialog.dismiss();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                sendAttachmentProgressDialog.dismiss();
                Log.d("failure", e.getMessage());
            }
        });
    }

    private void openImageGallery()
    {
        Intent imageIntent=new Intent();
        imageIntent.setType("image/*");
        imageIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(imageIntent,"Pick your Profile Image"),10);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==10)
        {
            if(data!=null)
            {
                imageUri=data.getData();

                imageAddedNotification.setVisibility(View.VISIBLE);
                imageAddedNotification.startAnimation(popupAnimation);
                imageAddedNotify_Text=findViewById(R.id.image_added_notification_text);
                imageAddedNotify_Image=findViewById(R.id.image_added_notification_image);
                imageAddedNotify_cancelBtn=findViewById(R.id.image_added_notification_cancelBtn);

                Picasso.get().load(imageUri).placeholder(R.drawable.placeholder).into(imageAddedNotify_Image);

                imageAddedNotify_cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imageAddedNotification.setVisibility(View.GONE);
                        imageUri=null;
                    }
                });


            }
        }
    }

    private void initialiseViews()
    {
        recieverName=getIntent().getStringExtra("RecieverName");
        recieverProfile=getIntent().getStringExtra("RecieverProfile");
        recieverUid=getIntent().getStringExtra("RecieverUid");

        profile=findViewById(R.id.profile_image_chat);
        Picasso.get().load(recieverProfile).into(profile);
        name=findViewById(R.id.username_chat);
        name.setText(recieverName);

        messageEntry =findViewById(R.id.messageEntry);
        sendMessage =findViewById(R.id.sendMessage);


        authenticate=FirebaseAuth.getInstance();
        senderUid=authenticate.getUid();

        SenderRoom=senderUid+recieverUid;
        RecieverRoom=recieverUid+senderUid;

        chats=findViewById(R.id.chats);
        chatsList=new ArrayList<>();

        imageAddedNotification=findViewById(R.id.imageAddedNotification);
        popupAnimation = AnimationUtils.loadAnimation(ChatActivity.this,R.anim.popup);
        scrollChats=findViewById(R.id.scrollChats);

        messageKeys = new HashMap<>();


    }

    private void createProgressDialog()
    {
        sendAttachmentProgressDialog=new Dialog(ChatActivity.this);
        sendAttachmentProgressDialog.setContentView(R.layout.sending_image_dialog);
        sendAttachmentProgressDialog.setCancelable(false);

        sendAttachmentProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView attachmentMessage=sendAttachmentProgressDialog.findViewById(R.id.sendAttachmentMessage);
        attachmentMessage.setText("Sending Image");

        sendAttachmentProgressDialog.show();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        UPDATESEENSTATUS.removeEventListener(valueEventListener);
    }
}