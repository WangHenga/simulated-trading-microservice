package com.cffex.simulatedtradingmodel.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class PositionVO implements Serializable {
    private final static long serialVersionUID = 1L;
    private Integer id;
    private Integer instrumentId;
    private String instrumentName;
    private String typeStr;
    private Integer quantity;
    private Integer remainQuantity;
    private String avePriceStr;
    private String profitLossStr;
}
