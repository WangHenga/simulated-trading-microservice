package com.cffex.simulatedtradinginstrumentservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cffex.simulatedtradingmodel.entity.Instrument;

import java.math.BigDecimal;

/**
* @author 17204
* @description 针对表【instrument(合约信息表)】的数据库操作Service
* @createDate 2024-07-09 16:11:06
*/
public interface InstrumentService extends IService<Instrument> {
    BigDecimal getTransactionPrice(Integer instrumentId,BigDecimal price1, BigDecimal price2);

    Instrument getByIdWithCache(Integer instrumentId);

    void updateLastPriceByIdWithCache(Integer instrumentId, BigDecimal transactionPrice);
}
