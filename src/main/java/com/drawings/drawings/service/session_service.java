package com.drawings.drawings.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class session_service {

    public boolean is_user_logged_in(HttpSession session){
        return session.getAttribute("username") != null;
    }
}
