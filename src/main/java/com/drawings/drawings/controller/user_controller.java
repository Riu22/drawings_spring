package com.drawings.drawings.controller;

import com.drawings.drawings.service.login_service;
import com.drawings.drawings.service.register_service;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class user_controller {

    @Autowired
    private register_service register_service;

    @Autowired
    private login_service login_service;


    @GetMapping("/")
    public String index(){
        return "index";
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @PostMapping("/login")
    public String correct_login(@RequestParam String username, @RequestParam String password, HttpSession session, Model model){
        if(login_service.check_user(username, password)) {
            session.setAttribute("username", username);
            return "redirect:/draw/new";
        }
        model.addAttribute("error", "Invalid username or password");
        return "login";
    }

    @GetMapping("/register")
    public String register(){
        return "register";
    }

    @PostMapping("/register")
    public String correct_register(@RequestParam String name, @RequestParam String password, @RequestParam String username,Model model){
        try{
        register_service.add_user(name, password, username);
        }catch (Exception e){
            model.addAttribute("error", e.getMessage());
            return "register";
        }
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session){
        session.invalidate();
        return "redirect:/";
    }
}
