package com.epam.utils;

import com.epam.domain.User;
import org.springframework.stereotype.Service;

@Service
public class CredentialsService {


    public boolean checkCredentials(User user, String password) {
        if (user == null || password == null) {
            return false;
        }
        return user.getPassword().equals(password);
    }
}