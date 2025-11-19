package com.drawings.drawings.service;

import com.drawings.drawings.security.JBCryptHasher;
import com.drawings.drawings.dao.user_dao;
import com.drawings.drawings.model.user;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class login_service {
    @Autowired
    private user_dao user_dao;
    @Autowired
    private JBCryptHasher hasher;


    public boolean check_user(String username, String password){
        user user = user_dao.find_user(username);
        if(user == null){
            return false;
        }
        String user_password = user.getPassword();
        if(hasher.verifyPassword(password,user_password)){
            return true;
        }
        return false;
    }
}
