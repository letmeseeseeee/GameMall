package com.gamemall.user;

import javax.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank
    public String username;
    @NotBlank
    public String password;
}
