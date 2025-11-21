package com.example.premier_league.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class EncrytedPasswordUtils {
    public static void main(String[] args) {
        String password = "123";
        String encrytedPassword = new BCryptPasswordEncoder().encode(password);
        System.out.println("Encryted Password: " + encrytedPassword);
    }
}
