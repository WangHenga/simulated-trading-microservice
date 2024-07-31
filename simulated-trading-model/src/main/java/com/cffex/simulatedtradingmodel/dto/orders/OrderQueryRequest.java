package com.cffex.simulatedtradingmodel.dto.orders;


import com.cffex.simulatedtradingmodel.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class OrderQueryRequest extends PageRequest implements Serializable {
    private static final long serialVersionUID = 1L;
}
