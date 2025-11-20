package com.drawings.drawings.service;

import com.drawings.drawings.dao.user_dao;
import com.drawings.drawings.model.user;
import com.drawings.drawings.security.JBCryptHasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
public class register_service {
    @Autowired
    private user_dao user_dao;
    @Autowired
    private JBCryptHasher hasher;



    public void add_user(String name, String password, String username){
        password = hasher.hashPassword(password);
        user user = new user(name, password, username);
        try {
            user_dao.add_user(user);
        }catch (DuplicateKeyException e){
            throw new user_exists_exception("Username already exists");
        }
    }


}
 class user_exists_exception extends RuntimeException {
    public user_exists_exception(String message) {
        super(message);
    }
}
