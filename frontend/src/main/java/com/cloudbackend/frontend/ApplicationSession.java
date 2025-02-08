package com.cloudbackend.frontend;

public class ApplicationSession {

    private static String jwtToken;
    private  static  String username;

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        ApplicationSession.username = username;
    }

    public static String getJwtToken() {
        return jwtToken;
    }

    public static void setJwtToken(String token){
        jwtToken = token;
    }


    public static void clear() {
        jwtToken = "";
        username = "";
    }
}
