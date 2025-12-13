package com.drawings.drawings.service;

import com.drawings.drawings.dao.*;
import com.drawings.drawings.dao.user_dao;
import com.drawings.drawings.model.draw;
import com.drawings.drawings.model.draw_data;
import com.drawings.drawings.model.version;
import com.drawings.drawings.records.gallery_record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class gallery_service {

    @Autowired
    draw_dao draw_dao;
    @Autowired
    permission_service permission_service;
    @Autowired
    user_dao user_dao;
    @Autowired
    data_version_dao data_version_dao;

    public List<gallery_record> select_owners_draw_details(int user_id){

        List<draw> draws = draw_dao.select_viewable_draws(user_id);
        List<gallery_record> gallery_items = new ArrayList<>();

        for (draw d : draws) {
            String author = user_dao.select_autor_by_id(d.getUser_id());

            version latest_version = data_version_dao.select_latest_draw_version(d.getId());

            int version_number = 0;
            String draw_content = "";

            if (latest_version != null) {
                version_number = latest_version.getVersion_number();
                draw_data data = data_version_dao.select_draw_data(latest_version.getId());

                if (data != null && data.getDraw_content() != null) {
                    draw_content = data.getDraw_content();
                }
            }

            boolean is_owner = d.getUser_id() == user_id;
            boolean can_edit = is_owner;

            if (!is_owner) {
                can_edit = permission_service.can_user_write(d.getId(), user_id);
            }

            gallery_record item = new gallery_record(
                    d.getId(),
                    d.getTitle(),
                    author,
                    d.getCreated_at(),
                    d.isPublic(),
                    version_number,
                    draw_content,
                    can_edit,
                    d.getUser_id()
            );

            gallery_items.add(item);
        }

        return gallery_items;
    }
}