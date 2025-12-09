package com.drawings.drawings.controller;

import com.drawings.drawings.records.gallery_record;
import com.drawings.drawings.service.gallery_service;
import com.drawings.drawings.service.public_service;
import com.drawings.drawings.service.save_service;
import com.drawings.drawings.service.trash_service;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;



@Controller
public class gallery_controller {
    @Autowired
    save_service save_service;
    @Autowired
    gallery_service gallery_service;
    @Autowired
    public_service public_service;
    @Autowired
    trash_service trash_service;

    @GetMapping("/gallery/private")
    public String gallery(Model model, HttpSession session){
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
                return "redirect:/gallery/private?message=Dibujo movido a papelera.";
            } else {
                return "redirect:/gallery?error=Acceso denegado o dibujo no encontrado.";
            }

        } catch (Exception e) {
            System.err.println("Error al mover a papelera: " + e.getMessage());
            return "redirect:/gallery?error=Error interno al mover a papelera.";
        }
    }

    @GetMapping("/gallery/public")
    public String public_gallery(Model model, HttpSession session){
        int id_user = save_service.iduser((String) session.getAttribute("username"));
        List<gallery_record> draw= public_service.select_public_draw_details(id_user);
        model.addAttribute("draws",draw);
        return "gallerypub";
    }
}
