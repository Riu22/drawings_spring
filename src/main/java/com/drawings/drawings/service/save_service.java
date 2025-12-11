package com.drawings.drawings.service;

import com.drawings.drawings.dao.draw_dao;
import com.drawings.drawings.dao.*;
import com.drawings.drawings.model.draw;
import com.drawings.drawings.model.draw_data;
import com.drawings.drawings.model.user;
import com.drawings.drawings.model.version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class save_service {

    @Autowired
    draw_dao draw_dao;

    @Autowired
    user_dao user_dao;
    @Autowired
    permission_dao permission_dao;
    @Autowired
    data_version_dao data_version_dao;

    public int iduser(String name) {
        user user = user_dao.find_user(name);
        if (user == null) {
            throw new NoSuchElementException("Usuario no encontrado: " + name);
        }
        return user.getId();
    }

    @Transactional
    public void save_draw_content_and_version(int draw_id, String drawing_content) {

        int latest_version_number = data_version_dao.get_latest_version_number(draw_id);

        int new_version_number = latest_version_number + 1;

        int version_id = data_version_dao.add_version(draw_id, new_version_number);

        data_version_dao.add_draw_content(version_id, drawing_content);
    }

    @Transactional
    public draw save_or_update_draw(Integer draw_id, String title, boolean is_public, int user_id, String drawing_content) {

        if (draw_id == null || draw_id == 0) {

            draw new_draw = new draw(user_id, title, is_public);
            draw current_draw = draw_dao.add_draw(new_draw);

            int version_id = data_version_dao.add_version(current_draw.getId(), 1);
            draw_dao.add_draw_content(version_id, drawing_content);

            return current_draw;

        } else {

            Optional<draw> optional_draw = draw_dao.select_draw_by_id(draw_id);
            draw current_draw = optional_draw
                    .orElseThrow(() -> new NoSuchElementException("Dibujo ID " + draw_id + " no encontrado."));

            draw_dao.update_draw_metadata(draw_id, title, is_public);

            save_draw_content_and_version(draw_id, drawing_content);

            current_draw.setTitle(title);
            current_draw.setPublic(is_public);

            return current_draw;
        }
    }
    @Transactional
    public draw clone_draw_from_version(int original_draw_id, int version_number, int new_owner_id, String new_title) {

        Optional<draw> original_draw_optional = draw_dao.select_draw_by_id(original_draw_id);
        draw original_draw = original_draw_optional
                .orElseThrow(() -> new NoSuchElementException("Dibujo original ID " + original_draw_id + " no encontrado."));

        Optional<version> version_optional = data_version_dao.select_version_by_number(original_draw_id, version_number);
        version selected_version = version_optional
                .orElseThrow(() -> new NoSuchElementException("Versión " + version_number + " no encontrada."));

        draw_data version_data = data_version_dao.select_draw_data(selected_version.getId());
        if (version_data == null || version_data.getDraw_content() == null) {
            throw new NoSuchElementException("Contenido de la versión no encontrado.");
        }

        String content_to_clone = version_data.getDraw_content();

        String clone_title = (new_title != null && !new_title.trim().isEmpty())
                ? new_title
                : "Copia de " + original_draw.getTitle() + " (v" + version_number + ")";

        draw new_draw = new draw(new_owner_id, clone_title, false);
        draw created_draw = draw_dao.add_draw(new_draw);

        int version_id = draw_dao.add_version(created_draw.getId(), 1);
        draw_dao.add_draw_content(version_id, content_to_clone);

        return created_draw;
    }
}