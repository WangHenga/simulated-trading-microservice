package com.cffex.simulatedtradingpositionservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cffex.simulatedtradingmodel.dto.positions.PositionQueryRequest;
import com.cffex.simulatedtradingmodel.entity.Positions;
import com.cffex.simulatedtradingmodel.vo.PositionVO;

/**
* @author 17204
* @description 针对表【positions(客户持仓表)】的数据库操作Service
* @createDate 2024-07-11 16:43:58
*/
public interface PositionsService extends IService<Positions> {

    Positions getPosition(Integer userId, Integer instrumentId, Integer type);

    QueryWrapper<Positions> getQueryWrapper(PositionQueryRequest positionQueryRequest, Integer userId);

    Page<PositionVO> getPositionVOPage(Page<Positions> positionsPage);

}
