package com.drawings.drawings.model;

public class permissions {
    private int user_id;
    private int draw_id;
    private boolean can_read;
    private boolean can_write;

    public permissions(int user_id, int draw_id, boolean can_read, boolean can_write) {
        this.user_id = user_id;
        this.draw_id = draw_id;
        this.can_read = can_read;
        this.can_write = can_write;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getDraw_id() {
        return draw_id;
    }

    public void setDraw_id(int draw_id) {
        this.draw_id = draw_id;
    }

    public boolean isCan_read() {
        return can_read;
    }

    public void setCan_read(boolean can_read) {
        this.can_read = can_read;
    }

    public boolean isCan_write() {
        return can_write;
    }

    public void setCan_write(boolean can_write) {
        this.can_write = can_write;
    }
}
