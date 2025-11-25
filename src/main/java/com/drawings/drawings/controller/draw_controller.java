package com.drawings.drawings.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class draw_controller {
    @GetMapping("/drawing")
    public String drawing(HttpSession session, Model model){
        String username = (String) session.getAttribute("username");
        model.addAttribute("username", username);
        return "drawing";
    }

    @GetMapping("/save")
    public String save(HttpSession session, Model model, HttpServletResponse response){
        // Cridar al servei per emmagatzemar el dibuix (versió)
        // Si l'usuar té permís, tornar un codi 200
        // Si l'usuari no té permís, tornar un codi 400... (error d'usuari)
        response.setStatus(400);
        return "ok";
    }

}
