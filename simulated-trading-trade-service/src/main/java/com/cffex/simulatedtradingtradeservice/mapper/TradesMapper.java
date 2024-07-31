package com.cffex.simulatedtradingtradeservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cffex.simulatedtradingmodel.dto.trade.TradeDTO;
import com.cffex.simulatedtradingmodel.entity.Trades;
import org.apache.ibatis.annotations.Param;

/**
* @author 17204
* @description 针对表【trades(成交记录表)】的数据库操作Mapper
* @createDate 2024-07-15 10:59:46
* @Entity com.cffex.SimulatedTrading.model.entity.Trades
*/
public interface TradesMapper extends BaseMapper<Trades> {
    Page<TradeDTO> selectPageTrade(@Param("page") Page<?> page, @Param("userId") Integer userId);
}




