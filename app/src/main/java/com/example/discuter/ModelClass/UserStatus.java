package com.example.discuter.ModelClass;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;

public class UserStatus implements Serializable {
    private String uid;
    private String name;
    private String profileImage;
    private String lastUpdatedDate;
    private String lastUpdatedTime;
    private ArrayList<Status>myStatusList;

    public UserStatus() { }


    public UserStatus(String uid,String name, String profileImage, String lastUpdatedDate,String lastUpdatedTime, ArrayList<Status> myStatusList) {
        this.uid=uid;
        this.name = name;
        this.profileImage = profileImage;
        this.lastUpdatedDate = lastUpdatedDate;
        this.lastUpdatedTime=lastUpdatedTime;
        this.myStatusList = myStatusList;
    }

    public void initMyStatusList(){
        myStatusList=new ArrayList<>();
    }

    //---------------------------------------------------GETTERS------------------------------------
    public String getUid() { return uid; }

    public String getName() { return name; }

    public String getProfileImage() { return profileImage; }

    public String getLastUpdatedDate() { return lastUpdatedDate; }

    public String getLastUpdatedTime() { return lastUpdatedTime; }

    public ArrayList<Status> getMyStatusList() { return myStatusList; }

    public Status getStatusAtIndex(int index){return myStatusList.get(index);}

    //---------------------------------------------------SETTERS------------------------------------
    public void setUid(String uid) { this.uid = uid; }

    public void setName(String name) { this.name = name; }

    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public void setLastUpdatedDate(String lastUpdatedDate) { this.lastUpdatedDate = lastUpdatedDate; }

    public void setLastUpdatedTime(String lastUpdatedTime) { this.lastUpdatedTime = lastUpdatedTime; }

    public void setMyStatusList(ArrayList<Status> myStatusList) { this.myStatusList = myStatusList; }

    public  void appendToMyStatusList(Status status){this.myStatusList.add(status);}

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void filter() {
        int i=0;
        ZoneId zid=ZoneId.of("Asia/Kolkata");
        LocalDateTime localDateTime=LocalDateTime.now(zid);
        for(Status s:myStatusList)
        {
            Duration duration = Duration.between(LocalDateTime.parse(s.getTimeStamp()),localDateTime);
            Log.d("Ary", String.valueOf(duration.getSeconds()));
            if(duration.getSeconds()>=86400l)
                myStatusList.remove(i);
            i++;
        }
    }
}


