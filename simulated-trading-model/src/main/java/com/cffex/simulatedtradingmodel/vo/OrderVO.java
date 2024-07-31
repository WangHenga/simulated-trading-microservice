package com.cffex.simulatedtradingmodel.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class OrderVO implements Serializable {
    private final static long serialVersionUID = 1L;

    private Integer id;


    /**
     * 合约名称
     */
    private String instrumentName;


    /**
     * 买卖方向
     */
    private String directionStr;

    /**
     * 组合开平标志
     */
    private String combOffsetStr;

    /**
     * 报价
     */
    private BigDecimal limitPrice;


    /**
     * 成交数量
     */
    private Integer volumeTraded;

    /**
     * 委托数量
     */
    private Integer volumeTotal;
}
