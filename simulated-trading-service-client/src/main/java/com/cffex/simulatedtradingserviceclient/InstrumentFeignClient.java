package com.cffex.simulatedtradingserviceclient;

import com.cffex.simulatedtradingmodel.entity.Instrument;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "simulated-trading-instrument-service",path = "/instrument/inner")
public interface InstrumentFeignClient {
    @GetMapping("/getByIdWithCache")
    Instrument getByIdWithCache(@RequestParam("id") Integer id);
    @GetMapping("/getById")
    Instrument getById(@RequestParam("id") Integer id);
    @GetMapping("/getTransactionPrice")
    BigDecimal getTransactionPrice(@RequestParam("id") Integer id, @RequestParam("firstPrice") String firstPrice, @RequestParam("secondPrice") String secondPrice);
    @PostMapping("/updateLastPriceByIdWithCache")
    void updateLastPriceByIdWithCache(@RequestParam("id") Integer id, @RequestParam("lastPrice") String lastPrice);
}
