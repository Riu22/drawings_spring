package com.drawings.drawings.controller;

import com.drawings.drawings.records.draw_request;
import com.drawings.drawings.model.draw;
import com.drawings.drawings.records.gallery_record;
import com.drawings.drawings.service.gallery_service;
import com.drawings.drawings.service.load_service;
import com.drawings.drawings.service.public_service;
import com.drawings.drawings.service.save_service;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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

    @GetMapping("/home")
    public String drawing(HttpSession session, Model model){
        String username = (String) session.getAttribute("username");
        model.addAttribute("username", username);
        return "drawing";
    }

    @PostMapping("/save")
    public String saveDrawing(HttpSession session, @RequestBody draw_request draw_request, HttpServletRequest request, HttpServletResponse response) {

        String author = (String) session.getAttribute("username");
        int id_author = save_service.iduser(author);

        try {
            draw saved_draw = save_service.save_draw(
                    draw_request.title(),
                    draw_request.ispublic(),
                    id_author,
                    draw_request.draw_content()
            );

            return "redirect:/login";

        } catch (NoSuchElementException e) {
            System.err.println("Error al guardar el dibujo: " + e.getMessage());
            return "redirect:/error?message=Datos Inválidos";
        } catch (Exception e) {
            System.err.println("Error interno del servidor: " + e.getMessage());
            return "redirect:/error?message=Fallo al guardar el dibujo";
        }
    }

    @GetMapping("/gallery")
    public String gallery(Model model,HttpSession session){
        int owner_id = save_service.iduser((String) session.getAttribute("username"));
        List<gallery_record> draw=gallery_service.select_owners_draw_details(owner_id);
        model.addAttribute("draws",draw);
        return "gallery";
    }

    @GetMapping("/pub_gallery")
    public String public_gallery(Model model){
        List<gallery_record> draw= public_service.select_public_draw_details();
        model.addAttribute("draws",draw);
        return "gallerypub";
    }

    @GetMapping("/draw/{drawId}")
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
