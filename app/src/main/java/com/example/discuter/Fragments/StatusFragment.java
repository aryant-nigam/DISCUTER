package com.example.discuter.Fragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.devlomi.circularstatusview.CircularStatusView;
import com.example.discuter.Activities.ActivityTab;
import com.example.discuter.Adapters.StatusAdapter;
import com.example.discuter.ModelClass.Status;
import com.example.discuter.ModelClass.User;
import com.example.discuter.ModelClass.UserStatus;
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
import com.squareup.picasso.Picasso;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;


public class StatusFragment extends Fragment {

    private View StatusView;
    private CircularStatusView circular_status_view_mystatus;
    private TextView tv_lastUpdated;
    private CircleImageView civ_addStatus,myStatus_PlaceHolder;
    private FirebaseAuth auth;
    private Uri statusImageURI;
    private Dialog ProgressDialog;
    private User currUser;
    private ArrayList<UserStatus>statusList;
    private StatusAdapter statusAdapter;
    private RecyclerView rv_userStatuses;
    private UserStatus myStatus;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        StatusView=inflater.inflate(R.layout.fragment_status, container, false);
        return StatusView;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialise();
        fetchStatuses();

        civ_addStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imageIntent=new Intent();
                imageIntent.setType("image/*");
                imageIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(imageIntent,"Pick your Profile Image"),10);
            }
        });

    }

    private void fetchStatuses() {

        DatabaseReference STATUS=FirebaseDatabase.getInstance().getReference().child("Statuses");
        STATUS.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                statusList.clear();
                for(DataSnapshot s:snapshot.getChildren())
                {
                    UserStatus userStatus=s.getValue(UserStatus.class);
                    userStatus.filter();
                    if(!userStatus.getUid().equals(currUser.getUid())&& !userStatus.getMyStatusList().isEmpty()) {
                        statusList.add(userStatus);
                    }
                    else {
                        myStatus=userStatus;
                        fetchMyStatus();
                    }

                }
                statusAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(StatusView.getContext());
        rv_userStatuses.setLayoutManager(linearLayoutManager);
        statusAdapter=new StatusAdapter(statusList,StatusView.getContext());
        rv_userStatuses.setAdapter(statusAdapter);

    }

    private void fetchMyStatus() {
        DatabaseReference STATUS=FirebaseDatabase.getInstance().getReference().child("Statuses").child(auth.getUid()).child("myStatusList");
        STATUS.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tv_lastUpdated.setText(formatTime(myStatus.getLastUpdatedTime()));
               // Toast.makeText(StatusView.getContext(), ""+myStatus.getMyStatusList().size(), Toast.LENGTH_SHORT).show();
                circular_status_view_mystatus.setPortionsCount(myStatus.getMyStatusList().size());
                circular_status_view_mystatus.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onClick(View v) {

                        ArrayList<MyStory> stories=new ArrayList<>();
                        myStatus.filter();
                        for(Status status:myStatus.getMyStatusList())
                        {
                            stories.add(new MyStory(status.getImageUrl()));
                        }

                        if(!stories.isEmpty())
                        {
                            new StoryView.Builder(((ActivityTab) StatusView.getContext()).getSupportFragmentManager())
                                    .setStoriesList(stories) // Required
                                    .setStoryDuration(5000) // Default is 2000 Millis (2 Seconds)
                                    .setTitleText(myStatus.getName()) // Default is Hidden
                                    .setTitleLogoUrl(myStatus.getProfileImage())
                                    .setStoryClickListeners(new StoryClickListeners() {
                                        @Override
                                        public void onDescriptionClickListener(int position) {
                                        }

                                        @Override
                                        public void onTitleIconClickListener(int position) {
                                        }
                                    })
                                    .build()
                                    .show();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==10)
        {
            if(data!=null)
            {
                ProgressDialog.show();
                statusImageURI=data.getData();

                long timestamp=Calendar.getInstance().getTimeInMillis();
                ZoneId zid=ZoneId.of("Asia/Kolkata");
                LocalDateTime localDateTime=LocalDateTime.now(zid);
                LocalTime time=LocalTime.now(zid);
                LocalDate date=LocalDate.now(zid);

                DatabaseReference STATUS=FirebaseDatabase.getInstance().getReference().child("Statuses").child(currUser.getUid());
                StorageReference IMAGE= FirebaseStorage.getInstance().getReference().child("Statuses").child(currUser.getUid()).child(String.valueOf(timestamp));
                IMAGE.putFile(data.getData()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            IMAGE.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Status status=new Status(
                                            uri.toString(),
                                            localDateTime.toString()
                                    );
                                    STATUS.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.exists()) {
                                                UserStatus userStatus = snapshot.getValue(UserStatus.class);
                                                userStatus.appendToMyStatusList(status);
                                                userStatus.setLastUpdatedTime(time.toString());
                                                STATUS.setValue(userStatus);
                                                //updateStatus(1,userStatus);
                                            }
                                            else if(!snapshot.exists())
                                            {
                                                ArrayList<Status> statuses=new ArrayList<>();
                                                statuses.add(status);
                                                UserStatus userStatus=new UserStatus(
                                                        currUser.getUid(),
                                                        currUser.getUsername(),
                                                        currUser.getProfileLink(),
                                                        date.toString(),
                                                        time.toString(),
                                                        null
                                                );
                                                userStatus.initMyStatusList();
                                                userStatus.setMyStatusList(statuses);
                                                STATUS.setValue(userStatus);
                                                //updateStatus(0,userStatus);
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(StatusView.getContext(), "Failed to upload Mood", Toast.LENGTH_LONG).show();
                                }
                            });
                            ProgressDialog.dismiss();
                            Toast.makeText(StatusView.getContext(), "Succesfully updated Mood", Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            Toast.makeText(StatusView.getContext(), "Failed to upload Mood", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }
    }

    private void updateStatus(int i,UserStatus userStatus) {

        DatabaseReference STATUS=FirebaseDatabase.getInstance().getReference().child("Statuses").child(currUser.getUid());
        if(i==1)
        {
            STATUS.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot s:snapshot.getChildren())
                    {
                        STATUS.child(s.getKey()).setValue(userStatus).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(StatusView.getContext(), "Succesfully updated Mood", Toast.LENGTH_LONG).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(StatusView.getContext(), "Failed to upload Mood", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d("TAG", error.toString());
                }
            });
        }
        else if(i==0)
        {
            STATUS.push().setValue(userStatus).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(StatusView.getContext(), "Succesfully updated Mood", Toast.LENGTH_LONG).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(StatusView.getContext(), "Failed to update status", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void initialise() {
        circular_status_view_mystatus=StatusView.findViewById(R.id.circular_status_view_mystatus);
        tv_lastUpdated=StatusView.findViewById(R.id.tv_lastUpdated);
        civ_addStatus=StatusView.findViewById(R.id.civ_addStatus);
        myStatus_PlaceHolder=StatusView.findViewById(R.id.myStatus_PlaceHolder);
        rv_userStatuses=StatusView.findViewById(R.id.rv_userStatuses);
        auth=FirebaseAuth.getInstance();
        statusList=new ArrayList<>();
        DatabaseReference USER = FirebaseDatabase.getInstance().getReference().child("User").child(auth.getUid());
        USER.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currUser=snapshot.getValue(User.class);
                Picasso.get().load(currUser.getProfileLink()).into(myStatus_PlaceHolder);
                //Toast.makeText(StatusView.getContext(), currUser.getUsername(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        ProgressDialog=new Dialog(StatusView.getContext());
        ProgressDialog.setContentView(R.layout.sending_image_dialog);
        ProgressDialog.setCancelable(false);
        ProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView attachmentMessage=ProgressDialog.findViewById(R.id.sendAttachmentMessage);
        attachmentMessage.setText("Sending Status Update");

    }

    private String formatTime(String lastUpdatedTime) {
        if(Integer.parseInt(lastUpdatedTime.substring(0,2))>12)
        {
            return "Last Updated "+ (Integer.parseInt(lastUpdatedTime.substring(0,2))-12)+ lastUpdatedTime.substring(2,5)+" PM";
        }
        else
        {
            if(Integer.parseInt(lastUpdatedTime.substring(0,2))==0)
            {
                return "Last Updated "+ "12" + lastUpdatedTime.substring(2,5)+" AM";
            }
            return lastUpdatedTime.substring(0,5)+" AM";
        }
    }
}