package com.cffex.simulatedtradinginstrumentservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cffex.simulatedtradingmodel.common.BaseResponse;
import com.cffex.simulatedtradingmodel.common.ErrorCode;
import com.cffex.simulatedtradingmodel.common.ResultUtils;
import com.cffex.simulatedtradingmodel.dto.instrument.InstrumentQueryRequest;
import com.cffex.simulatedtradingmodel.exception.BusinessException;
import com.cffex.simulatedtradingmodel.dto.instrument.InstrumentAddRequest;
import com.cffex.simulatedtradingmodel.dto.instrument.InstrumentUpdateRequest;
import com.cffex.simulatedtradingmodel.entity.Instrument;
import com.cffex.simulatedtradinginstrumentservice.service.InstrumentService;
import com.cffex.simulatedtradingmodel.vo.InstrumentVO;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;

@RestController
public class InstrumentController {
    @Resource
    private InstrumentService instrumentService;
    @PostMapping("/add")
    public BaseResponse<Integer> addInstrument(@RequestBody InstrumentAddRequest request) {
        if(request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Instrument instrument = new Instrument();
        BeanUtils.copyProperties(request, instrument);
        boolean result = instrumentService.save(instrument);
        if(!result){
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return ResultUtils.success(instrument.getId());
    }
    @GetMapping("/delete")
    public BaseResponse<Boolean> deleteInstrument(Integer instrumentId) {
        boolean result = instrumentService.removeById(instrumentId);
        return ResultUtils.success(result);
    }
    @PostMapping("/update")
    public BaseResponse<Boolean> updateInstrument(@RequestBody InstrumentUpdateRequest request) {
        if(request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Instrument instrument = new Instrument();
        BeanUtils.copyProperties(request, instrument);
        instrument.setUpdateTime(new Date());
        boolean result = instrumentService.updateById(instrument);
        return ResultUtils.success(result);
    }
    @GetMapping("/query")
    public BaseResponse<Instrument> queryInstrument(Integer instrumentId) {
        Instrument instrument = instrumentService.getById(instrumentId);
        return ResultUtils.success(instrument);
    }

    @GetMapping("/info")
    public BaseResponse<InstrumentVO> getInstrumentInfo(Integer instrumentId) {
        return ResultUtils.success(instrumentService.getVOById(instrumentId));
    }
    @PostMapping("/list/page")
    public BaseResponse<Page<Instrument>> queryInstrumentList(@RequestBody InstrumentQueryRequest instrumentQueryRequest) {
        if(instrumentQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = instrumentQueryRequest.getCurrent();
        long size = instrumentQueryRequest.getPageSize();
        Page<Instrument> instrumentPage = instrumentService.page(new Page<>(current, size),
                instrumentService.getQueryWrapper(instrumentQueryRequest));
        return ResultUtils.success(instrumentPage);
    }
}
