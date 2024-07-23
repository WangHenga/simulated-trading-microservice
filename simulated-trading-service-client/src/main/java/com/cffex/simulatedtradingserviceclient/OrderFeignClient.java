package com.cffex.simulatedtradingserviceclient;

import com.cffex.simulatedtradingmodel.entity.Orders;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "simulated-trading-order-service",path = "/order/inner")
public interface OrderFeignClient {
    @GetMapping("/getById")
    Orders getById(@RequestParam("id") Integer id);
    @PostMapping ("/updateOrderVolume")
    void updateOrderVolume(@RequestParam("orderId") Integer orderId, @RequestParam("volume") int volume);
}
