package com.drawings.drawings.model;


import java.sql.Timestamp;

public class draw {
    private int id;
    private int user_id;
    private String title;
    private Timestamp created_at;

    public draw(int id, int user_id, String title, Timestamp created_at) {
        this.id = id;
        this.user_id = user_id;
        this.title = title;
        this.created_at = created_at;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Timestamp getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }
}
