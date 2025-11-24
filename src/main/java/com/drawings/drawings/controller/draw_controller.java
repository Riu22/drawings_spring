package com.drawings.drawings.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class draw_controller {
    @GetMapping("/drawing")
    public String drawing(HttpSession session){
        return "drawing";
    }

}
