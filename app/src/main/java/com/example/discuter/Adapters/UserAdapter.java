package com.example.discuter.Adapters;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.discuter.Activities.ChatActivity;
import com.example.discuter.Activities.VideoCallingActivity;
import com.example.discuter.Fragments.ChatFragment;
import com.example.discuter.ModelClass.User;
import com.example.discuter.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.Viewholder> {
    private Context context;
    private ArrayList<User> usersList;
    public UserAdapter(Context context, ArrayList<User> usersList)
    {
        this.context=context;
        this.usersList=usersList;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.user_row_item,parent,false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {

        User item=usersList.get(position);

        holder.username.setText(item.getUsername());
        holder.status.setText(item.getStatus());

        Picasso.get().load(item.getProfileLink()).into(holder.profile);

        holder.profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog profileDialog=new Dialog(context,R.style.dialogStyle);
                profileDialog.setContentView(R.layout.show_profile_dialog);
                profileDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                ImageView back=profileDialog.findViewById(R.id.backButton);
                ImageView profile=profileDialog.findViewById(R.id.userProfileImage);
                TextView uname=profileDialog.findViewById(R.id.usernameShowProfileActivity);

                Picasso.get().load(item.getProfileLink()).into(profile);
                uname.setText(item.getUsername());

                back.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        profileDialog.dismiss();
                    }
                });

                profileDialog.show();
            }
        });

        holder.iv_video_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(arePermissionsGranted())
                {
                    context.startActivity(new Intent(context, VideoCallingActivity.class));
                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, ChatActivity.class);
                intent.putExtra("RecieverName",item.getUsername());
                intent.putExtra("RecieverProfile",item.getProfileLink());
                intent.putExtra("RecieverUid",item.getUid());

                context.startActivity(intent);
            }
        });

    }

    private void askPermisissions() {
        ActivityCompat.requestPermissions((Activity) context,ChatFragment.permissions,ChatFragment.requestCode);
    }

    private boolean arePermissionsGranted() {
        for(String permission :ChatFragment.permissions)
        {
            if(ActivityCompat.checkSelfPermission(context,permission)!= PackageManager.PERMISSION_GRANTED) {
                askPermisissions();
            }
        }
        return true;
    }


    @Override
    public int getItemCount() {
        return usersList.size();
    }

    class Viewholder extends RecyclerView.ViewHolder {
        CircleImageView profile;
        TextView username,status;
        ImageView iv_video_call;

        public Viewholder(@NonNull View itemView) {
            super(itemView);

            profile=itemView.findViewById(R.id.profile_user_row_item);
            username=itemView.findViewById(R.id.username_user_row_item);
            status=itemView.findViewById(R.id.status_user_row_item);
            iv_video_call=itemView.findViewById(R.id.iv_video_call);
        }
    }
}
