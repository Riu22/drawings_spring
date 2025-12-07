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

    // ------------------------------------------------------------------------
    // MÉTODOS DE UTILIDAD
    // ------------------------------------------------------------------------

    /**
     * Obtiene el ID del usuario a partir de su nombre.
     * @param name Nombre de usuario.
     * @return ID del usuario.
     * @throws NoSuchElementException Si el usuario no es encontrado.
     */
    public int iduser(String name) {
        user user = user_dao.find_user(name);
        // Asumiendo que find_user devuelve null o lanza excepción si no encuentra
        if (user == null) {
            throw new NoSuchElementException("Usuario no encontrado: " + name);
        }
        return user.getId();
    }

    // ------------------------------------------------------------------------
    // MÉTODOS TRANSACCIONALES DE VERSIÓN (CORE)
    // ------------------------------------------------------------------------

    /**
     * Lógica transaccional para crear una nueva versión (V2, V3, ...)
     * para un dibujo ya existente.
     * @param draw_id ID del dibujo a editar.
     * @param drawing_content Contenido JSON del dibujo.
     */
    @Transactional
    public void save_draw_content_and_version(int draw_id, String drawing_content) {

        // 1. Obtener el número de versión más alto actual (e.g., V5)
        int latest_version_number = draw_dao.get_latest_version_number(draw_id);

        // 2. Calcular el nuevo número de versión (e.g., 5 + 1 = 6)
        int new_version_number = latest_version_number + 1;

        // 3. Insertar la nueva entrada en la tabla 'version'
        int version_id = draw_dao.add_version(draw_id, new_version_number);

        // 4. Insertar el contenido del dibujo ligado a la nueva version_id
        draw_dao.add_draw_content(version_id, drawing_content);
    }

    /**
     * Método principal que gestiona la creación inicial o la actualización de un dibujo.
     * @param draw_id ID del dibujo (null/0 si es nuevo).
     * @param title Título.
     * @param is_public Es público.
     * @param user_id ID del propietario.
     * @param drawing_content Contenido JSON.
     * @return El objeto 'draw' (metadata) guardado o actualizado.
     */
    @Transactional
    public draw save_or_update_draw(Integer draw_id, String title, boolean is_public, int user_id, String drawing_content) {

        if (draw_id == null || draw_id == 0) {

            draw new_draw = new draw(user_id, title, is_public);
            draw current_draw = draw_dao.add_draw(new_draw);

            int version_id = draw_dao.add_version(current_draw.getId(), 1); // Versión 1
            draw_dao.add_draw_content(version_id, drawing_content);

            return current_draw;

        } else {

            Optional<draw> optional_draw = draw_dao.select_draw_by_id(draw_id);
            draw current_draw = optional_draw
                    .orElseThrow(() -> new NoSuchElementException("Dibujo ID " + draw_id + " no encontrado."));

            // 2.2. Guardar metadatos (actualizar título/publicidad en la tabla 'draw')
            // Se necesita este método en el DAO: draw_dao.update_draw_metadata(draw_id, title, is_public);

            // 2.3. Crear la Versión V2, V3, etc.
            save_draw_content_and_version(draw_id, drawing_content);

            // Actualizar el objeto draw en memoria con los nuevos valores
            current_draw.setTitle(title);
            current_draw.setPublic(is_public);

            return current_draw;
        }
    }
}