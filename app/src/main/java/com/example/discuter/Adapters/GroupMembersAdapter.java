package com.example.discuter.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.discuter.ModelClass.Groups;
import com.example.discuter.ModelClass.User;
import com.example.discuter.R;
import com.example.discuter.UtilityClasses.CheckNetworkAvailibility;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupMembersAdapter extends RecyclerView.Adapter<GroupMembersAdapter.Viewholder> {
    private Context context;
    private ArrayList<User> groupMembersList;
    private Groups currentGroup;
    private FirebaseAuth auth;

    public GroupMembersAdapter(Context context, ArrayList<User> groupMembersList,Groups currentGroup) {
        this.context = context;
        this.groupMembersList = groupMembersList;
        this.currentGroup=currentGroup;
        this.auth=FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.group_member_item_layout,parent,false);
       return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        User member=groupMembersList.get(position);

        holder.tv_groupMemberName.setText(member.getUsername());
        holder.tv_groupDesignation.setText(member.getDesignation());
        Picasso.get().load(member.getProfileLink()).into(holder.civ_groupMemberProfile);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (holder.iv_isSelectedForDeletion.getVisibility()==View.GONE  &&  ! member.getUid().equals(currentGroup.getAdminUid()))
                    {
                        holder.iv_isSelectedForDeletion.setVisibility(View.VISIBLE);
                        Dialog dialog = new Dialog(context);
                        dialog.setContentView(R.layout.logout_dialog);
                        dialog.setCancelable(false);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                        TextView message = dialog.findViewById(R.id.tv_logout_message);
                        TextView Yes = dialog.findViewById(R.id.ok2);
                        TextView No = dialog.findViewById(R.id.no);


                        message.setText("Are you sure you want to remove " + member.getUsername() + " from this group ?");
                        Yes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(auth.getUid().equals(currentGroup.getAdminUid())) {
                                    if (CheckNetworkAvailibility.isConnectionAvailable(context)) {
                                        member.removeGroup(currentGroup.getGroupUid());
                                        FirebaseDatabase.getInstance().getReference().child("User").child(member.getUid()).setValue(member)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        for (int i = 0; i < currentGroup.getGroupMembers().size(); i++) {
                                                            if (currentGroup.getGroupMembers().get(i).getUid().equals(member.getUid()))
                                                                currentGroup.getGroupMembers().remove(i);
                                                        }
                                                        FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroup.getGroupUid()).setValue(currentGroup)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {
                                                                        dialog.dismiss();
                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                //Log.d("TAG", e.getMessage());
                                                            }
                                                        });
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //Log.d("TAG", e.getMessage());
                                            }
                                        });
                                    }
                                    else {
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
                                else
                                {
                                    Toast.makeText(context, "Only moderator can remove the members", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        No.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                holder.iv_isSelectedForDeletion.setVisibility(View.GONE);
                            }
                        });
                        dialog.show();
                    }
                    else if(member.getUid().equals(currentGroup.getAdminUid())){
                        Toast.makeText(context, "You cant remove the moderator", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        holder.iv_isSelectedForDeletion.setVisibility(View.GONE);
                    }

                    return false;
                }
            });

    }



    @Override
    public int getItemCount() {
        return groupMembersList.size();
    }

    class Viewholder extends RecyclerView.ViewHolder {
        CircleImageView civ_groupMemberProfile;
        TextView tv_groupMemberName;
        TextView tv_groupDesignation;
        ImageView iv_isSelectedForDeletion;
        public Viewholder(@NonNull View itemView) {
            super(itemView);
            civ_groupMemberProfile=itemView.findViewById(R.id.civ_groupMemberProfile);
            tv_groupMemberName=itemView.findViewById(R.id.tv_groupMemberName);
            tv_groupDesignation=itemView.findViewById(R.id.tv_groupDesignation);
            iv_isSelectedForDeletion=itemView.findViewById(R.id.iv_isSelectedForDeletion);
        }
    }
}
