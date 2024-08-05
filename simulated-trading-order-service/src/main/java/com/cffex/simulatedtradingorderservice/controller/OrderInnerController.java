package com.cffex.simulatedtradingorderservice.controller;

import com.cffex.simulatedtradingmodel.entity.Orders;
import com.cffex.simulatedtradingorderservice.service.OrdersService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/inner")
public class OrderInnerController {
    @Resource
    private OrdersService ordersService;
    @GetMapping("/getById")
    Orders getById(@RequestParam("id") Integer id){
        return ordersService.getById(id);
    }
    @PostMapping("/updateOrderVolume")
    void updateOrderVolume(@RequestParam("orderId") Integer orderId, @RequestParam("volume") int volume){
        ordersService.updateOrderVolume(orderId, volume);
    }

}
