package com.cffex.simulatedtradingtradeservice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cffex.simulatedtradingmodel.dto.trade.TradeDTO;
import com.cffex.simulatedtradingmodel.entity.Trades;
import com.cffex.simulatedtradingmodel.vo.TradeVO;

/**
* @author 17204
* @description 针对表【trades(成交记录表)】的数据库操作Service
* @createDate 2024-07-15 10:59:46
*/
public interface TradesService extends IService<Trades> {
    void trade(Integer orderId);

    Page<TradeDTO> getPageTrade(long current, long pageSize, int userId);

    Page<TradeVO> getPageTradeVO(Page<TradeDTO> pageTrade);
}
