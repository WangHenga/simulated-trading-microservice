package com.cffex.simulatedtradingmodel.dto.orders;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
@Data
public class OrderCreateRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 合约id
     */
    private Integer instrumentId;

    /**
     * 买卖方向
     */
    private Integer direction;

    /**
     * 组合开平标志
     */
    private Integer combOffsetFlag;

    /**
     * 报价
     */
    private BigDecimal limitPrice;
    /**
     * 委托数量
     */
    private Integer volumeTotal;

}
