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

    // Inyección de DAO y Servicio de Permisos
    private final draw_dao draw_dao;
    private final permission_service permission_service; // ⭐ Asumimos inyección para checar permisos

    @Autowired
    public gallery_service(draw_dao draw_dao, permission_service permission_service) {
        this.draw_dao = draw_dao;
        this.permission_service = permission_service;
    }

    /**
     * Recupera la lista de dibujos que un usuario puede ver (dueño o colaborador)
     * y calcula la bandera 'can_edit' para cada elemento.
     * * @param userId El ID del usuario logueado.
     * @return Lista de records de galería.
     */
    public List<gallery_record> select_owners_draw_details(int userId){

        // 1. Obtener TODOS los dibujos visibles para el usuario (dueño O colaborador)
        // Se asume que draw_dao.select_owners_draws ahora usa el JOIN/UNION.
        List<draw> draws = draw_dao.select_viewable_draws(userId);
        List<gallery_record> gallery_items = new ArrayList<>();

        for (draw d : draws) {

            // --- Carga de Datos y Versión ---
            version latest_version = draw_dao.select_latest_draw_version(d.getId());

            int version_number = 0;
            String draw_content = ""; // Corregido de draw_nontent a draw_content

            if (latest_version != null) {
                version_number = latest_version.getVersion_number();
                draw_data data = draw_dao.select_draw_data(latest_version.getId());

                if (data != null && data.getDraw_content() != null) {
                    draw_content = data.getDraw_content();
                }
            }

            // --- 2. Cálculo de Permiso de Edición (can_edit) ---

            // Si eres el dueño, siempre puedes editar.
            boolean isOwner = d.getUser_id() == userId;
            boolean canEdit = isOwner;

            if (!isOwner) {
                // Si no eres el dueño, verifica si tienes permiso de escritura asignado
                canEdit = permission_service.canUserWrite(d.getId(), userId);
            }
            // --- Fin Cálculo Permiso ---

            // 3. Crear el objeto DTO (Record)
            gallery_record item = new gallery_record(
                    d.getId(),
                    d.getTitle(),
                    d.getCreated_at(),
                    d.isPublic(),
                    version_number,
                    draw_content,
                    canEdit
            );

            gallery_items.add(item);
        }

        return gallery_items;
    }
}