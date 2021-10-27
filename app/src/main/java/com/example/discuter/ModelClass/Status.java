package com.example.discuter.ModelClass;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Status implements Serializable {
    private String imageUrl;
    private String timeStamp;

    public Status() { }
    public Status(String imageUrl, String timeStamp) {
        this.imageUrl = imageUrl;
        this.timeStamp = timeStamp;
    }

    public String getImageUrl() { return imageUrl; }
    public String getTimeStamp() { return timeStamp; }

    public void setTimeStamp(String timeStamp) { this.timeStamp = timeStamp; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
