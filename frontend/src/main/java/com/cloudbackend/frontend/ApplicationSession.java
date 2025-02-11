package com.cloudbackend.frontend;

public class ApplicationSession {

    private static String jwtToken;
    private  static  String username;
    private static String role;

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

    public static String getRole() {
        return role;
    }

    public static void setRole(String role) {
        ApplicationSession.role = role;
    }


    public static void clear() {
        jwtToken = "";
        username = "";
        role = "";
    }
}
