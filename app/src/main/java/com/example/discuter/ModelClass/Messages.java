package com.example.discuter.ModelClass;

import java.io.Serializable;

public class Messages implements Serializable {
    private String message;
    private String senderId;
    private Long timeStamp;
    private String imageMessageLink;
    private Boolean messageViewStatus;
    private String senderProfileLink;
    private String senderName;
    private int reaction;

    public Messages(){}

    public Messages(String message, String senderId, Long timeStamp,String imageMessageLink,Boolean messageViewStatus,String senderProfileLink,String senderName,int reaction) {
        this.message = message;
        this.senderId = senderId;
        this.timeStamp = timeStamp;
        this.imageMessageLink = imageMessageLink;
        this.messageViewStatus=messageViewStatus;
        this.senderProfileLink=senderProfileLink;
        this.senderName=senderName;
        this.reaction=reaction;

    }

    //--------------------------------GETTER---------------------------------

    public String getMessage() { return message; }

    public String getSenderId() { return senderId; }

    public Long getTimeStamp() { return timeStamp; }

    public String getImageMessageLink() { return imageMessageLink; }

    public Boolean getMessageViewStatus() { return messageViewStatus; }

    public String getSenderProfileLink() { return senderProfileLink; }

    public String getSenderName() { return senderName; }

    public int getReaction() { return reaction; }

    //-------------------------------SETTER----------------------------------

    public void setMessage(String message) { this.message = message; }

    public void setTimeStamp(Long timeStamp) { this.timeStamp = timeStamp; }

    public void setSenderId(String senderId) { this.senderId = senderId; }

    public void setImageMessageLink(String imageMessageLink) { this.imageMessageLink = imageMessageLink; }

    public void setMessageViewStatus(Boolean messageViewStatus) { this.messageViewStatus = messageViewStatus; }

    public void setSenderProfileLink(String senderProfileLink) { this.senderProfileLink = senderProfileLink; }

    public void setSenderName(String senderName) { this.senderName = senderName; }

    public void setReaction(int reaction) { this.reaction = reaction; }
}
