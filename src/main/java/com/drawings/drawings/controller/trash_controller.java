package com.drawings.drawings.controller;

import com.drawings.drawings.records.gallery_record;
import com.drawings.drawings.service.trash_service;
import com.drawings.drawings.service.save_service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.NoSuchElementException;

@Controller
public class trash_controller {

    @Autowired
    private save_service save_service;

    @Autowired
    private trash_service trash_service;

    @GetMapping("/gallery/trash")
    public String trash(HttpSession session, Model model) {

        String username = (String) session.getAttribute("username");
        try {
            int owner_id = save_service.iduser(username);

            List<gallery_record> trashed_draws = trash_service.get_trashed_draws(owner_id);

            model.addAttribute("trashed_draws", trashed_draws);
            model.addAttribute("username", username);

            return "trash";

        } catch (Exception e) {
            System.err.println("Error al cargar la papelera para " + username + ": " + e.getMessage());
            return "redirect:/error?message=Fallo al cargar la papelera";
        }
    }

    @GetMapping("/trash/delete/{drawId}")
    public String delete_draw_from_trash(@PathVariable("drawId") int drawId, HttpSession session) {

        String username = (String) session.getAttribute("username");

        try {
            int user_id = save_service.iduser(username);

            boolean success = trash_service.delete_trashed_draw(drawId, user_id);

            if (success) {
                return "redirect:/gallery/trash?message=Dibujo eliminado permanentemente.";
            } else {
                return "redirect:/gallery/trash?error=No se pudo eliminar el dibujo o acceso denegado.";
            }

        } catch (java.util.NoSuchElementException e) {
            return "redirect:/login";
        } catch (Exception e) {
            return "redirect:/error?message=Error al procesar la solicitud de borrado.";
        }
    }

    @GetMapping("/trash/rescue/{drawId}")
    public String rescue_draw_to_trash(@PathVariable("drawId") int drawId, HttpSession session) {
        try {
            boolean success = trash_service.rescue_from_trash(drawId, save_service.iduser((String) session.getAttribute("username")));

            if (success) {
                return "redirect:/gallery/private?message=Dibujo restaurado.";
            } else {
                return "redirect:/gallery?error=No se pudo mover el dibujo a la papelera o acceso denegado.";
            }

        } catch (NoSuchElementException e) {
            return "redirect:/login";
        } catch (Exception e) {
            return "redirect:/error?message=Error al procesar la solicitud de mover a la papelera.";
        }
    }
}