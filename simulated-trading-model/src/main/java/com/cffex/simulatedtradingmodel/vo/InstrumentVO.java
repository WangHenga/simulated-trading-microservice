package com.cffex.simulatedtradingmodel.vo;

import lombok.Data;

import java.io.Serializable;
@Data
public class InstrumentVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String name;
    private String symbol;
    private String lastPriceStr;
    private String buyPriceStr;
    private String sellPriceStr;
    private Integer buyVolume;
    private Integer sellVolume;
    private String maxPrice;
    private String minPrice;
    private String minPriceChangeStr;
}
