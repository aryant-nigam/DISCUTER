package com.example.discuter.ModelClass;

import java.io.Serializable;
import java.util.ArrayList;

public class Groups implements Serializable {
    String groupName;
    String groupDescription;
    String groupProfile;
    ArrayList<User> groupMembers;
    String groupUid;
    String AdminUid;

    public Groups() { }

    public Groups(String groupName, String groupDescription, String groupProfile, ArrayList<User> groupMembers, String groupUid, String AdminUid) {
        this.groupName = groupName;
        this.groupDescription = groupDescription;
        this.groupProfile = groupProfile;
        this.groupMembers = groupMembers;
        this.groupUid=groupUid;
        this.AdminUid=AdminUid;
    }

    //---------------------------------------GETTERS----------------------------------------
    public String getGroupName() { return groupName; }

    public String getGroupDescription() { return groupDescription; }

    public String getGroupProfile() { return groupProfile; }

    public ArrayList<User> getGroupMembers() { return groupMembers; }

    public String getGroupUid() { return groupUid; }

    public String getAdminUid() { return AdminUid; }



    //--------------------------------------SETTERS------------------------------------------

    public void setGroupName(String groupName) { this.groupName = groupName; }

    public void setGroupDescription(String groupDescription) { this.groupDescription = groupDescription; }

    public void setGroupProfile(String groupProfile) { this.groupProfile = groupProfile; }

    public void setGroupMembers(ArrayList<User> groupMembers) { this.groupMembers = groupMembers; }

    public void setGroupUid(String groupUid) { this.groupUid = groupUid; }

    public void setAdminUid(String adminUid) { AdminUid = adminUid; }

    public void addUser(User u)
    {
        groupMembers.add(u);
    }

}
