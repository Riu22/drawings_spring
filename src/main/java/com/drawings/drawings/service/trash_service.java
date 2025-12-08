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
public class trash_service {

    @Autowired
    draw_dao draw_dao;

    public List<gallery_record> get_trashed_draws(int owner_id) {
        List<draw> trashed_draws = draw_dao.select_trashed_draws(owner_id);

        if (trashed_draws.isEmpty()) {
            return new ArrayList<>();
        }

        List<gallery_record> gallery_items = new ArrayList<>();
        for (draw d : trashed_draws) {
            gallery_record item = new gallery_record(
                    d.getId(),
                    d.getTitle(),
                    d.getCreated_at(),
                    d.isPublic(),
                    0,
                    "",
                    true,
                    d.getUser_id()
            );

            gallery_items.add(item);
        }

        return gallery_items;
    }

    public boolean delete_trashed_draw(int draw_id, int user_id) {

        List<draw> trashed_draws = draw_dao.select_trashed_draws(user_id);
        boolean is_owner_and_trashed = trashed_draws.stream()
                .anyMatch(d -> d.getId() == draw_id);

        if (!is_owner_and_trashed) {
            return false;
        }

        try {
            int rows_deleted = draw_dao.delete_draw_by_id(draw_id);
            return rows_deleted > 0;
        } catch (Exception e) {
            System.err.println("Error al eliminar el dibujo ID " + draw_id + ": " + e.getMessage());
            return false;
        }
    }

    public boolean move_to_trash(int draw_id, int user_id) {
        try {
            int rows_affected = draw_dao.update_draw_to_trashed(draw_id, user_id);
            return rows_affected > 0;
        } catch (Exception e) {
            System.err.println("Error al intentar mover el dibujo ID " + draw_id + " a la papelera: " + e.getMessage());
            return false;
        }
    }

    public boolean rescue_from_trash(int draw_id, int user_id) {
        try {
            int rows_affected = draw_dao.rescue_draw_from_trash(draw_id, user_id);
            return rows_affected > 0;
        } catch (Exception e) {
            System.err.println("Error al intentar rescatar el dibujo ID " + draw_id + " de la papelera: " + e.getMessage());
            return false;
        }
    }
}