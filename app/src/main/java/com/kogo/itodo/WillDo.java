package com.kogo.itodo;

import com.google.firebase.auth.FirebaseUser;

import java.util.Date;

public class WillDo {
    private String id;
    private String willDoText;
    private String createdDate;
    private String deadLine;




    public WillDo(String id,String willDoText, String createdDate, String deadLine) {
        this.id = id;
        this.willDoText = willDoText;
        this.createdDate = createdDate;
        this.deadLine = deadLine;
    }

    public WillDo() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }



    public String getWillDoText() {
        return willDoText;
    }

    public void setWillDoText(String willDoText) {
        this.willDoText = willDoText;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getDeadLine() {
        return deadLine;
    }

    public void setDeadLine(String deadLine) {
        this.deadLine = deadLine;
    }


}
