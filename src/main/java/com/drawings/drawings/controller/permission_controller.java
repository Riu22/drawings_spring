// permission_controller.java - AJUSTADO A @Controller

package com.drawings.drawings.controller;

import com.drawings.drawings.model.draw;
import com.drawings.drawings.service.load_service;
import com.drawings.drawings.service.permission_service;
import com.drawings.drawings.service.save_service; // Asumo que save_service tiene iduser
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller; // ⭐ Cambiado a @Controller
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.NoSuchElementException;
import java.util.Optional;

@Controller
public class permission_controller {

    @Autowired
    load_service load_service;
    save_service save_service;
    permission_service permission_service;
    public record PermissionRequest(
            int draw_id,
            String collaborator_username,
            boolean can_read,
            boolean can_write
    ) {}

    @Autowired
    public permission_controller(permission_service permission_service, save_service save_service) {
        this.permission_service = permission_service;
        this.save_service = save_service;
    }

    @PostMapping("/grant")
    @ResponseBody
    public String grantPermissions(@RequestBody PermissionRequest request, HttpSession session) {

        String username = (String) session.getAttribute("username");

        if (username == null) {
            return "ERROR_UNAUTHORIZED";
        }

        try {
            int requesterId = save_service.iduser(username);

            permission_service.grant_permissions(
                    requesterId,
                    request.draw_id(),
                    request.collaborator_username(),
                    request.can_read(),
                    request.can_write()
            );

            return "SUCCESS";

        } catch (NoSuchElementException e) {
            System.err.println("Error: Colaborador o dueño no encontrado: " + e.getMessage());
            return "ERROR_USER_NOT_FOUND";
        } catch (IllegalArgumentException e) {
            // El servicio lanza esta excepción si el usuario autenticado no es el dueño del dibujo
            System.err.println("Error de permisos: " + e.getMessage());
            return "ERROR_NOT_OWNER";
        } catch (Exception e) {
            System.err.println("Error interno del servidor al asignar permisos: " + e.getMessage());
            return "ERROR_INTERNAL";
        }
    }
    @GetMapping("/share/{drawId}")
    public String shareDrawingPage(@PathVariable("drawId") int drawId,
                                   HttpSession session,
                                   Model model) {

        String username = (String) session.getAttribute("username");

        try {
            int ownerId = save_service.iduser(username);

            // 1. Verificar la propiedad del dibujo
            Optional<draw> drawMetadata = load_service.get_draw_metadata(drawId);

            if (drawMetadata.isEmpty() || drawMetadata.get().getUser_id() != ownerId) {
                // El dibujo no existe o el usuario no es el dueño
                return "redirect:/gallery?error=No tiene permisos para compartir este dibujo.";
            }

            draw currentDraw = drawMetadata.get();

            model.addAttribute("drawId", drawId);
            model.addAttribute("drawTitle", currentDraw.getTitle());

            return "share"; // ⭐ Nombre de tu nueva vista Thymeleaf

        } catch (Exception e) {
            System.err.println("Error al cargar la página de compartir: " + e.getMessage());
            return "redirect:/gallery?error=Error interno al cargar la página.";
        }
    }
}