package com.cffex.simulatedtradingpositionservice.controller;

import com.cffex.simulatedtradingmodel.entity.Positions;
import com.cffex.simulatedtradingpositionservice.service.PositionsService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/inner")
public class PositionInnerController {
    @Resource
    private PositionsService positionsService;
    @GetMapping("/getPosition")
    Positions getPosition(@RequestParam("orderId") Integer orderId, @RequestParam("instrumentId") Integer instrumentId, @RequestParam("type") Integer type){
        return positionsService.getPosition(orderId, instrumentId, type);
    }
    @PostMapping("/save")
    Positions save(@RequestBody Positions positions){
        boolean result = positionsService.save(positions);
        if(result){
            return positions;
        }else{
            return null;
        }
    }
    @PostMapping("/updateById")
    boolean updateById(@RequestBody Positions positions){
        return positionsService.updateById(positions);
    }
}
