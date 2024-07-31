package com.cffex.simulatedtradingmodel.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class UserAccountVO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 账号
     */
    private String account;
    /**
     * 可用余额
     */
    private String balance;
    /**
     * 冻结保证金
     */
    private String frozenMargin;

    /**
     * 占用保证金
     */
    private String usedMargin;
}
