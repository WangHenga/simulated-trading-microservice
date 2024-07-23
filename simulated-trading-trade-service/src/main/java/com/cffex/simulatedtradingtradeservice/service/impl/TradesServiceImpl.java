package com.cffex.simulatedtradingtradeservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cffex.simulatedtradingtradeservice.mapper.TradesMapper;
import com.cffex.simulatedtradingmodel.entity.Trades;
import com.cffex.simulatedtradingtradeservice.service.TradesService;
import org.springframework.stereotype.Service;

/**
* @author 17204
* @description 针对表【trades(成交记录表)】的数据库操作Service实现
* @createDate 2024-07-15 10:59:46
*/
@Service
public class TradesServiceImpl extends ServiceImpl<TradesMapper, Trades>
    implements TradesService{

}




