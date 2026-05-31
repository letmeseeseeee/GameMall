package com.gamemall.user;

public class AuthResponse {
    public String token;
    public Long userId;
    public String username;
    public String role;

    public AuthResponse(String token, Long userId, String username, String role) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.role = role;
    }
}
