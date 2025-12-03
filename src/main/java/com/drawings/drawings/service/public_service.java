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
public class public_service {

    @Autowired
    draw_dao draw_dao;


    public List<gallery_record> select_public_draw_details(){

        List<draw> draws = draw_dao.select_public_draws();

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

            gallery_record item = new gallery_record(
                    d.getId(),
                    d.getTitle(),
                    d.getCreated_at(),
                    d.isPublic(),
                    version_number,
                    draw_content
            );

            gallery_items.add(item);
        }

        return gallery_items;
    }
}