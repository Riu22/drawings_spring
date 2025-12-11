package com.drawings.drawings.controller;

import com.drawings.drawings.records.draw_request;
import com.drawings.drawings.model.draw;
import com.drawings.drawings.service.*;
import com.drawings.drawings.model.version;
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
    load_service load_service;

    @Autowired
    permission_service permission_service;

    @GetMapping({"/draw/new", "/draw/{id}"})
    public String editDrawing(@PathVariable("id") Optional<Integer> drawIdOptional,
                              HttpSession session,
                              Model model) {

        String username = (String) session.getAttribute("username");
        model.addAttribute("username", username);

        if (username == null) {
            return "redirect:/login";
        }

        int loggedUserId;
        try {
            loggedUserId = save_service.iduser(username);
        } catch (NoSuchElementException e) {
            return "redirect:/login";
        }

        model.addAttribute("drawId", null);
        model.addAttribute("drawTitle", "Nuevo Dibujo");
        model.addAttribute("isPublic", false);
        model.addAttribute("initialDrawContent", "{}");
        model.addAttribute("canEdit", true);


        if (drawIdOptional.isPresent()) {
            int drawId = drawIdOptional.get();

            try {
                Optional<draw> drawMetadata = load_service.get_draw_metadata(drawId);

                if (drawMetadata.isEmpty()) {
                    return "redirect:/error?message=Dibujo no encontrado.";
                }

                draw currentDraw = drawMetadata.get();

                boolean canEdit = permission_service.can_user_write(drawId, loggedUserId);

                if (!canEdit) {
                    return "redirect:/error?message=Acceso denegado. No tienes permisos de edición.";

                }

                Optional<String> drawContentOptional = load_service.load_draw_content(drawId);
                String drawContent = drawContentOptional.orElse("{}");

                model.addAttribute("drawId", drawId);
                model.addAttribute("drawTitle", currentDraw.getTitle());
                model.addAttribute("isPublic", currentDraw.isPublic());
                model.addAttribute("initialDrawContent", drawContent);
                model.addAttribute("canEdit", true);

            } catch (Exception e) {
                System.err.println("Error al cargar el dibujo para edición: " + e.getMessage());
                return "redirect:/error?message=Fallo al cargar el dibujo para edición.";
            }
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

    @GetMapping("/view/{drawId}")
    public String load_draw(@PathVariable("drawId") int draw_id, Model model, HttpSession session){

        String username = (String) session.getAttribute("username");
        try {
            int user_id = save_service.iduser(username);

            Optional<draw> optional_draw = load_service.get_draw_metadata(draw_id);

            if (optional_draw.isEmpty()) {
                return "redirect:/error?message=Dibujo no encontrado";
            }

            draw draw_metadata = optional_draw.get();
            String author = load_service.get_author(draw_metadata.getUser_id());

            boolean canView = draw_metadata.isPublic() ||
                    draw_metadata.getUser_id() == user_id ||
                    permission_service.can_user_read(draw_id, user_id);

            if (!canView) {
                return "redirect:/error?message=Acceso denegado al dibujo";
            }

            Optional<String> optional_content = load_service.load_draw_content(draw_id);
            String draw_content_json = optional_content.orElse("[]");

            model.addAttribute("drawId", draw_id);
            model.addAttribute("drawContentJson", draw_content_json);
            model.addAttribute("drawTitle", draw_metadata.getTitle());
            model.addAttribute("author", author);

            return "viewdraw";

        } catch (java.util.NoSuchElementException e) {
            return "redirect:/error?message=Usuario no válido";

        } catch (Exception e) {
            System.err.println("Error al cargar el dibujo ID " + draw_id + ": " + e.getMessage());
            return "redirect:/error?message=Error interno al cargar el dibujo";
        }
    }


    @GetMapping("/draw/{drawId}/versions")
    public String showVersions(@PathVariable("drawId") int drawId,
                               HttpSession session,
                               Model model) {

        String username = (String) session.getAttribute("username");

        if (username == null) {
            return "redirect:/login";
        }

        try {
            int userId = save_service.iduser(username);

            Optional<draw> drawOptional = load_service.get_draw_metadata(drawId);

            if (drawOptional.isEmpty()) {
                return "redirect:/error?message=Dibujo no encontrado";
            }

            draw drawMetadata = drawOptional.get();

            boolean canView = drawMetadata.isPublic() ||
                    drawMetadata.getUser_id() == userId ||
                    permission_service.can_user_read(drawId, userId);

            if (!canView) {
                return "redirect:/error?message=Acceso denegado";
            }

            List<version> versions = load_service.get_all_versions(drawId);

            model.addAttribute("drawId", drawId);
            model.addAttribute("drawTitle", drawMetadata.getTitle());
            model.addAttribute("versions", versions);
            model.addAttribute("username", username);

            return "versions";

        } catch (Exception e) {
            System.err.println("Error al cargar versiones: " + e.getMessage());
            return "redirect:/error?message=Error al cargar versiones";
        }
    }

    @GetMapping("/view/{drawId}/version/{versionNumber}")
    public String viewSpecificVersion(@PathVariable("drawId") int drawId,
                                      @PathVariable("versionNumber") int versionNumber,
                                      HttpSession session,
                                      Model model) {

        String username = (String) session.getAttribute("username");

        try {
            int userId = save_service.iduser(username);

            Optional<draw> drawOptional = load_service.get_draw_metadata(drawId);

            if (drawOptional.isEmpty()) {
                return "redirect:/error?message=Dibujo no encontrado";
            }

            draw drawMetadata = drawOptional.get();

            boolean canView = drawMetadata.isPublic() ||
                    drawMetadata.getUser_id() == userId ||
                    permission_service.can_user_read(drawId, userId);

            if (!canView) {
                return "redirect:/error?message=Acceso denegado al dibujo";
            }

            Optional<String> contentOptional = load_service.load_draw_content_by_version(drawId, versionNumber);
            String drawContent = contentOptional.orElse("[]");

            model.addAttribute("drawId", drawId);
            model.addAttribute("drawContentJson", drawContent);
            model.addAttribute("drawTitle", drawMetadata.getTitle() + " (Versión " + versionNumber + ")");
            model.addAttribute("versionNumber", versionNumber);
            model.addAttribute("username", username);

            return "viewdraw";

        } catch (Exception e) {
            System.err.println("Error al cargar versión específica: " + e.getMessage());
            return "redirect:/error?message=Error al cargar la versión";
        }
    }

    @GetMapping("/draw/{drawId}/version/{versionNumber}")
    public String editSpecificVersion(@PathVariable("drawId") int drawId,
                                      @PathVariable("versionNumber") int versionNumber,
                                      HttpSession session,
                                      Model model) {

        String username = (String) session.getAttribute("username");
        model.addAttribute("username", username);

        if (username == null) {
            return "redirect:/login";
        }

        try {
            int loggedUserId = save_service.iduser(username);

            Optional<draw> drawMetadata = load_service.get_draw_metadata(drawId);

            if (drawMetadata.isEmpty()) {
                return "redirect:/error?message=Dibujo no encontrado.";
            }

            draw currentDraw = drawMetadata.get();

            boolean canEdit = permission_service.can_user_write(drawId, loggedUserId);

            if (!canEdit) {
                return "redirect:/error?message=Acceso denegado. No tienes permisos de edición.";
            }

            Optional<String> drawContentOptional = load_service.load_draw_content_by_version(drawId, versionNumber);
            String drawContent = drawContentOptional.orElse("[]");

            model.addAttribute("drawId", drawId);
            model.addAttribute("drawTitle", currentDraw.getTitle());
            model.addAttribute("isPublic", currentDraw.isPublic());
            model.addAttribute("initialDrawContent", drawContent);
            model.addAttribute("canEdit", true);
            model.addAttribute("editingVersion", versionNumber);

            return "drawing";

        } catch (Exception e) {
            System.err.println("Error al cargar la versión para edición: " + e.getMessage());
            return "redirect:/error?message=Fallo al cargar la versión para edición.";
        }
    }


    @PostMapping("/draw/{drawId}/version/{versionNumber}/clone")
    public String cloneVersion(@PathVariable("drawId") int drawId,
                               @PathVariable("versionNumber") int versionNumber,
                               @RequestParam(value = "title", required = false) String newTitle,
                               HttpSession session) {

        String username = (String) session.getAttribute("username");

        if (username == null) {
            return "redirect:/login";
        }

        try {
            int userId = save_service.iduser(username);

            Optional<draw> drawOptional = load_service.get_draw_metadata(drawId);

            if (drawOptional.isEmpty()) {
                return "redirect:/error?message=Dibujo no encontrado";
            }

            draw originalDraw = drawOptional.get();

            boolean canRead = originalDraw.isPublic() ||
                    originalDraw.getUser_id() == userId ||
                    permission_service.can_user_read(drawId, userId);

            if (!canRead) {
                return "redirect:/error?message=Acceso denegado. No puedes copiar este dibujo.";
            }

            draw clonedDraw = save_service.clone_draw_from_version(drawId, versionNumber, userId, newTitle);

            return "redirect:/draw/" + clonedDraw.getId() + "?message=Dibujo clonado con éxito";

        } catch (NoSuchElementException e) {
            System.err.println("Error al clonar: " + e.getMessage());
            return "redirect:/error?message=" + e.getMessage();
        } catch (Exception e) {
            System.err.println("Error al clonar versión: " + e.getMessage());
            return "redirect:/error?message=Error al crear la copia del dibujo";
        }
    }

}