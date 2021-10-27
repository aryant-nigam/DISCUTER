package com.example.discuter.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.discuter.Adapters.GroupMembersAdapter;
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
import com.google.android.material.textfield.TextInputEditText;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateGroupInfoActivity extends AppCompatActivity {
    private ProgressBar load7;
    private CircleImageView civ_updateGroupProfile;
    private ImageView iv_camera2,iv_removeGroupProfile,iv_exitGroup,iv_deleteGroup,iv_addMemberToGroup;
    private TextInputEditText til_updateGroupName,til_updateGroupDescription;
    private Button btn_saveGroupChanges;
    private Groups currentGroup;
    private Uri newProfileUri;
    private int flagUnchanged=0;
    private FirebaseAuth auth;
    private User currentUser;
    private RecyclerView rv_groupMembersList;
    private ArrayList<User>memberList;
    private CardView iv_deleteGroupHolder;
    private int imageRemovedFlag;
    private LinearLayout groupProfileHolder;
    private Animation popup;
    private GroupMembersAdapter adapter;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //Intent intent=new Intent(getApplicationContext(),GroupChatActivity.class);
        //intent.putExtra("GroupData",currentGroup);
        //startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_group_info);
        initialise();


        iv_camera2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imageIntent=new Intent();
                imageIntent.setType("image/*");
                imageIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(imageIntent,"Pick your Profile Image"),11);
            }
        });

        iv_removeGroupProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CheckNetworkAvailibility.isConnectionAvailable(getApplicationContext()))
                {
                    if(!currentGroup.getGroupProfile().equals("https://firebasestorage.googleapis.com/v0/b/discuter-8ec5d.appspot.com/o/DefaultGroupProfile.jpg?alt=media&token=3125349c-c46b-4941-b772-54eb8c29e853"));
                    {
                        FirebaseStorage.getInstance().getReference().child("Group Profile Images").child(currentGroup.getGroupUid()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                currentGroup.setGroupProfile("https://firebasestorage.googleapis.com/v0/b/discuter-8ec5d.appspot.com/o/DefaultGroupProfile.jpg?alt=media&token=3125349c-c46b-4941-b772-54eb8c29e853");
                                imageRemovedFlag = 1;
                                Picasso.get().load(currentGroup.getGroupProfile()).into(civ_updateGroupProfile);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "Failed to remove the group profile", Toast.LENGTH_SHORT).show();
                            }
                        });
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

        iv_addMemberToGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CheckNetworkAvailibility.isConnectionAvailable(getApplicationContext()))
                {
                    Intent intent = new Intent(getApplicationContext(), SelectGroupMembers.class);
                    intent.putExtra("GroupData", currentGroup);
                    intent.putExtra("from", "UGA");
                    startActivity(intent);
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

        iv_exitGroup.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                Dialog dialog=new Dialog(UpdateGroupInfoActivity.this);
                dialog.setContentView(R.layout.logout_dialog);
                dialog.setCancelable(false);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                TextView message=dialog.findViewById(R.id.tv_logout_message);
                TextView Yes=dialog.findViewById(R.id.ok2);
                TextView No=dialog.findViewById(R.id.no);

                message.setText("Are you sure you want to leave this group?");
                Yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        load7.setVisibility(View.VISIBLE);
                        exitGroup();
                        load7.setVisibility(View.GONE);
                    }
                });
                No.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        load7.setVisibility(View.GONE);
                    }
                });
               dialog.show();

            }
        });

        iv_deleteGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CheckNetworkAvailibility.isConnectionAvailable(getApplicationContext()))
                {
                    load7.setVisibility(View.VISIBLE);
                    for (int i = 0; i < currentGroup.getGroupMembers().size(); i++) {
                        User u = currentGroup.getGroupMembers().get(i);
                        if (u.removeGroup(currentGroup.getGroupUid()) != -1 || u.removeGroup(currentGroup.getGroupUid()) != 0) {
                            DatabaseReference USER = FirebaseDatabase.getInstance().getReference().child("User").child(u.getUid());
                            USER.setValue(u).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    load7.setVisibility(View.VISIBLE);
                                    Log.d("TAG", e.getMessage());
                                }
                            });
                        }
                        if (i == currentGroup.getGroupMembers().size() - 1) {
                            load7.setVisibility(View.VISIBLE);
                            Toast.makeText(getApplicationContext(), "You successfully deleted the group", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), ActivityTab.class));
                        }
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

        btn_saveGroupChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CheckNetworkAvailibility.isConnectionAvailable(getApplicationContext())) {

                    if(validate()) {
                        currentGroup.setGroupName(til_updateGroupName.getText().toString().trim());
                        currentGroup.setGroupDescription(til_updateGroupDescription.getText().toString().trim());

                        if(imageRemovedFlag==1)
                        {
                            load7.setVisibility(View.VISIBLE);
                            updateGroupData();
                            imageRemovedFlag=0;
                        }
                        else if (newProfileUri.toString().equals(currentGroup.getGroupProfile())) {
                            load7.setVisibility(View.VISIBLE);
                            updateGroupData();
                        }
                        else
                            load7.setVisibility(View.VISIBLE);
                            updateGroupDataWithProfile();
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

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void exitGroup()
    {
        if( currentGroup.getGroupMembers()!=null)
        {
            for (int i = 0; i < currentGroup.getGroupMembers().size(); i++) {
                if (currentGroup.getGroupMembers().get(i).getUid().equals(currentUser.getUid())) {
                    currentGroup.getGroupMembers().remove(i);
                    break;
                }
            }
        }
        if(currentUser.getGroups()!=null)
        {
            for (int i = 0; i < currentUser.getGroups().size(); i++) {
                if (currentUser.getGroups().get(i).equals(currentGroup.getGroupUid())) {
                    currentUser.getGroups().remove(i);
                    break;
                }
            }
        }
        sendQuitMessage();
        FirebaseDatabase.getInstance().getReference().child("User").child(currentUser.getUid()).setValue(currentUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroup.getGroupUid()).setValue(currentGroup)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(getApplicationContext(), "You left the group", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(getApplicationContext(),ActivityTab.class));
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "Failed to leave the group", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Failed to leave the group", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendQuitMessage()
    {
        ZoneId zoneId=ZoneId.of("Asia/Kolkata");
        LocalDate date=LocalDate.now(zoneId);
        LocalTime time=LocalTime.now(zoneId);
        Messages message=new Messages
                (
                currentUser.getUsername()+" has left the group on "+date.toString()+" at "+time.toString().substring(0,8),
                currentUser.getUid(),
                Calendar.getInstance().getTimeInMillis(),
                "",
                false,
                "https://firebasestorage.googleapis.com/v0/b/discuter-8ec5d.appspot.com/o/IMG_20211005_161002%5B1%5D.jpg?alt=media&token=12153a4e-25b0-44f8-896a-e15584cc4960",
                "Discuter",
                -1
        );
        //Log.d("Ary", message.getMessage());

        FirebaseDatabase.getInstance().getReference().child("GroupChats").child(currentGroup.getGroupUid()).child("Messages").push().setValue(message)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) { }})
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) { Log.d("failure", e.getMessage()); }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==11){
            if(data!=null){
                newProfileUri=data.getData();
                civ_updateGroupProfile.setImageURI(newProfileUri);
            }
        }
    }

    private  boolean  validate()
    {
        if(til_updateGroupName.getText().toString().trim().equals("")){
            Toast.makeText(getApplicationContext(), "Group ame can not be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(til_updateGroupDescription.getText().toString().trim().equals(""))
        {
            til_updateGroupDescription.setText("Discuter Groups - celebrating togetherness ..");
            return false;
        }
        if(til_updateGroupName.getText().toString().trim().equals(currentGroup.getGroupName())&&til_updateGroupDescription.getText().toString().trim().equals(currentGroup.getGroupDescription())) {
            flagUnchanged=1;
            return true;
        }
        if(hasSpecialCharacters(til_updateGroupName.getText().toString())){
            Toast.makeText(getApplicationContext(), "Only Letters, Numbers and Spaces allowed allowed in groupname", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private  void  updateGroupData()
    {
        if(flagUnchanged!=1||imageRemovedFlag==1) {
            DatabaseReference GROUPS = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroup.getGroupUid());
            GROUPS.setValue(currentGroup).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(getApplicationContext(), "Succesfully Updated !!", Toast.LENGTH_SHORT).show();
                    load7.setVisibility(View.GONE);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "Failed To Update", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private  void  updateGroupDataWithProfile()
    {
       StorageReference GROUPPROFILE = FirebaseStorage.getInstance().getReference().child("Group Profile Images").child(currentGroup.getGroupUid());
       GROUPPROFILE.putFile(newProfileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        GROUPPROFILE.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                currentGroup.setGroupProfile(uri.toString());
                                DatabaseReference GROUPS = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroup.getGroupUid());
                                GROUPS.setValue(currentGroup).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        load7.setVisibility(View.GONE);
                                        Toast.makeText(getApplicationContext(), "Succesfully Updated", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(), "Failed to Update", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
                    }
                }
            });

    }


    private void initialise()
    {
        load7=findViewById(R.id.load7);
        civ_updateGroupProfile=findViewById(R.id.civ_updateGroupProfile);
        iv_camera2=findViewById(R.id.iv_camera2);
        iv_removeGroupProfile=findViewById(R.id.iv_removeGroupProfile);
        iv_exitGroup=findViewById(R.id.iv_exitGroup);
        iv_deleteGroup=findViewById(R.id.iv_deleteGroup);
        iv_addMemberToGroup=findViewById(R.id.iv_addMemberToGroup);
        til_updateGroupName=findViewById(R.id.til_updateGroupName);
        til_updateGroupDescription=findViewById(R.id.til_updateGroupDescription);
        btn_saveGroupChanges=findViewById(R.id.btn_saveGroupChanges);
        groupProfileHolder=findViewById(R.id.groupProfileHolder);

        popup= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.popup);
        popup.setDuration(400);
        groupProfileHolder.startAnimation(popup);

        currentGroup =(Groups)  getIntent().getSerializableExtra("GroupInfo");

        if (CheckNetworkAvailibility.isConnectionAvailable(getApplicationContext()))
        {
            try {
                FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroup.getGroupUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        currentGroup = snapshot.getValue(Groups.class);
                        Picasso.get().load(currentGroup.getGroupProfile()).into(civ_updateGroupProfile);
                        til_updateGroupName.setText(currentGroup.getGroupName());
                        til_updateGroupDescription.setText(currentGroup.getGroupDescription());
                        newProfileUri = Uri.parse(currentGroup.getGroupProfile());

                        try
                        {
                            FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroup.getGroupUid()).child("groupMembers").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot ss : snapshot.getChildren()) {
                                        memberList.add(ss.getValue(User.class));
                                        adapter.notifyDataSetChanged();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });
                        }
                        catch (Exception e)
                        {
                            Log.d("TAG", e.getMessage());
                        }

                        memberList.clear();

                        rv_groupMembersList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        adapter=new GroupMembersAdapter(UpdateGroupInfoActivity.this,memberList,currentGroup);
                        rv_groupMembersList.setAdapter(adapter);

                        FirebaseDatabase.getInstance().getReference().child("User").child(auth.getUid()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                currentUser = snapshot.getValue(User.class);

                                if (currentUser.getUid().equals(currentGroup.getAdminUid())) {
                                    iv_deleteGroupHolder.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            } catch (Exception e) {
                Log.d("TAG", e.getMessage());
            }
        }
        else
        {
            Snackbar snackbar = Snackbar.make(getCurrentFocus(), "NETWORK UNAVAILABLE", Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            snackbar.setBackgroundTint(Color.parseColor("#93999C"));
            snackbar.setTextColor(Color.parseColor("#222D32"));
            snackbar.setActionTextColor(Color.parseColor("#171F24"));
            snackbar.show();
        }


        rv_groupMembersList=findViewById(R.id.rv_groupMembersList);
        iv_deleteGroupHolder=findViewById(R.id.iv_deleteGroupHolder);

        memberList=new ArrayList<>();

        auth=FirebaseAuth.getInstance();
        memberList.clear();

    }

    private boolean hasSpecialCharacters(String groupname) {
        for(int i=0;i<groupname.length();i++)
        {
            if(!(groupname.charAt(i)>=48&&groupname.charAt(i)<=57||groupname.charAt(i)>=97&&groupname.charAt(i)<=122||groupname.charAt(i)>=65&&groupname.charAt(i)<=90 ||groupname.charAt(i)==' '))
            {
                return true;
            }
        }
        return false;
    }

}