package com.drawings.drawings.dto;

public class draw_dto {
    private String title;
    private boolean ispublic;
    private String drawContent;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isIspublic() {
        return ispublic;
    }

    public void setIspublic(boolean ispublic) {
        this.ispublic = ispublic;
    }

    public String getDrawContent() {
        return drawContent;
    }

    public void setDrawContent(String drawContent) {
        this.drawContent = drawContent;
    }

}
