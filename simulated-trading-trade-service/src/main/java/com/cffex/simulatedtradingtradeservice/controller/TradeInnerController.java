package com.cffex.simulatedtradingtradeservice.controller;

import com.cffex.simulatedtradingmodel.entity.Trades;
import com.cffex.simulatedtradingtradeservice.service.TradesService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/inner")
public class TradeInnerController {
    @Resource
    private TradesService tradesService;
    @PostMapping("/save")
    Trades save(@RequestBody Trades trades){
        boolean result = tradesService.save(trades);
        if(result){
            return trades;
        }else{
            return null;
        }
    }
}
