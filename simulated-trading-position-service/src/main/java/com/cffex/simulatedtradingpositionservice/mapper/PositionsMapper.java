package com.cffex.simulatedtradingpositionservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cffex.simulatedtradingmodel.entity.Positions;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
* @author 17204
* @description 针对表【positions(客户持仓表)】的数据库操作Mapper
* @createDate 2024-07-11 16:43:58
* @Entity com.cffex.SimulatedTrading.model.entity.Positions
*/
public interface PositionsMapper extends BaseMapper<Positions> {

    @Select("SELECT * FROM positions WHERE userId = #{userId} AND instrumentId = #{instrumentId} AND type = #{type}")
    Positions getPosition(@Param("userId") Integer userId, @Param("instrumentId") Integer instrumentId, @Param("type") Integer type);
}




