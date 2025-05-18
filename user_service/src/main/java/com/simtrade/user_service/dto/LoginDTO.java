package com.simtrade.user_service.dto;

import lombok.Data;

@Data
public class LoginDTO {
    private String email;
    private String password;
}
