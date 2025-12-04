package com.drawings.drawings.controller;

// ... (Otros imports) ...
import com.drawings.drawings.records.gallery_record; // Necesario para el tipo de lista
import com.drawings.drawings.service.trash_service; // Asumo que inyectaste este servicio
import com.drawings.drawings.service.save_service; // Asumo que inyectaste este servicio
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.NoSuchElementException;

@Controller
public class trash_controller {

    @Autowired
    private save_service save_service;

    @Autowired
    private trash_service trash_service;

    @GetMapping("/trash")
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
}