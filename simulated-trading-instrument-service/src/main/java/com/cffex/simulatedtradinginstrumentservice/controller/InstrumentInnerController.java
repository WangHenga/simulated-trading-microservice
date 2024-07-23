package com.cffex.simulatedtradinginstrumentservice.controller;

import com.cffex.simulatedtradinginstrumentservice.service.InstrumentService;
import com.cffex.simulatedtradingmodel.entity.Instrument;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;

@RestController
@RequestMapping("/inner")
public class InstrumentInnerController {
    @Resource
    private InstrumentService instrumentService;
    @GetMapping("/getByIdWithCache")
    Instrument getByIdWithCache(@RequestParam("id") Integer id){
        return instrumentService.getByIdWithCache(id);
    }
    @GetMapping("/getById")
    Instrument getById(@RequestParam("id") Integer id){
        return instrumentService.getById(id);
    }
    @GetMapping("/getTransactionPrice")
    BigDecimal getTransactionPrice(@RequestParam("id") Integer id, @RequestParam("firstPrice") String firstPrice, @RequestParam("secondPrice") String secondPrice){
        return instrumentService.getTransactionPrice(id,new BigDecimal(firstPrice), new BigDecimal(secondPrice));
    }
    @PostMapping("/updateLastPriceByIdWithCache")
    void updateLastPriceByIdWithCache(@RequestParam("id") Integer id, @RequestParam("lastPrice") String lastPrice){
        instrumentService.updateLastPriceByIdWithCache(id, new BigDecimal(lastPrice));
    }
}
