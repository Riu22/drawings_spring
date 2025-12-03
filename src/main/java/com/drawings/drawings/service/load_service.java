package com.drawings.drawings.service;

import com.drawings.drawings.dao.draw_dao;
import com.drawings.drawings.model.draw;
import com.drawings.drawings.model.draw_data;
import com.drawings.drawings.model.version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class load_service {

    @Autowired
    private draw_dao draw_dao;

    public Optional<String> load_draw_content(int drawId) {

        version latest_version = draw_dao.select_latest_draw_version(drawId);
        if (latest_version == null) {
            return Optional.empty();
        }

        draw_data draw_data = draw_dao.select_draw_data(latest_version.getId());

        if (draw_data != null && draw_data.getDraw_content() != null) {
            return Optional.of(draw_data.getDraw_content());
        }

        return Optional.empty();
    }


    public Optional<draw> get_draw_metadata(int drawId) {
        return draw_dao.select_draw_by_id(drawId);
    }
}