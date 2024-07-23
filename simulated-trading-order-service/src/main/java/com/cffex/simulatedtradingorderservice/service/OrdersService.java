package com.cffex.simulatedtradingorderservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cffex.simulatedtradingmodel.entity.Orders;

/**
* @author 17204
* @description 针对表【orders(订单信息表)】的数据库操作Service
* @createDate 2024-07-10 10:15:50
*/
public interface OrdersService extends IService<Orders> {

    Integer validate(Orders orders);

    Boolean cancelOrder(Integer orderId);

    boolean validClose(Integer positionId,Integer volume);
    void updateOrderVolume(Integer orderId, int volume);
}
