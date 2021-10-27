package com.example.discuter.Fragments;

import android.Manifest;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.discuter.Adapters.UserAdapter;
import com.example.discuter.ModelClass.User;
import com.example.discuter.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChatFragment extends Fragment {

    private View ChatView;
    private RecyclerView chatAreaUsers;

    private FirebaseAuth authenticate;
    private UserAdapter adapter;
    private ArrayList<User> usersList;
    private LinearLayout skeletonUi;
    private User currentUser=new User();
    private Dialog profileDialog;
    public static String[] permissions=new String[]{Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};
    public static int requestCode = 1;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ChatView= inflater.inflate(R.layout.fragment_chat, container, false);
        return ChatView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialise();
        showSkeletonUI();


        authenticate=FirebaseAuth.getInstance();

        DatabaseReference USERS= FirebaseDatabase.getInstance().getReference().child("User");
        usersList=new ArrayList<>();

        USERS.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for(DataSnapshot u:snapshot.getChildren())
                {
                    User user=u.getValue(User.class);
                    if(!authenticate.getUid().equals(user.getUid())&&!user.getProfileLink().equals("")) {
                        usersList.add(user);
                        //recieverKeys.add(snapshot.getKey());
                    }
                    else {
                        currentUser = u.getValue(User.class);
                    }
                    adapter.notifyDataSetChanged();
                }

            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });

        chatAreaUsers.setLayoutManager(new LinearLayoutManager(ChatView.getContext()));
        adapter=new UserAdapter(ChatView.getContext(),usersList);
        chatAreaUsers.setAdapter(adapter);

    }


    private void initialise() {
        skeletonUi=ChatView.findViewById(R.id.skeletonUi);
        chatAreaUsers=ChatView.findViewById(R.id.chatAreaUsers);
        chatAreaUsers.setVisibility(View.GONE);
    }

    private void showSkeletonUI()
    {
        new CountDownTimer(3000, 3000) {

            public void onTick(long millisUntilFinished) { }

            public void onFinish() {
                chatAreaUsers.setVisibility(View.VISIBLE);
                skeletonUi.setVisibility(View.GONE);
            }
        }.start();
    }


}