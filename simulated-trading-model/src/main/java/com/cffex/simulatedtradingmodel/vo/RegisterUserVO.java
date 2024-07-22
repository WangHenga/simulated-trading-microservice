package com.cffex.simulatedtradingmodel.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class RegisterUserVO implements Serializable {
    static final long serialVersionUID = 1L;
    private String account;
}
