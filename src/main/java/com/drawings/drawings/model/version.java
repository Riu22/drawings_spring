package com.drawings.drawings.model;

import java.sql.Timestamp;

public class version {
    private int id;
    private int version_number;
    private Timestamp created_at;
    private int draw_id;

    public version(int id, int version_number, Timestamp created_at, int draw_id) {
        this.id = id;
        this.version_number = version_number;
        this.created_at = created_at;
        this.draw_id = draw_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVersion_number() {
        return version_number;
    }

    public void setVersion_number(int version_number) {
        this.version_number = version_number;
    }

    public Timestamp getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }

    public int getDraw_id() {
        return draw_id;
    }

    public void setDraw_id(int draw_id) {
        this.draw_id = draw_id;
    }
}