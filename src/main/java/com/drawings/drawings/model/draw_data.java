package com.drawings.drawings.model;

public class draw_data {
    private int version_id;
    private String draw_content;

    public draw_data(int version_id, String draw_content) {
        this.version_id = version_id;
        this.draw_content = draw_content;
    }


    public int getVersion_id() {
        return version_id;
    }

    public void setVersion_id(int version_id) {
        this.version_id = version_id;
    }

    public String getDraw_content() {
        return draw_content;
    }

    public void setDraw_content(String draw_content) {
        this.draw_content = draw_content;
    }
}
