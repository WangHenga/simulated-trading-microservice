package com.cffex.simulatedtradingmodel.dto.user;

import lombok.Data;

@Data
public class UserRegisterRequest {
    private static final long serialVersionUID = 1L;
    private String account;
    private String password;
    private String checkPassword;
}
