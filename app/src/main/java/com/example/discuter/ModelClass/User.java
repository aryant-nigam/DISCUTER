package com.example.discuter.ModelClass;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {
    private String username;
    private String email;
    private String Uid;
    private String profileLink;
    private String status;
    private ArrayList<String>groups;
    private String designation;
    private Boolean fingerprint_auth_enabled;

    public User(){}

    public User(String username, String email, String Uid, String profileLink, String status,ArrayList<String>groups,String designation,Boolean fingerprint_auth_enabled) {
        this.username = username;
        this.email = email;
        this.Uid = Uid;
        this.profileLink = profileLink;
        this.status = status;
        this.groups=groups;
        this.designation=designation;
        this.fingerprint_auth_enabled=fingerprint_auth_enabled;
    }
    public void initialiseGroups()
    {
        this.groups=new ArrayList<>();
    }
    //- - - - - - - - - - - - - - - - - - - - GETTERS - - - - - - - - - - - - - - - - - - - -

    public String getUsername() { return username; }

    public String getEmail() { return email; }

    public String getUid() { return Uid; }

    public String getProfileLink() { return profileLink; }

    public String getStatus() { return status; }

    public ArrayList<String> getGroups() { return groups; }

    public String getDesignation() { return designation; }

    public Boolean getFingerprint_auth_enabled() { return fingerprint_auth_enabled; }


    //- - - - - - - - - - - - - - - - - - - - SETTERS - - - - - - - - - - - - - - - - - - - -

    public void setUsername(String username) { this.username = username; }

    public void setEmail(String email) { this.email = email; }

    public void setUid(String uid) { this.Uid = uid; }

    public void setProfileLink(String profileLink) { this.profileLink = profileLink; }

    public void setStatus(String status) { this.status = status; }

    public void setGroups(ArrayList<String> groups) { this.groups = groups; }

    public void  addGroups(String newGroupId) { groups.add(newGroupId) ;}

    public void setDesignation(String designation) { this.designation = designation; }

    public void setFingerprint_auth_enabled(Boolean fingerprint_auth_enabled) { this.fingerprint_auth_enabled = fingerprint_auth_enabled; }


    public int removeGroup(String groupId){
        if(groups!=null)
        {
            for(int i=0;i<groups.size();i++)
            {
                if(groups.get(i).equals(groupId)) {
                    groups.remove(i);
                    return i;
                }
            }
            return groups.size();
        }
        else
        {
            return -1;
        }
    }


}
