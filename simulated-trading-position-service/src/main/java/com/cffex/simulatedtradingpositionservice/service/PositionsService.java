package com.cffex.simulatedtradingpositionservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cffex.simulatedtradingmodel.entity.Positions;

/**
* @author 17204
* @description 针对表【positions(客户持仓表)】的数据库操作Service
* @createDate 2024-07-11 16:43:58
*/
public interface PositionsService extends IService<Positions> {

    Positions getPosition(Integer userId, Integer instrumentId, Integer type);


}
