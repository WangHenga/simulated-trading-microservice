package com.cffex.simulatedtradingmodel.dto.trade;

import com.cffex.simulatedtradingmodel.common.PageRequest;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
@EqualsAndHashCode(callSuper = true)
public class TradeQueryRequest extends PageRequest implements Serializable {
    private static final long serialVersionUID = 1L;
}
