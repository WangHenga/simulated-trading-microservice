package com.cffex.simulatedtradingserviceclient;

import com.cffex.simulatedtradingmodel.entity.Trades;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "simulated-trading-trade-service",path = "/trade/inner")
public interface TradeFeignClient {
    @PostMapping("/save")
    Trades save(@RequestBody Trades trades);
    @PostMapping("/trade")
    void trade(@RequestParam("orderId") Integer orderId);
}
