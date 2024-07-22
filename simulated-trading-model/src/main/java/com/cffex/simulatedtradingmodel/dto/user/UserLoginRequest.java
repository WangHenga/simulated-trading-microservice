package com.cffex.simulatedtradingmodel.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLoginRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String account;
    private String password;
}
