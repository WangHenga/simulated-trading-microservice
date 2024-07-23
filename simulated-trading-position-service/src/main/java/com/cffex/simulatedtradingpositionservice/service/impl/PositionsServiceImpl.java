package com.cffex.simulatedtradingpositionservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cffex.simulatedtradingpositionservice.mapper.PositionsMapper;
import com.cffex.simulatedtradingmodel.entity.Positions;
import com.cffex.simulatedtradingpositionservice.service.PositionsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
* @author 17204
* @description 针对表【positions(客户持仓表)】的数据库操作Service实现
* @createDate 2024-07-11 16:43:58
*/
@Service
public class PositionsServiceImpl extends ServiceImpl<PositionsMapper, Positions>
    implements PositionsService{
    @Resource
    private PositionsMapper positionsMapper;
    @Override
    public Positions getPosition(Integer userId, Integer instrumentId, Integer type) {
        return positionsMapper.getPosition(userId, instrumentId, type);
    }

}




