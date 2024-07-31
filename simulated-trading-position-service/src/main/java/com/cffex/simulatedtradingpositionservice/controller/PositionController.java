package com.cffex.simulatedtradingpositionservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cffex.simulatedtradingmodel.annotation.AuthCheck;
import com.cffex.simulatedtradingmodel.common.BaseResponse;
import com.cffex.simulatedtradingmodel.common.ErrorCode;
import com.cffex.simulatedtradingmodel.common.ResultUtils;
import com.cffex.simulatedtradingmodel.common.ThreadLocalUtil;
import com.cffex.simulatedtradingmodel.dto.positions.PositionQueryRequest;
import com.cffex.simulatedtradingmodel.entity.Positions;
import com.cffex.simulatedtradingmodel.exception.BusinessException;
import com.cffex.simulatedtradingmodel.vo.PositionVO;
import com.cffex.simulatedtradingpositionservice.service.PositionsService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;

@RestController
public class PositionController {
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private PositionsService positionsService;
    @GetMapping("/get")
    public BaseResponse<Positions> getPositions(@RequestParam Integer id){
        Positions position = positionsService.getById(id);
        if(redisTemplate.hasKey("position_" + id)){
            position.setAvePrice(new BigDecimal(redisTemplate.opsForHash().get("position_" + id, "avePrice").toString()).setScale(2, BigDecimal.ROUND_HALF_UP));
            position.setMarginOpe(new BigDecimal(redisTemplate.opsForHash().get("position_" + id, "marginOpe").toString()).setScale(2, BigDecimal.ROUND_HALF_UP));
            position.setQuantity(Integer.parseInt(redisTemplate.opsForHash().get("position_" + id, "quantity").toString()));
            position.setProfitLoss(new BigDecimal(redisTemplate.opsForHash().get("position_" + id, "profitLoss").toString()).setScale(2, BigDecimal.ROUND_HALF_UP));
            positionsService.updateById(position);
        }
        return ResultUtils.success(position);
    }

    @PostMapping("/list/page")
    @AuthCheck
    public BaseResponse<Page<PositionVO>> queryPositionList(@RequestBody PositionQueryRequest positionQueryRequest){
        Integer userId = ThreadLocalUtil.getUserId();
        if(positionQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = positionQueryRequest.getCurrent();
        long size = positionQueryRequest.getPageSize();
        Page<Positions> positionPage = positionsService.page(new Page<>(current, size),
                positionsService.getQueryWrapper(positionQueryRequest,userId));

        return ResultUtils.success(positionsService.getPositionVOPage(positionPage));
    }
}
