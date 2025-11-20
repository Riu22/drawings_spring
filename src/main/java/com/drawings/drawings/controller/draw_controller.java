package com.drawings.drawings.controller;

import com.drawings.drawings.service.session_service;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class draw_controller {
    @Autowired
    private session_service session_service;

    @GetMapping("/drawing")
    public String drawing(HttpSession session){
        if(!session_service.is_user_logged_in(session)) {
            return "redirect:/login";
        }
        return "drawing";
    }

}
