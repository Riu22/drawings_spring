package com.drawings.drawings.security;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class JBCryptHasher {


    private static final int COST_FACTOR = 12;

    public String hashPassword(String password) {
        String salt = BCrypt.gensalt(COST_FACTOR);
        return BCrypt.hashpw(password, salt);
    }

    public boolean verifyPassword(String candidatePassword, String storedHash) {
        return BCrypt.checkpw(candidatePassword, storedHash);
    }
}