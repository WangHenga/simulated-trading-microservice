package com.cffex.simulatedtradingmodel.dto.instrument;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class InstrumentUpdateRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
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
}
