package com.drawings.drawings.service;

import com.drawings.drawings.dao.draw_dao;
import com.drawings.drawings.dao.user_dao;
import com.drawings.drawings.model.draw;
import com.drawings.drawings.model.user;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class save_service {
    @Autowired
    private draw_dao draw_dao;
    @Autowired
    private user_dao user_dao;

    public int iduser(String name){
        user user = user_dao.find_user(name);
        return user.getId();
    }

    public draw save_draw(String title,boolean ispublic,int author,String draw_content){
        draw new_draw = new draw(author, title, ispublic);
        draw saved_draw = draw_dao.add_draw(new_draw);
        int draw_id = saved_draw.getId();
        int version_id = draw_dao.add_version(draw_id);
        draw_dao.add_draw_content(version_id,draw_content);
        return saved_draw;
    }

}
