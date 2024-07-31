package com.cffex.simulatedtradingmodel.dto.positions;

import com.cffex.simulatedtradingmodel.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class PositionQueryRequest extends PageRequest implements Serializable {
    private final static long serialVersionUID = 1L;
}
