package com.cffex.simulatedtradingorderservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cffex.simulatedtradingmodel.annotation.AuthCheck;
import com.cffex.simulatedtradingmodel.common.BaseResponse;
import com.cffex.simulatedtradingmodel.common.ErrorCode;
import com.cffex.simulatedtradingmodel.common.ResultUtils;
import com.cffex.simulatedtradingmodel.common.ThreadLocalUtil;
import com.cffex.simulatedtradingmodel.dto.orders.OrderCreateRequest;
import com.cffex.simulatedtradingmodel.dto.orders.OrderQueryRequest;
import com.cffex.simulatedtradingmodel.entity.Orders;
import com.cffex.simulatedtradingmodel.exception.BusinessException;
import com.cffex.simulatedtradingmodel.vo.OrderVO;
import com.cffex.simulatedtradingorderservice.mq.MessageProducer;
import com.cffex.simulatedtradingorderservice.service.OrdersService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class OrdersController {
    @Resource
    private OrdersService ordersService;
    @Resource
    private MessageProducer messageProducer;
    /**
     * 创建订单
     *
     * @param request 订单创建请求，包含订单信息
     * @return 创建成功后的订单ID封装在BaseResponse对象中
     * @throws BusinessException 如果订单验证不通过或保存失败，则抛出BusinessException异常
     * @PostMapping("/create") 定义HTTP POST请求映射到该方法
     * @AuthCheck 标记该方法需要进行权限检查
     */
    @PostMapping("/create")
    @AuthCheck
    public BaseResponse<Integer> createOrder(@RequestBody OrderCreateRequest request) {
        Integer userId = ThreadLocalUtil.getUserId();
        Orders orders = new Orders();
        BeanUtils.copyProperties(request, orders);
        orders.setUserId(userId);
        Integer positionId=ordersService.validate(orders);
        if(positionId<0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        orders.setPositionId(positionId);
        boolean result = ordersService.save(orders);
        if(!result){
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        messageProducer.sendMessage(orders.getId().toString());
        return ResultUtils.success(orders.getId());
    }
    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @return BaseResponse<Boolean> 包含操作结果的响应对象
     * @throws BusinessException 当订单不存在、用户没有权限取消订单、订单状态不允许取消时抛出该异常
     */
    @PostMapping("/cancel")
    @AuthCheck
    public BaseResponse<Boolean> cancelOrder(Integer orderId) {
        boolean result=ordersService.cancelOrder(orderId);
        return ResultUtils.success(result);
    }

    @PostMapping("/list/page")
    @AuthCheck
    public BaseResponse<Page<OrderVO>> queryOrderList(@RequestBody OrderQueryRequest orderQueryRequest) {
        Integer userId = ThreadLocalUtil.getUserId();
        if(orderQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = orderQueryRequest.getCurrent();
        long size = orderQueryRequest.getPageSize();
        Page<Orders> orderPage = ordersService.page(new Page<>(current, size),
                ordersService.getQueryWrapper(orderQueryRequest,userId));

        return ResultUtils.success(ordersService.getOrderVOPage(orderPage));
    }
}
