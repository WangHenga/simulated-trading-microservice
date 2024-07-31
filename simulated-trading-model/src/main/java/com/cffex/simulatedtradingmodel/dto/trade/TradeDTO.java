package com.cffex.simulatedtradingmodel.dto.trade;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class TradeDTO implements Serializable {
    private final static long serialVersionUID = 1L;
    private String instrumentName;
    private Integer direction;
    private Integer combOffsetFlag;
    private BigDecimal price;
    private Integer volume;
    private Date createTime;
}
