// permission_service.java (Nueva clase)

package com.drawings.drawings.service;

import com.drawings.drawings.dao.draw_dao; // O el DAO donde pusiste los métodos
import com.drawings.drawings.dao.user_dao; // Asumiendo que tienes un DAO para buscar usuarios
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class permission_service {

    @Autowired
    draw_dao draw_dao;
    @Autowired
    user_dao user_dao;

    public boolean grant_permissions(int requesterId, int drawId, String collaboratorUsername, boolean canRead, boolean canWrite) throws NoSuchElementException, IllegalArgumentException {


        int collaboratorId = user_dao.iduser(collaboratorUsername);

        if (collaboratorId == requesterId) {
            return true;
        }

        if (canRead || canWrite) {
            draw_dao.save_or_update_permissions(drawId, collaboratorId, canRead, canWrite);
        } else {
            draw_dao.delete_permissions(drawId, collaboratorId);
        }

        return true;
    }

    public boolean canUserWrite(int drawId, int userId) {

        if (draw_dao.is_owner(drawId, userId)) {
            return true;
        }

        return draw_dao.get_can_write_permission(drawId, userId);
    }
}