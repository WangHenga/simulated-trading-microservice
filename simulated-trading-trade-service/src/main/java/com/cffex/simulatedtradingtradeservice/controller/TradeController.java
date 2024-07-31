package com.cffex.simulatedtradingtradeservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cffex.simulatedtradingmodel.annotation.AuthCheck;
import com.cffex.simulatedtradingmodel.common.BaseResponse;
import com.cffex.simulatedtradingmodel.common.ErrorCode;
import com.cffex.simulatedtradingmodel.common.ResultUtils;
import com.cffex.simulatedtradingmodel.common.ThreadLocalUtil;
import com.cffex.simulatedtradingmodel.dto.trade.TradeDTO;
import com.cffex.simulatedtradingmodel.dto.trade.TradeQueryRequest;
import com.cffex.simulatedtradingmodel.exception.BusinessException;
import com.cffex.simulatedtradingmodel.vo.TradeVO;
import com.cffex.simulatedtradingtradeservice.service.TradesService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class TradeController {
    @Resource
    private TradesService tradesService;
    @PostMapping("/list/page")
    @AuthCheck
    public BaseResponse<Page<TradeVO>> queryTradeList(@RequestBody TradeQueryRequest tradeQueryRequest) {
        Integer userId= ThreadLocalUtil.getUserId();
        if(tradeQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<TradeDTO> pageTrade = tradesService.getPageTrade(tradeQueryRequest.getCurrent(), tradeQueryRequest.getPageSize(), userId);
        return ResultUtils.success(tradesService.getPageTradeVO(pageTrade));
    }
}
