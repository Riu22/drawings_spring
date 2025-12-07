package com.drawings.drawings.controller;

import com.drawings.drawings.model.draw_data;
import com.drawings.drawings.records.draw_request;
import com.drawings.drawings.model.draw;
import com.drawings.drawings.records.gallery_record;
import com.drawings.drawings.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Controller
public class draw_controller {
    @Autowired
    save_service save_service;
    @Autowired
    gallery_service gallery_service;
    @Autowired
    public_service public_service;
    @Autowired
    load_service load_service;
    @Autowired
    trash_service trash_service;
    @Autowired
    permission_service permission_service;

    @GetMapping({"/draw/new", "/draw/{id}"})
    public String editDrawing(@PathVariable("id") Optional<Integer> drawIdOptional,
                              HttpSession session,
                              Model model) {

        String username = (String) session.getAttribute("username");
        model.addAttribute("username", username);

        // Si no está autenticado, redirigir al login
        if (username == null) {
            return "redirect:/login";
        }

        // Obtener el ID del usuario logueado
        int loggedUserId;
        try {
            loggedUserId = save_service.iduser(username);
        } catch (NoSuchElementException e) {
            // Esto debería ser manejado por un Interceptor, pero es una buena guardia
            return "redirect:/login";
        }

        model.addAttribute("drawId", null);
        model.addAttribute("drawTitle", "Nuevo Dibujo");
        model.addAttribute("isPublic", false);
        model.addAttribute("initialDrawContent", "{}");
        model.addAttribute("canEdit", true);

        // --- Lógica de Carga y Permisos ---

        if (drawIdOptional.isPresent()) {
            int drawId = drawIdOptional.get();

            try {
                Optional<draw> drawMetadata = load_service.get_draw_metadata(drawId);

                if (drawMetadata.isEmpty()) {
                    return "redirect:/error?message=Dibujo no encontrado.";
                }

                draw currentDraw = drawMetadata.get();

                // 1. Verificar si el usuario puede escribir (dueño o colaborador con can_write)
                boolean canEdit = permission_service.canUserWrite(drawId, loggedUserId);

                if (!canEdit) {
                    // Si no puede editar, se le niega el acceso a la interfaz de edición
                    return "redirect:/error?message=Acceso denegado. No tienes permisos de edición.";
                    // Alternativamente, podrías redirigir a la vista de solo lectura:
                    // return "redirect:/view/" + drawId;
                }

                // 2. Cargar el contenido de la última versión usando load_draw_content
                Optional<String> drawContentOptional = load_service.load_draw_content(drawId);
                String drawContent = drawContentOptional.orElse("{}");

                // 3. Añadir datos al modelo para la edición
                model.addAttribute("drawId", drawId);
                model.addAttribute("drawTitle", currentDraw.getTitle());
                model.addAttribute("isPublic", currentDraw.isPublic());
                model.addAttribute("initialDrawContent", drawContent);
                model.addAttribute("canEdit", true); // Confirmamos que puede editar

            } catch (Exception e) {
                System.err.println("Error al cargar el dibujo para edición: " + e.getMessage());
                return "redirect:/error?message=Fallo al cargar el dibujo para edición.";
            }
        } else {
            // ⭐ Modo CREACIÓN (/draw/new)
        }

        return "drawing";
    }


    @PostMapping("/save")
    @ResponseBody
    public String saveDrawing(HttpSession session, @RequestBody draw_request draw_request) {

        String author = (String) session.getAttribute("username");

        try {
            int id_author = save_service.iduser(author);

            draw saved_draw = save_service.save_or_update_draw(
                    draw_request.draw_id(),
                    draw_request.title(),
                    draw_request.ispublic(),
                    id_author,
                    draw_request.draw_content()
            );

            return String.valueOf(saved_draw.getId());

        } catch (NoSuchElementException e) {
            System.err.println("Error al guardar el dibujo: " + e.getMessage());
            return "ERROR_INVALID_DATA";
        } catch (Exception e) {
            System.err.println("Error interno del servidor al guardar: " + e.getMessage());
            return "ERROR_SERVER_FAILURE";
        }
    }

    @GetMapping("/gallery")
    public String gallery(Model model,HttpSession session){
        int username = save_service.iduser((String) session.getAttribute("username"));
        int logged_user_id = save_service.iduser((String) session.getAttribute("username"));
        List<gallery_record> draw=gallery_service.select_owners_draw_details(username);
        model.addAttribute("logged_user_id", logged_user_id);
        model.addAttribute("draws",draw);
        return "gallery";
    }

    @GetMapping("/gallery/trash/{drawId}")
    public String move_to_trash(@PathVariable("drawId") int draw_id, HttpSession session) {

        String username = (String) session.getAttribute("username");

        try {
            int user_id = save_service.iduser(username);

            boolean success = trash_service.move_to_trash(draw_id, user_id);

            if (success) {
                return "redirect:/gallery?message=Dibujo enviado a la papelera.";
            } else {
                return "redirect:/gallery?error=Acceso denegado o dibujo no encontrado.";
            }

        } catch (Exception e) {
            System.err.println("Error al mover a papelera: " + e.getMessage());
            return "redirect:/gallery?error=Error interno al mover a papelera.";
        }
    }

    @GetMapping("/pub_gallery")
    public String public_gallery(Model model){
        List<gallery_record> draw= public_service.select_public_draw_details();
        model.addAttribute("draws",draw);
        return "gallerypub";
    }

    @GetMapping("/view/{drawId}")
    public String load_draw(@PathVariable("drawId") int draw_id, Model model, HttpSession session){

        String username = (String) session.getAttribute("username");
        try {
            int owner_id = save_service.iduser(username);

            Optional<draw> optional_draw = load_service.get_draw_metadata(draw_id);

            if (optional_draw.isEmpty()) {
                return "redirect:/error?message=Dibujo no encontrado";
            }

            draw draw_metadata = optional_draw.get();

            if (!draw_metadata.isPublic() && draw_metadata.getUser_id() != owner_id) {
                return "redirect:/error?message=Acceso denegado al dibujo";
            }

            Optional<String> optional_content = load_service.load_draw_content(draw_id);
            String draw_content_json = optional_content.orElse("[]");

            model.addAttribute("drawId", draw_id);
            model.addAttribute("drawContentJson", draw_content_json);
            model.addAttribute("drawTitle", draw_metadata.getTitle());
            model.addAttribute("username", username);

            return "viewdraw";

        } catch (java.util.NoSuchElementException e) {
            return "redirect:/error?message=Usuario no válido";

        } catch (Exception e) {
            System.err.println("Error al cargar el dibujo ID " + draw_id + ": " + e.getMessage());
            return "redirect:/error?message=Error interno al cargar el dibujo";
        }
    }
}