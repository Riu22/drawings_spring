package com.drawings.drawings.service;

import com.drawings.drawings.dao.draw_dao;
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
    private draw_dao draw_dao;

    /**
     * Carga el contenido de la última versión de un dibujo
     */
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

    /**
     * Carga el contenido de una versión específica de un dibujo
     */
    public Optional<String> load_draw_content_by_version(int drawId, int versionNumber) {
        Optional<version> versionOptional = draw_dao.select_version_by_number(drawId, versionNumber);

        if (versionOptional.isEmpty()) {
            return Optional.empty();
        }

        draw_data draw_data = draw_dao.select_draw_data(versionOptional.get().getId());

        if (draw_data != null && draw_data.getDraw_content() != null) {
            return Optional.of(draw_data.getDraw_content());
        }

        return Optional.empty();
    }

    /**
     * Obtiene la metadata del dibujo
     */
    public Optional<draw> get_draw_metadata(int drawId) {
        return draw_dao.select_draw_by_id(drawId);
    }

    /**
     * Obtiene todas las versiones de un dibujo
     */
    public List<version> get_all_versions(int drawId) {
        return draw_dao.select_all_versions_by_draw_id(drawId);
    }
}