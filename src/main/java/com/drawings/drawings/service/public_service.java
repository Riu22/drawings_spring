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

        List<gallery_record> galleryItems = new ArrayList<>();

        for (draw d : draws) {
            version latestVersion = draw_dao.select_latest_draw_version(d.getId());

            int versionNumber = 0;
            String drawContent = "";

            if (latestVersion != null) {
                versionNumber = latestVersion.getVersion_number();

                draw_data data = draw_dao.select_draw_data(latestVersion.getId());

                if (data != null && data.getDraw_content() != null) {
                    drawContent = data.getDraw_content();
                }
            }

            gallery_record item = new gallery_record(
                    d.getId(),
                    d.getTitle(),
                    d.getCreated_at(),
                    d.isPublic(),
                    versionNumber,
                    drawContent
            );

            galleryItems.add(item);
        }

        return galleryItems;
    }
}