package com.cffex.simulatedtradingserviceclient;

import com.cffex.simulatedtradingmodel.entity.Positions;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "simulated-trading-position-service",path = "/position/inner")
public interface PositionFeignClient {
    @GetMapping("/getPosition")
    Positions getPosition(@RequestParam("orderId") Integer orderId, @RequestParam("instrumentId") Integer instrumentId, @RequestParam("type") Integer type);
    @PostMapping("/save")
    Positions save(@RequestBody Positions positions);
    @PostMapping("/updateById")
    boolean updateById(@RequestBody Positions positions);
}
