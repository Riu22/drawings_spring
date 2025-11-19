package com.drawings.drawings.service;

import com.drawings.drawings.dao.user_dao;
import com.drawings.drawings.model.user;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class login_service {
    @Autowired
    private user_dao user_dao;

    public boolean check_user(String username, String password){
       for(user user: user_dao.findAll())
           if(user.getUsername().equals(username) && user.getPassword().equals(password))
               return true;
       return false;
    }
}
