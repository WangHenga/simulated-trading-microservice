package com.cffex.simulatedtradingtradeservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cffex.simulatedtradingmodel.entity.Trades;

/**
* @author 17204
* @description 针对表【trades(成交记录表)】的数据库操作Service
* @createDate 2024-07-15 10:59:46
*/
public interface TradesService extends IService<Trades> {
    void trade(Integer orderId);
}
