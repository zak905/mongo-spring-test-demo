package com.gwidgets.mongotest;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Transaction {

    @Id
    String id;

    @Indexed
    boolean success;

    @Indexed
    String userId;

    @Indexed
    long created;

    public Transaction() {
    }

    public Transaction(String id, boolean success, String userId, long created) {
        this.id = id;
        this.success = success;
        this.userId = userId;
        this.created = created;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }
}
