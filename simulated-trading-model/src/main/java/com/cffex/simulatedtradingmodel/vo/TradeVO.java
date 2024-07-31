package com.cffex.simulatedtradingmodel.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class TradeVO implements Serializable {
    private final static long serialVersionUID = 1L;
    private String instrumentName;
    private String directionStr;
    private String combOffsetStr;
    private String priceStr;
    private Integer volume;
    private String createTimeStr;
}
