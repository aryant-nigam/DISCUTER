package com.example.discuter.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.discuter.Adapters.SelectGroupMemberAdapter;
import com.example.discuter.ModelClass.Groups;
import com.example.discuter.ModelClass.User;
import com.example.discuter.ModelClass.UserSelected;
import com.example.discuter.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SelectGroupMembers extends AppCompatActivity {
    public static ArrayList<User>membersList;
    private ArrayList<UserSelected>usersList;
    public static ArrayList<UserSelected>usersListAll;
    private FirebaseAuth auth;
    private User currentUser;
    private RecyclerView rv_selectMemberList;
    private SelectGroupMemberAdapter adapter;
    private ImageView iv_doneSelectingMembers;
    private Groups groups;
    private SearchView sv_SearchGroupMembers;
    private EditText et_searchBox;
    private Uri groupProfileUri;
    private ProgressBar load;
    private String intentFrom;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_group_members);

        initialise();



        iv_doneSelectingMembers.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if(groupProfileUri!=null &&intentFrom.equals("CGA"))
               {
                   load.setVisibility(View.VISIBLE);
                   StorageReference GROUPPROFILE = FirebaseStorage.getInstance().getReference().child("Group Profile Images").child(groups.getGroupUid());
                   GROUPPROFILE.putFile(groupProfileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                       @Override
                       public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                           if (task.isSuccessful()) {
                               GROUPPROFILE.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                   @Override
                                   public void onSuccess(Uri uri) {
                                       groups.setGroupProfile(uri.toString());
                                       createNewGroup();
                                   }
                               }).addOnFailureListener(new OnFailureListener() {
                                   @Override
                                   public void onFailure(@NonNull Exception e) {
                                       load.setVisibility(View.GONE);
                                       Toast.makeText(getApplicationContext(), "Failed to upload profile", Toast.LENGTH_SHORT).show();
                                   }
                               });
                           } else {
                               load.setVisibility(View.GONE);
                               Toast.makeText(getApplicationContext(), "Failed to upload profile", Toast.LENGTH_SHORT).show();
                           }
                       }
                   });

               }
               else
               {
                   load.setVisibility(View.VISIBLE);
                   createNewGroup();
               }
           }
       });

        DatabaseReference USERS= FirebaseDatabase.getInstance().getReference().child("User");
        USERS.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                usersListAll.clear();
                for(DataSnapshot u:snapshot.getChildren())
                {
                    User user=u.getValue(User.class);
                    if(!auth.getUid().equals(user.getUid())&&!user.getProfileLink().equals("")) {
                        usersListAll.add(new UserSelected(false,user));
                        usersList.add(new UserSelected(false,user));
                    }
                    else {
                        currentUser = u.getValue(User.class);
                        groups.setAdminUid(currentUser.getUid());
                        membersList.add(currentUser);
                        membersList.get(0).setDesignation("Moderator");
                    }
                    adapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        rv_selectMemberList.setLayoutManager(new LinearLayoutManager(this));
        adapter=new SelectGroupMemberAdapter(getApplicationContext(),usersList);
        rv_selectMemberList.setAdapter(adapter);

        et_searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

            @Override
            public void afterTextChanged(Editable s) {
                usersList.clear();
                    for(int i=0;i<usersListAll.size();i++)
                    {
                        if(usersListAll.get(i).getUser().getUsername().toLowerCase().contains(s.toString().toLowerCase()))
                            usersList.add(usersListAll.get(i));
                    }
                adapter.notifyDataSetChanged();

            }
        });

    }

    private void createNewGroup() {
        if(intentFrom.equals("CGA"))
        {
            DatabaseReference GROUPS = FirebaseDatabase.getInstance().getReference().child("Groups").child(groups.getGroupUid());
            for (int i = 1; i < membersList.size(); i++)
                membersList.get(i).setDesignation("Group Member");
            groups.setGroupMembers(membersList);
            GROUPS.setValue(groups).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    associateUsersToGroup();
                    Toast.makeText(getApplicationContext(), "Congratulations ! Group created", Toast.LENGTH_SHORT).show();
                    load.setVisibility(View.GONE);
                    Intent intent = new Intent(SelectGroupMembers.this, ActivityTab.class);
                    startActivity(intent);
                    DatabaseReference GROUPS = FirebaseDatabase.getInstance().getReference().child("Groups").child(groups.getGroupUid());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    load.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Failed to create group", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else
        {
           for (int i = 1; i < membersList.size(); i++)
                membersList.get(i).setDesignation("Group Member");

            Set<String>newIds=new HashSet<>();
            Set<String>oldIds=new HashSet<>();
            Map<String,User> m=new HashMap<>();
            for(int i=0;i<membersList.size();i++) {
                m.put(membersList.get(i).getUid(),membersList.get(i));
                newIds.add(membersList.get(i).getUid());
            }
            for(int i=0;i<groups.getGroupMembers().size();i++) {
                m.put(groups.getGroupMembers().get(i).getUid(),groups.getGroupMembers().get(i));
                oldIds.add(groups.getGroupMembers().get(i).getUid());
            }

            newIds.removeAll(oldIds);

            for(String id:newIds)
            {
                if(m.get(id).getGroups()==null)
                {
                    m.get(id).initialiseGroups();
                    m.get(id).addGroups(groups.getGroupUid());
                }
                else
                    m.get(id).addGroups(groups.getGroupUid());

                FirebaseDatabase.getInstance().getReference().child("User").child(id).setValue(m.get(id))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            }

            ArrayList<User>AllUsers=groups.getGroupMembers();
            for(String s:newIds)
            {
                AllUsers.add(m.get(s));
            }
            groups.setGroupMembers(membersList);
            FirebaseDatabase.getInstance().getReference().child("Groups").child(groups.getGroupUid())
            .setValue(groups).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    load.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Users Added Sucessfully", Toast.LENGTH_SHORT).show();
                    //Intent intent=new Intent(getApplicationContext(),UpdateGroupInfoActivity.class);
                    //intent.putExtra("GroupInfo", groups);
                    //startActivity(intent);
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
    }

    private void associateUsersToGroup() {
        for(User member:membersList)
        {
            member.setDesignation("");
            DatabaseReference USERS= FirebaseDatabase.getInstance().getReference().child("User");
            if(member.getGroups()==null)
            {
                member.initialiseGroups();
                member.addGroups(groups.getGroupUid());
            }
            else {
                member.addGroups(groups.getGroupUid());
            }
            USERS.child(member.getUid()).setValue(member).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), member.getUsername()+" skipped from being added", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void initialise() {
        auth=FirebaseAuth.getInstance();
        usersList=new ArrayList<>();
        membersList=new ArrayList<>();
        usersListAll=new ArrayList<>();

        groups =(Groups) getIntent().getSerializableExtra("GroupData");
        intentFrom=getIntent().getStringExtra("from");
        if(groups.getGroupProfile().equals("")&&intentFrom.equals("CGA"))
        {
            groupProfileUri = (Uri) getIntent().getParcelableExtra("groupProfileUri");
        }

        rv_selectMemberList=findViewById(R.id.rv_selectMemberList);
        iv_doneSelectingMembers=findViewById(R.id.iv_doneSelectingMembers);
        et_searchBox=findViewById(R.id.et_searchBox);
        load=findViewById(R.id.load6);
    }
}