// permission_service.java (Nueva clase)

package com.drawings.drawings.service;

import com.drawings.drawings.dao.draw_dao; // O el DAO donde pusiste los métodos
import com.drawings.drawings.dao.user_dao; // Asumiendo que tienes un DAO para buscar usuarios
import com.drawings.drawings.model.permissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class permission_service {

    @Autowired
    draw_dao draw_dao;
    @Autowired
    user_dao user_dao;

    public boolean grant_permissions(int requester_id, int draw_id, String collaborator_username, boolean can_read, boolean can_write) throws NoSuchElementException, IllegalArgumentException {


        int collaboratorId = user_dao.iduser(collaborator_username);

        if (collaboratorId == requester_id) {
            return true;
        }

        if (can_read || can_write) {
            draw_dao.save_or_update_permissions(draw_id, collaboratorId, can_read, can_write);
        } else {
            draw_dao.delete_permissions(draw_id, collaboratorId);
        }

        return true;
    }

    public boolean can_user_write(int drawId, int userId) {

        if (draw_dao.is_owner(drawId, userId)) {
            return true;
        }

        return draw_dao.get_can_write_permission(drawId, userId);
    }

    public boolean can_user_read(int drawId, int userId) {
        if (draw_dao.is_owner(drawId, userId)) {
            return true;
        }

        Optional<permissions> perms = draw_dao.get_permissions_for_user(drawId, userId);

        return perms.isPresent() && (perms.get().isCan_read() || perms.get().isCan_write());
    }
}