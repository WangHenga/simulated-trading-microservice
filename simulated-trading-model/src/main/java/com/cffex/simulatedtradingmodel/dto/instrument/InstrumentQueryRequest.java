package com.cffex.simulatedtradingmodel.dto.instrument;


import com.cffex.simulatedtradingmodel.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class InstrumentQueryRequest extends PageRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 合约名称
     */
    private String name;

    /**
     * 合约代码
     */
    private String symbol;
}
