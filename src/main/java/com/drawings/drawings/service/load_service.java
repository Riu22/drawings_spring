package com.drawings.drawings.service;

import com.drawings.drawings.dao.*;
import com.drawings.drawings.model.draw;
import com.drawings.drawings.model.draw_data;
import com.drawings.drawings.model.version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class load_service {

    @Autowired
    draw_dao draw_dao;
    @Autowired
    data_version_dao data_version_dao;
    @Autowired
    user_dao user_dao;


    public Optional<String> load_draw_content(int draw_id) {
        version latest_version = data_version_dao.select_latest_draw_version(draw_id);
        if (latest_version == null) {
            return Optional.empty();
        }

        draw_data draw_data = data_version_dao.select_draw_data(latest_version.getId());

        if (draw_data != null && draw_data.getDraw_content() != null) {
            return Optional.of(draw_data.getDraw_content());
        }

        return Optional.empty();
    }


    public Optional<String> load_draw_content_by_version(int draw_id, int version_number) {
        Optional<version> versionOptional = data_version_dao.select_version_by_number(draw_id, version_number);

        if (versionOptional.isEmpty()) {
            return Optional.empty();
        }

        draw_data draw_data = draw_dao.select_draw_data(versionOptional.get().getId());

        if (draw_data != null && draw_data.getDraw_content() != null) {
            return Optional.of(draw_data.getDraw_content());
        }

        return Optional.empty();
    }


    public Optional<draw> get_draw_metadata(int draw_id) {
        return draw_dao.select_draw_by_id(draw_id);
    }


    public List<version> get_all_versions(int draw_id) {
        return draw_dao.select_all_versions_by_draw_id(draw_id);
    }

    public String get_author(int user_id) {
        return user_dao.select_autor_by_id(user_id);
    }
}