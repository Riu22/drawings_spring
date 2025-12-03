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
    @Autowired
    draw_dao draw_dao;

    public List<gallery_record> select_owners_draw_details(int userId){

        List<draw> draws = draw_dao.select_owners_draws(userId);
        List<gallery_record> gallery_items = new ArrayList<>();

        for (draw d : draws) {
            version latest_version = draw_dao.select_latest_draw_version(d.getId());

            int version_number = 0;
            String draw_nontent = "";

            if (latest_version != null) {
                version_number = latest_version.getVersion_number();
                draw_data data = draw_dao.select_draw_data(latest_version.getId());

                if (data != null && data.getDraw_content() != null) {
                    draw_nontent = data.getDraw_content();
                }
            }

            // 3. Crear el objeto DTO usando el Record (llamada al constructor canónico)
            gallery_record item = new gallery_record(
                    d.getId(),
                    d.getTitle(),
                    d.getCreated_at(),
                    d.isPublic(),
                    version_number,
                    draw_nontent
            );

            gallery_items.add(item);
        }

        return gallery_items;
    }
}