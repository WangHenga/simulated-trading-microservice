package com.cffex.simulatedtradingorderservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cffex.simulatedtradingmodel.entity.Orders;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
* @author 17204
* @description 针对表【orders(订单信息表)】的数据库操作Mapper
* @createDate 2024-07-10 10:15:50
* @Entity com.cffex.SimulatedTrading.model.entity.Orders
*/
public interface OrdersMapper extends BaseMapper<Orders> {
    @Select("SELECT * FROM orders WHERE userId = #{userId} AND instrumentId = #{instrumentId} AND direction = #{direction} " +
            "AND orderStatus != 3 AND combOffsetFlag = 1")
    List<Orders> getExistCloseOrders(@Param("userId") Integer userId, @Param("instrumentId") Integer instrumentId, @Param("direction")Integer direction);

    @Update("UPDATE orders SET volumeTotal = volumeTotal - #{volume}, volumeTraded = volumeTraded + #{volume}, orderStatus = 1 WHERE id = #{id}")
    void updateOrderStatusAndVolumes(@Param("id") int id, @Param("volume") int volume);

    @Update("UPDATE orders SET orderStatus = 2 WHERE id = #{id} AND volumeTotal = 0")
    void updateOrderToCompleteIfZero(@Param("id") int id);
}




