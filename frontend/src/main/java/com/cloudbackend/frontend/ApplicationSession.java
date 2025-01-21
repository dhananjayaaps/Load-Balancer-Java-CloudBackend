package com.cloudbackend.frontend;

public class ApplicationSession {

    private static String jwtToken;

    public static String getJwtToken() {
        return jwtToken;
    }

    public static void setJwtToken(String token) {
        jwtToken = token;
    }
}
