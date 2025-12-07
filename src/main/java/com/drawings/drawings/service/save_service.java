package com.drawings.drawings.service;

import com.drawings.drawings.dao.draw_dao;
import com.drawings.drawings.dao.user_dao;
import com.drawings.drawings.model.draw;
import com.drawings.drawings.model.user;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class save_service {

    @Autowired
    private draw_dao draw_dao;

    @Autowired
    private user_dao user_dao;

    public int iduser(String name) {
        user user = user_dao.find_user(name);
        if (user == null) {
            throw new NoSuchElementException("Usuario no encontrado: " + name);
        }
        return user.getId();
    }

    @Transactional
    public void save_draw_content_and_version(int draw_id, String drawing_content) {

        int latest_version_number = draw_dao.get_latest_version_number(draw_id);

        int new_version_number = latest_version_number + 1;

        int version_id = draw_dao.add_version(draw_id, new_version_number);

        draw_dao.add_draw_content(version_id, drawing_content);
    }

    @Transactional
    public draw save_or_update_draw(Integer draw_id, String title, boolean is_public, int user_id, String drawing_content) {

        if (draw_id == null || draw_id == 0) {

            draw new_draw = new draw(user_id, title, is_public);
            draw current_draw = draw_dao.add_draw(new_draw);

            int version_id = draw_dao.add_version(current_draw.getId(), 1); // Versión 1
            draw_dao.add_draw_content(version_id, drawing_content);

            return current_draw;

        } else {

            // 2.1. Verificar que el dibujo existe
            Optional<draw> optional_draw = draw_dao.select_draw_by_id(draw_id);
            draw current_draw = optional_draw
                    .orElseThrow(() -> new NoSuchElementException("Dibujo ID " + draw_id + " no encontrado."));

            // 2.2. ⭐ ACTUALIZAR METADATOS (título e ispublic)
            draw_dao.update_draw_metadata(draw_id, title, is_public);

            // 2.3. Crear nueva versión con el contenido actualizado
            save_draw_content_and_version(draw_id, drawing_content);

            // 2.4. Actualizar el objeto draw en memoria con los nuevos valores
            current_draw.setTitle(title);
            current_draw.setPublic(is_public);

            return current_draw;
        }
    }
}