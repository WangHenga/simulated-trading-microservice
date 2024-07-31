package com.cffex.simulatedtradingpositionservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cffex.simulatedtradingmodel.constant.CommonConstant;
import com.cffex.simulatedtradingmodel.dto.positions.PositionQueryRequest;
import com.cffex.simulatedtradingmodel.entity.Instrument;
import com.cffex.simulatedtradingmodel.enums.DirectionEnum;
import com.cffex.simulatedtradingmodel.utils.SqlUtils;
import com.cffex.simulatedtradingmodel.vo.PositionVO;
import com.cffex.simulatedtradingpositionservice.mapper.PositionsMapper;
import com.cffex.simulatedtradingmodel.entity.Positions;
import com.cffex.simulatedtradingpositionservice.service.PositionsService;
import com.cffex.simulatedtradingserviceclient.InstrumentFeignClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

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
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private InstrumentFeignClient instrumentFeignClient;
    @Override
    public Positions getPosition(Integer userId, Integer instrumentId, Integer type) {
        return positionsMapper.getPosition(userId, instrumentId, type);
    }

    @Override
    public QueryWrapper<Positions> getQueryWrapper(PositionQueryRequest positionQueryRequest, Integer userId) {
        QueryWrapper<Positions> queryWrapper = new QueryWrapper<>();
        if (positionQueryRequest == null|| userId==null) {
            return queryWrapper;
        }
        String sortField = positionQueryRequest.getSortField();
        String sortOrder = positionQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.ne("quantity",0);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public Page<PositionVO> getPositionVOPage(Page<Positions> positionsPage) {
        List<Positions> positionList = positionsPage.getRecords();
        Page<PositionVO> positionVOPage = new Page<>(positionsPage.getCurrent(), positionsPage.getSize(), positionsPage.getTotal());
        if(positionList == null || positionList.size() == 0) {
            return positionVOPage;
        }
        List<PositionVO> positionVOList=new ArrayList<>(positionList.size());
        for(Positions position : positionList) {
            PositionVO positionVO = new PositionVO();
            BeanUtils.copyProperties(position, positionVO);
            positionVO.setAvePriceStr(position.getAvePrice().toString());
            positionVO.setTypeStr(position.getType().equals(DirectionEnum.CALL.getCode())?"多": "空");
            Instrument instrument = instrumentFeignClient.getById(position.getInstrumentId());
            positionVO.setInstrumentName(instrument.getName());
            Integer remainVolume = Integer.parseInt(redisTemplate.opsForHash().get("position_" + position.getId(), "remainVolume").toString());
            positionVO.setRemainQuantity(remainVolume);
            BigDecimal avePrice = position.getAvePrice();
            Integer quantity = position.getQuantity();
            BigDecimal lastPrice = instrument.getLastPrice();
            BigDecimal multiplier = instrument.getMultiplier();
            if(position.getType().equals(DirectionEnum.CALL.getCode())) {
                positionVO.setProfitLossStr(lastPrice.subtract(avePrice).multiply(new BigDecimal(quantity)).multiply(multiplier).setScale(2, RoundingMode.HALF_UP).toString());
            }else{
                positionVO.setProfitLossStr(avePrice.subtract(lastPrice).multiply(new BigDecimal(quantity)).multiply(multiplier).setScale(2, RoundingMode.HALF_UP).toString());
            }
            positionVOList.add(positionVO);
        }
        positionVOPage.setRecords(positionVOList);
        return positionVOPage;
    }

}




