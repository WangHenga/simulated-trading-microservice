package com.cffex.simulatedtradingmodel.dto.instrument;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class InstrumentAddRequest implements Serializable {
    /**
     * 合约名称
     */
    private String name;

    /**
     * 合约代码
     */
    private String symbol;

    /**
     * 合约标的
     */
    private String subject;

    /**
     * 合约报价单位
     */
    private String quoteUnit;

    /**
     * 最小变动价位
     */
    private BigDecimal minPriceChange;

    /**
     * 最大变动幅度
     */
    private BigDecimal maxDailyPriceFluctuation;

    /**
     * 最小保证金率
     */
    private BigDecimal minMarginRate;

    /**
     * 合约乘数
     */
    private BigDecimal multiplier;

    /**
     * 合约状态
     */
    private Integer state;

    /**
     * 最新成交价
     */
    private BigDecimal lastPrice;

    /**
     * 结算价
     */
    private BigDecimal settlementPrice;

    private static final long serialVersionUID = 1L;
}
