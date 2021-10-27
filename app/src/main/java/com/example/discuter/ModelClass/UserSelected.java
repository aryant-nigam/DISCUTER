package com.example.discuter.ModelClass;

public class UserSelected {
    private Boolean isSelected;
    private User user;

    public UserSelected() { }

    public UserSelected(Boolean isSelected, User user) {
        this.isSelected = isSelected;
        this.user = user;
    }

    //--------------------------------------------GETTER-------------------------------------
    public Boolean getSelection() { return isSelected; }

    public User getUser() { return user; }

    //----------------------------------------------SETTER------------------------------------
    public void setSelection(Boolean selected) { isSelected = selected; }

    public void setUser(User user) { this.user = user; }
}
