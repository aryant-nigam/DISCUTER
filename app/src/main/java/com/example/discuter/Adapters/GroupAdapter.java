package com.example.discuter.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.discuter.Activities.GroupChatActivity;
import com.example.discuter.ModelClass.Groups;
import com.example.discuter.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.Viewholder> {
    private Context context;
    private ArrayList<Groups> groupsList;
    public GroupAdapter(Context context, ArrayList<Groups> groupsList) {
        this.context = context;
        this.groupsList = groupsList;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view= LayoutInflater.from(context).inflate(R.layout.groupmember_row_item,parent,false);
       return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        Groups groups = groupsList.get(position);
            holder.tv_groupName.setText(groups.getGroupName());
            holder.tv_groupDescription.setText(groups.getGroupDescription());

            Picasso.get().load(groups.getGroupProfile()).into(holder.cv_groupProfile);

            holder.cv_groupProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dialog profileDialog = new Dialog(context, R.style.dialogStyle);
                    profileDialog.setContentView(R.layout.show_profile_dialog);
                    profileDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                    ImageView back = profileDialog.findViewById(R.id.backButton);
                    ImageView profile = profileDialog.findViewById(R.id.userProfileImage);
                    TextView uname = profileDialog.findViewById(R.id.usernameShowProfileActivity);

                    Picasso.get().load(groups.getGroupProfile()).into(profile);
                    uname.setText(groups.getGroupName());

                    Animation popup = AnimationUtils.loadAnimation(context, R.anim.popup);

                    back.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            profileDialog.dismiss();
                        }
                    });

                    profileDialog.show();
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, GroupChatActivity.class);
                    intent.putExtra("GroupData", groups);
                    context.startActivity(intent);
                }
            });

    }

    @Override
    public int getItemCount() {
        return groupsList.size();
    }
    class Viewholder extends RecyclerView.ViewHolder {
        CircleImageView cv_groupProfile;
        TextView tv_groupName ,tv_groupDescription;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            cv_groupProfile=itemView.findViewById(R.id.civ_profile_groupmember_row_item);
            tv_groupName=itemView.findViewById(R.id.tv_username_groupmember_row_item);
            tv_groupDescription=itemView.findViewById(R.id.tv_status_groupmember_row_item);
        }
    }
}
