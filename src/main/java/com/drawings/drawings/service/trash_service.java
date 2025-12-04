package com.drawings.drawings.service;

import com.drawings.drawings.dao.draw_dao;
import com.drawings.drawings.model.draw;
import com.drawings.drawings.model.draw_data; // Necesitas este import para el contenido
import com.drawings.drawings.model.version;  // Necesitas este import para la versión
import com.drawings.drawings.records.gallery_record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service; // ¡Faltaba la anotación @Service!
import java.util.ArrayList;
import java.util.List;

@Service // ⬅️ ¡Anotación de Spring necesaria!
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
                    ""
            );

            gallery_items.add(item);
        }

        return gallery_items;
    }
}