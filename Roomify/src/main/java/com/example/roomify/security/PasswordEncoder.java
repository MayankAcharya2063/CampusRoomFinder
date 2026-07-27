package com.example.roomify.security;

import java.util.Base64;

public class PasswordEncoder {

    public static String encode(String password){

        return Base64.getEncoder()
                .encodeToString(password.getBytes());

    }

    public static boolean matches(String raw,String encoded){

        return encode(raw).equals(encoded);

    }

}
