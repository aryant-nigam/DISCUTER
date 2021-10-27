package com.example.discuter.Adapters;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.devlomi.circularstatusview.CircularStatusView;
import com.example.discuter.Activities.ActivityTab;
import com.example.discuter.Fragments.StatusFragment;
import com.example.discuter.ModelClass.Status;
import com.example.discuter.ModelClass.UserStatus;
import com.example.discuter.R;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;
import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.Viewholder> {

    private ArrayList<UserStatus>status;
    private Context context;

    public StatusAdapter(ArrayList<UserStatus>status, Context context) {
        this.status = status;
        this.context = context;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.status_layout,parent,false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        UserStatus userStatus=status.get(position);
        holder.tv_statusOf.setText(userStatus.getName());
        holder.tv_status_updateTime.setText(formatTime(userStatus.getLastUpdatedTime()));
        holder.circularStatusView.setPortionsCount(userStatus.getMyStatusList().size());
        Picasso.get().load(userStatus.getProfileImage()).placeholder(R.drawable.default_profile).into(holder.civ_status_profile);

        holder.circularStatusView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                ArrayList<MyStory> stories=new ArrayList<>();
                userStatus.filter();
                for(Status status:userStatus.getMyStatusList())
                {
                       stories.add(new MyStory(status.getImageUrl()));
                }
                if(!stories.isEmpty())
                {
                    new StoryView.Builder(((ActivityTab) context).getSupportFragmentManager())
                            .setStoriesList(stories) // Required
                            .setStoryDuration(5000) // Default is 2000 Millis (2 Seconds)
                            .setTitleText(userStatus.getName()) // Default is Hidden
                            // Default is Hidden
                            .setTitleLogoUrl(userStatus.getProfileImage())
                            .setStoryClickListeners(new StoryClickListeners() {
                                @Override
                                public void onDescriptionClickListener(int position) {
                                    //your action
                                }

                                @Override
                                public void onTitleIconClickListener(int position) {
                                    //your action
                                }
                            }) // Optional Listeners
                            .build() // Must be called before calling show method
                            .show();
                }

            }
        });
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


    @Override
    public int getItemCount() {
        return status.size();
    }

    class Viewholder extends RecyclerView.ViewHolder {
        TextView tv_statusOf,tv_status_updateTime;
        CircularStatusView circularStatusView;
        CircleImageView civ_status_profile;
        public Viewholder(@NonNull View itemView) {
            super(itemView);
            tv_statusOf=itemView.findViewById(R.id.tv_status_of);
            tv_status_updateTime=itemView.findViewById(R.id.tv_status_updateTime);
            circularStatusView=itemView.findViewById(R.id.circular_status_view);
            civ_status_profile=itemView.findViewById(R.id.civ_status_profile);
        }
    }
}
