package com.drawings.drawings.controller;

import com.drawings.drawings.service.register_service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class user_controller {

    @Autowired
    private register_service register_service;

    @GetMapping("/")
    public String index(){
        return "index";
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @PostMapping("/login")
    public String correct_login(){
        return "drawing";
    }

    @GetMapping("/register")
    public String register(){
        return "register";
    }

    @PostMapping("/register")
    public String correct_register(@RequestParam String name, @RequestParam String password, @RequestParam String username){
        register_service.add_user(name, password, username);
        return "drawing";
    }
}
