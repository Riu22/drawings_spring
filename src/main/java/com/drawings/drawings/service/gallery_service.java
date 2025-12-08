package com.drawings.drawings.service;

import com.drawings.drawings.dao.draw_dao;
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

    private final draw_dao draw_dao;
    private final permission_service permission_service;

    @Autowired
    public gallery_service(draw_dao draw_dao, permission_service permission_service) {
        this.draw_dao = draw_dao;
        this.permission_service = permission_service;
    }

    public List<gallery_record> select_owners_draw_details(int userId){

        List<draw> draws = draw_dao.select_viewable_draws(userId);
        List<gallery_record> gallery_items = new ArrayList<>();

        for (draw d : draws) {

            version latest_version = draw_dao.select_latest_draw_version(d.getId());

            int version_number = 0;
            String draw_content = "";

            if (latest_version != null) {
                version_number = latest_version.getVersion_number();
                draw_data data = draw_dao.select_draw_data(latest_version.getId());

                if (data != null && data.getDraw_content() != null) {
                    draw_content = data.getDraw_content();
                }
            }

            // Cálculo de Permiso de Edición (can_edit)
            boolean isOwner = d.getUser_id() == userId;
            boolean canEdit = isOwner;

            if (!isOwner) {
                canEdit = permission_service.canUserWrite(d.getId(), userId);
            }

            gallery_record item = new gallery_record(
                    d.getId(),
                    d.getTitle(),
                    d.getCreated_at(),
                    d.isPublic(),
                    version_number,
                    draw_content,
                    canEdit,
                    d.getUser_id()  // ID del dueño del dibujo
            );

            gallery_items.add(item);
        }

        return gallery_items;
    }
}