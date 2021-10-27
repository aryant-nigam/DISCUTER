package com.example.discuter.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.discuter.Activities.SelectGroupMembers;
import com.example.discuter.ModelClass.User;
import com.example.discuter.ModelClass.UserSelected;
import com.example.discuter.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class SelectGroupMemberAdapter extends RecyclerView.Adapter<SelectGroupMemberAdapter.Viewholder>  {
    private Context context;
    private ArrayList<UserSelected>userList;
    public SelectGroupMemberAdapter(Context context, ArrayList<UserSelected> userList)
    {
        this.context = context;
        this.userList=userList;
        //this.isSelected=new ArrayList<Boolean>(Collections.nCopies(this.userList.size(),false));
    }


    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(context).inflate(R.layout.groupmember_row_item,parent,false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        User user =userList.get(position).getUser();

        holder.iv_selected.setVisibility(View.GONE);
        holder.tv_userName.setText(user.getUsername());
        holder.tv_userStatus.setText(user.getStatus());

        Picasso.get().load(user.getProfileLink()).into(holder.cv_userProfile);

        int pos=findPositionInActualList(user);
        if(SelectGroupMembers.usersListAll.get(pos).getSelection())
            holder.iv_selected.setVisibility(View.VISIBLE);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                for(int i=0;i<SelectGroupMembers.usersListAll.size();i++) {
                    if(SelectGroupMembers.usersListAll.get(i).getUser().getUid().equals(user.getUid()))
                    {
                        if(SelectGroupMembers.usersListAll.get(i).getSelection())
                        {
                             SelectGroupMembers.usersListAll.get(i).setSelection(false);
                             removeFromMembersList(user);
                             holder.iv_selected.setVisibility(View.GONE);
                             break;
                        }
                        else
                        {
                            SelectGroupMembers.usersListAll.get(i).setSelection(true);
                            SelectGroupMembers.membersList.add(user);
                            holder.iv_selected.setVisibility(View.VISIBLE);
                            break;
                        }
                    }
                }
                return false;
            }
        });


    }

    private int findPositionInActualList(User u)
    {
        int pos=0;
        for(int i=0;i<SelectGroupMembers.membersList.size();i++)
        {
            if(u.getUid().equals(SelectGroupMembers.usersListAll.get(i).getUser().getUid())) {
                pos = i;
                break;
            }
        }
        return pos;
    }
    private void removeFromMembersList(User u) {
        for(int i=0;i<SelectGroupMembers.membersList.size();i++)
        {
            if(u.getUid().equals(SelectGroupMembers.membersList.get(i).getUid()))
                SelectGroupMembers.membersList.remove(i);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class Viewholder extends RecyclerView.ViewHolder {
        CircleImageView cv_userProfile;
        TextView tv_userName ,tv_userStatus;
        ImageView iv_selected;

        public Viewholder(@NonNull View itemView) {
            super(itemView);

            cv_userProfile=itemView.findViewById(R.id.civ_profile_groupmember_row_item);
            tv_userName=itemView.findViewById(R.id.tv_username_groupmember_row_item);
            tv_userStatus=itemView.findViewById(R.id.tv_status_groupmember_row_item);
            iv_selected=itemView.findViewById(R.id.iv_selected_groupmember_row_item);
        }
    }
}
