package com.drawings.drawings.controller;

import com.drawings.drawings.model.draw;
import com.drawings.drawings.service.load_service;
import com.drawings.drawings.service.permission_service;
import com.drawings.drawings.service.save_service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.NoSuchElementException;
import java.util.Optional;

@Controller
public class permission_controller {

    @Autowired
    load_service load_service;
    @Autowired
    save_service save_service;
    @Autowired
    permission_service permission_service;
    public record PermissionRequest(
            int draw_id,
            String collaborator_username,
            boolean can_read,
            boolean can_write
    ) {}


    @PostMapping("/grant")
    @ResponseBody
    public String grant_permissions(@RequestBody PermissionRequest request, HttpSession session) {

        String username = (String) session.getAttribute("username");

        try {
            int requester_id = save_service.iduser(username);

            permission_service.grant_permissions(
                    requester_id,
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
            System.err.println("Error de permisos: " + e.getMessage());
            return "ERROR_NOT_OWNER";
        } catch (Exception e) {
            System.err.println("Error interno del servidor al asignar permisos: " + e.getMessage());
            return "ERROR_INTERNAL";
        }
    }
    @GetMapping("/share/{drawId}")
    public String share_drawing_page(@PathVariable("drawId") int draw_id,
                                     HttpSession session,
                                     Model model) {

        String username = (String) session.getAttribute("username");

        try {
            int owner_id = save_service.iduser(username);

            Optional<draw> drawMetadata = load_service.get_draw_metadata(draw_id);

            if (drawMetadata.isEmpty() || drawMetadata.get().getUser_id() != owner_id) {
                return "redirect:/gallery?error=No tiene permisos para compartir este dibujo.";
            }

            draw current_draw = drawMetadata.get();

            model.addAttribute("drawId", draw_id);
            model.addAttribute("drawTitle", current_draw.getTitle());

            return "share";

        } catch (Exception e) {
            System.err.println("Error al cargar la página de compartir: " + e.getMessage());
            return "redirect:/gallery?error=Error interno al cargar la página.";
        }
    }
}