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

import com.example.discuter.Adapters.GroupMessagesAdapter;
import com.example.discuter.ModelClass.Groups;
import com.example.discuter.ModelClass.Messages;
import com.example.discuter.ModelClass.User;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatActivity extends AppCompatActivity {

    private CircleImageView civ_group_profile_chatActivity;
    private TextView tv_groupname,tv_image_added_notification_text_groupchat;
    private ScrollView sv_scrollGroupChats;
    private RecyclerView rv_groupChats;
    private LinearLayout imageAddedNotification_groupchat;
    private ImageView iv_image_added_notification_image_groupchat,iv_image_added_notification_cancelBtn_groupchat,
                      iv_sendMessage_groupchat;
    private EditText et_messageEntry_groupchat;
    private Groups currentGroups;
    private Uri imageUri;
    private Animation popupAnimation;
    private Dialog sendAttachmentProgressDialog;
    private User currentUser;
    private ArrayList<Messages>groupChatList;
    private GroupMessagesAdapter groupMessagesAdapter;
    FirebaseAuth auth;
    private ImageView iv_GroupSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        initialise();

        DatabaseReference USER= FirebaseDatabase.getInstance().getReference().child("User").child(auth.getUid());
        USER.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               currentUser=snapshot.getValue(User.class);
                Log.d("TAG", currentUser.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference GROUPCHATS= FirebaseDatabase.getInstance().getReference().child("GroupChats").child(currentGroups.getGroupUid()).child("Messages");
        GROUPCHATS.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupChatList.clear();
                for(DataSnapshot ss:snapshot.getChildren())
                {
                    Messages msgobj=ss.getValue(Messages.class);
                    groupChatList.add(msgobj);
                }
                groupMessagesAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        rv_groupChats.setLayoutManager(new LinearLayoutManager(GroupChatActivity.this));
        groupMessagesAdapter=new GroupMessagesAdapter(GroupChatActivity.this,groupChatList);
        rv_groupChats.setAdapter(groupMessagesAdapter);

        iv_sendMessage_groupchat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               imageAddedNotification_groupchat.setVisibility(View.GONE);
                String msg= et_messageEntry_groupchat.getText().toString();

                if(CheckNetworkAvailibility.isConnectionAvailable(GroupChatActivity.this))
                {
                    if(!msg.isEmpty() && imageUri != null)
                    {
                        //sendCaptionedImageMessage();
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

        iv_sendMessage_groupchat.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                PopupMenu attachmentMenu=new PopupMenu(GroupChatActivity.this,iv_sendMessage_groupchat);
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

        iv_GroupSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(), UpdateGroupInfoActivity.class);
                intent.putExtra("GroupInfo", currentGroups);
                startActivity(intent);
            }
        });
    }

    private void sendCaptionedImageMessage()
    {
        String msg=et_messageEntry_groupchat.getText().toString();
        et_messageEntry_groupchat.setText("");
        createProgressDialog();
        Calendar calendar = Calendar.getInstance();
        StorageReference CHATIMAGE = FirebaseStorage.getInstance().getReference().child("GroupChats").child(String.valueOf(calendar.getTimeInMillis()));
        CHATIMAGE.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    CHATIMAGE.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Messages messages = new Messages(msg, currentUser.getUid(), new Date().getTime(), uri.toString(),false,currentUser.getProfileLink(),currentUser.getUsername(),-1);

                            FirebaseDatabase.getInstance().getReference().child("GroupChats").child(currentGroups.getGroupUid()).child("Messages").push().setValue(messages)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
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
                    });
                } else {
                    Toast.makeText(GroupChatActivity.this, "Failed to send image", Toast.LENGTH_SHORT).show();
                    sendAttachmentProgressDialog.dismiss();
                }
            }
        });

    }

    private void sendTextMessage(String msg)
    {
        et_messageEntry_groupchat.setText("");
        Messages messages = new Messages(msg, currentUser.getUid(), new Date().getTime(), "",false,currentUser.getProfileLink(),currentUser.getUsername(),-1);
        FirebaseDatabase.getInstance().getReference().child("GroupChats").child(currentGroups.getGroupUid()).child("Messages").push().setValue(messages)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
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
                            Messages messages = new Messages(msg, currentUser.getUid(), new Date().getTime(), uri.toString(),false,currentUser.getProfileLink(),currentUser.getUsername(),-1);
                            updateChats(messages);
                        }
                    });
                } else {
                    Toast.makeText(GroupChatActivity.this, "Failed to send image", Toast.LENGTH_SHORT).show();
                    sendAttachmentProgressDialog.dismiss();
                }
            }
        });
    }

    private void updateChats(Messages messages)
    {
        FirebaseDatabase.getInstance().getReference().child("GroupChats").child(currentGroups.getGroupUid()).child("Messages").push().setValue(messages)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                    sendAttachmentProgressDialog.dismiss();
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==10)
        {
            if(data!=null)
            {
                imageUri=data.getData();

                imageAddedNotification_groupchat.setVisibility(View.VISIBLE);
                imageAddedNotification_groupchat.startAnimation(popupAnimation);
                tv_image_added_notification_text_groupchat=findViewById(R.id.tv_image_added_notification_text_groupchat);
                iv_image_added_notification_image_groupchat=findViewById(R.id.iv_image_added_notification_image_groupchat);
                iv_image_added_notification_cancelBtn_groupchat=findViewById(R.id.iv_image_added_notification_cancelBtn_groupchat);

                Picasso.get().load(imageUri).placeholder(R.drawable.placeholder).into(iv_image_added_notification_image_groupchat);

                iv_image_added_notification_cancelBtn_groupchat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imageAddedNotification_groupchat.setVisibility(View.GONE);
                        imageUri=null;
                    }
                });
            }
        }
    }

    private void initialise() {
        civ_group_profile_chatActivity=findViewById(R.id.civ_group_profile_chatActivity);
        tv_groupname=findViewById(R.id.tv_groupname);
        sv_scrollGroupChats=findViewById(R.id.sv_scrollGroupChats);
        rv_groupChats=findViewById(R.id.rv_groupChats);

        imageAddedNotification_groupchat=findViewById(R.id.imageAddedNotification_groupchat);
        tv_image_added_notification_text_groupchat=findViewById(R.id.tv_image_added_notification_text_groupchat);
        iv_image_added_notification_image_groupchat=findViewById(R.id.iv_image_added_notification_image_groupchat);
        iv_image_added_notification_cancelBtn_groupchat=findViewById(R.id.iv_image_added_notification_cancelBtn_groupchat);

        iv_sendMessage_groupchat=findViewById(R.id.iv_sendMessage_groupchat);
        et_messageEntry_groupchat=findViewById(R.id.et_messageEntry_groupchat);

        currentGroups =(Groups) getIntent().getSerializableExtra("GroupData");
        DatabaseReference GROUPS=FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroups.getGroupUid());
        GROUPS.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    currentGroups = snapshot.getValue(Groups.class);
                    Picasso.get().load(currentGroups.getGroupProfile()).placeholder(R.drawable.default_group_profile).into(civ_group_profile_chatActivity);
                    tv_groupname.setText(currentGroups.getGroupName());
                }
            }
            @Override  public void onCancelled(@NonNull DatabaseError error) { Log.d("TAG", error.getMessage()); }
        });
        iv_GroupSettings=findViewById(R.id.iv_GroupSettings);

        popupAnimation = AnimationUtils.loadAnimation(GroupChatActivity.this,R.anim.popup);

        groupChatList=new ArrayList<>();
        auth=FirebaseAuth.getInstance();
    }

    private void createProgressDialog()
    {
        sendAttachmentProgressDialog=new Dialog(GroupChatActivity.this);
        sendAttachmentProgressDialog.setContentView(R.layout.sending_image_dialog);
        sendAttachmentProgressDialog.setCancelable(false);

        sendAttachmentProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView attachmentMessage=sendAttachmentProgressDialog.findViewById(R.id.sendAttachmentMessage);
        attachmentMessage.setText("Sending Image");

        sendAttachmentProgressDialog.show();

    }
}