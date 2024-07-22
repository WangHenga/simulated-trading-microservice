package com.cffex.simulatedtradingmodel.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoginUserVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String account;
    private String token;
}
