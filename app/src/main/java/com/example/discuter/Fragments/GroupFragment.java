package com.example.discuter.Fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.discuter.Adapters.GroupAdapter;
import com.example.discuter.ModelClass.Groups;
import com.example.discuter.ModelClass.User;
import com.example.discuter.R;
import com.example.discuter.UtilityClasses.CheckNetworkAvailibility;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupFragment extends Fragment {

    private RecyclerView rv_myGroups;
    private View GroupView;
    private FirebaseAuth auth;
    private User currentUser;
    private ArrayList<Groups> groupsList;
    private GroupAdapter groupAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        GroupView = inflater.inflate(R.layout.fragment_group, container, false);
        return GroupView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialise();

        rv_myGroups.setLayoutManager(new LinearLayoutManager(GroupView.getContext()));
        groupAdapter=new GroupAdapter(GroupView.getContext(), groupsList);
        rv_myGroups.setAdapter(groupAdapter);



    }

    private void initialise() {
        rv_myGroups=GroupView.findViewById(R.id.rv_myGroups);
        groupsList =new ArrayList<>();

        auth=FirebaseAuth.getInstance();
        if(auth.getUid()!=null)
        {
            DatabaseReference USER = FirebaseDatabase.getInstance().getReference().child("User").child(auth.getUid());
            USER.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    currentUser = snapshot.getValue(User.class);
                    groupsList.clear();
                    if (currentUser.getGroups() != null) {
                        for (int i = 0; i < currentUser.getGroups().size(); i++) {
                            DatabaseReference GROUPS = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentUser.getGroups().get(i));
                            GROUPS.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Groups g = snapshot.getValue(Groups.class);
                                    try {
                                        int ispresent = findGroup(groupsList, g.getGroupUid());
                                        if (ispresent != -1) {
                                            groupsList.set(ispresent, g);
                                        } else {
                                            groupsList.add(g);
                                        }
                                        groupAdapter.notifyDataSetChanged();
                                    } catch (Exception e) {
                                        Log.d("TAG", e.getMessage());
                                    } finally {

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    private int findGroup(ArrayList<Groups> groupsList,String gid) {
        for(int i=0;i<groupsList.size();i++){
            if(groupsList.get(i).getGroupUid().equals(gid))
                return i;
        }
        return -1;
    }
}

