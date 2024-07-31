package com.cffex.simulatedtradinginstrumentservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cffex.simulatedtradinginstrumentservice.mapper.InstrumentMapper;
import com.cffex.simulatedtradingmodel.constant.CommonConstant;
import com.cffex.simulatedtradingmodel.dto.instrument.InstrumentQueryRequest;
import com.cffex.simulatedtradingmodel.entity.Instrument;
import com.cffex.simulatedtradinginstrumentservice.service.InstrumentService;
import com.cffex.simulatedtradingmodel.enums.InstrumentStateEnum;
import com.cffex.simulatedtradingmodel.utils.SqlUtils;
import com.cffex.simulatedtradingmodel.vo.InstrumentVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Set;

/**
* @author 17204
* @description 针对表【instrument(合约信息表)】的数据库操作Service实现
* @createDate 2024-07-09 16:11:06
*/
@Service
public class InstrumentServiceImpl extends ServiceImpl<InstrumentMapper, Instrument>
    implements InstrumentService{
    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public BigDecimal getTransactionPrice(Integer instrumentId, BigDecimal price1, BigDecimal price2) {
        BigDecimal buyPrice;
        BigDecimal sellPrice;
        if(price1.compareTo(price2) < 0){
            buyPrice=price2;
            sellPrice=price1;
        }else{
            buyPrice=price1;
            sellPrice=price2;
        }
        if(!redisTemplate.hasKey("instrument_"+instrumentId)) {
            this.getByIdWithCache(instrumentId);
        }
        BigDecimal lastPrice=new BigDecimal(redisTemplate.opsForHash().get("instrument_"+instrumentId, "lastPrice").toString());
        if(buyPrice.compareTo(lastPrice) < 0){
            return buyPrice;
        }else if(sellPrice.compareTo(lastPrice) > 0){
            return sellPrice;
        }else{
            return lastPrice;
        }
    }

    @Override
    public Instrument getByIdWithCache(Integer instrumentId) {
        if(redisTemplate.hasKey("instrument_"+instrumentId)) {
            Instrument instrument = new Instrument();
            instrument.setId(instrumentId);
            instrument.setLastPrice(new BigDecimal(redisTemplate.opsForHash().get("instrument_"+instrumentId, "lastPrice").toString()));
            instrument.setMinPriceChange(new BigDecimal(redisTemplate.opsForHash().get("instrument_"+instrumentId, "minPriceChange").toString()));
            instrument.setMinMarginRate(new BigDecimal(redisTemplate.opsForHash().get("instrument_"+instrumentId, "minMarginRate").toString()));
            instrument.setMaxDailyPriceFluctuation(new BigDecimal(redisTemplate.opsForHash().get("instrument_"+instrumentId, "maxDailyPriceFluctuation").toString()));
            instrument.setMultiplier(new BigDecimal(redisTemplate.opsForHash().get("instrument_"+instrumentId, "multiplier").toString()));
            instrument.setSettlementPrice(new BigDecimal(redisTemplate.opsForHash().get("instrument_"+instrumentId, "settlementPrice").toString()));
            instrument.setState(Integer.parseInt(redisTemplate.opsForHash().get("instrument_"+instrumentId, "state").toString()));
            return instrument;
        }else{
            Instrument instrument = this.getById(instrumentId);
            this.saveInCache(instrument);
            return instrument;
        }
    }

    @Override
    public void updateLastPriceByIdWithCache(Integer instrumentId, BigDecimal transactionPrice) {
        redisTemplate.opsForHash().put("instrument_"+instrumentId, "lastPrice", transactionPrice.toString());
        Instrument instrument = new Instrument();
        instrument.setId(instrumentId);
        instrument.setLastPrice(transactionPrice);
        this.updateById(instrument);
    }

    @Override
    public QueryWrapper<Instrument> getQueryWrapper(InstrumentQueryRequest instrumentQueryRequest) {
        QueryWrapper<Instrument> queryWrapper = new QueryWrapper<>();
        if (instrumentQueryRequest == null) {
            return queryWrapper;
        }
        String name = instrumentQueryRequest.getName();
        String symbol = instrumentQueryRequest.getSymbol();
        String sortField = instrumentQueryRequest.getSortField();
        String sortOrder = instrumentQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.like(StringUtils.isNotBlank(symbol), "symbol", symbol);
        queryWrapper.eq("isDelete", false);
        queryWrapper.eq("state", InstrumentStateEnum.LISTED.getCode());
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public InstrumentVO getVOById(Integer instrumentId) {
        Instrument instrument = this.getById(instrumentId);
        InstrumentVO instrumentVO = new InstrumentVO();
        BeanUtils.copyProperties(instrument, instrumentVO);
        instrumentVO.setLastPriceStr(instrument.getLastPrice().toString());
        instrumentVO.setMinPriceChangeStr(instrument.getMinPriceChange().toString());
        // TODO 计算买卖价格以及涨跌停价格
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Set<ZSetOperations.TypedTuple<String>> bestPriceOrderSet = zSetOperations.rangeWithScores(instrumentId + "_0",0,0);
        if(bestPriceOrderSet==null||bestPriceOrderSet.isEmpty()){
            instrumentVO.setBuyPriceStr(instrument.getLastPrice().toString());
            instrumentVO.setBuyVolume(0);
        }else {
            ZSetOperations.TypedTuple<String> bestPriceOrder = bestPriceOrderSet.iterator().next();
            long score = new BigDecimal(bestPriceOrder.getScore()).longValue();
            BigDecimal price=new BigDecimal((score>>32)+"").divide(new BigDecimal("100")).setScale(2,BigDecimal.ROUND_HALF_UP).abs();
            instrumentVO.setBuyPriceStr(price.toString());
            Set<String> buySet = zSetOperations.range(instrumentId + "_0", 0, -1);
            Integer buyVolume = 0;
            for(String orderId:buySet){
                Object value = valueOperations.get("order_" + orderId);
                if(value!=null){
                    buyVolume+=Integer.parseInt(value.toString());
                }
            }
            instrumentVO.setBuyVolume(buyVolume);
        }
        bestPriceOrderSet = zSetOperations.rangeWithScores(instrumentId + "_1",0,0);
        if(bestPriceOrderSet==null||bestPriceOrderSet.isEmpty()){
            instrumentVO.setSellPriceStr(instrument.getLastPrice().toString());
            instrumentVO.setSellVolume(0);
        }else {
            ZSetOperations.TypedTuple<String> bestPriceOrder = bestPriceOrderSet.iterator().next();
            long score = new BigDecimal(bestPriceOrder.getScore()).longValue();
            BigDecimal price=new BigDecimal((score>>32)+"").divide(new BigDecimal("100")).setScale(2,BigDecimal.ROUND_HALF_UP).abs();
            instrumentVO.setSellPriceStr(price.toString());
            Set<String> sellSet = zSetOperations.range(instrumentId + "_1", 0, -1);
            Integer sellVolume = 0;
            for(String orderId:sellSet){
                Object value = valueOperations.get("order_" + orderId);
                if(value!=null){
                    sellVolume+=Integer.parseInt(value.toString());
                }
            }
            instrumentVO.setSellVolume(sellVolume);
        }
        BigDecimal settlementPrice = instrument.getSettlementPrice();
        BigDecimal maxDailyPriceFluctuation = instrument.getMaxDailyPriceFluctuation();
        BigDecimal maxPrice=settlementPrice.multiply(maxDailyPriceFluctuation.divide(new BigDecimal("100"),2, RoundingMode.HALF_UP).add(BigDecimal.ONE));
        maxPrice = maxPrice.setScale(2, RoundingMode.HALF_UP);
        BigDecimal minPrice=settlementPrice.multiply(BigDecimal.ONE.subtract(maxDailyPriceFluctuation.divide(new BigDecimal("100"),2,RoundingMode.HALF_UP)));
        minPrice = minPrice.setScale(2, RoundingMode.HALF_UP);
        instrumentVO.setMinPrice(minPrice.toString());
        instrumentVO.setMaxPrice(maxPrice.toString());
        return instrumentVO;
    }

    private void saveInCache(Instrument instrument) {
        String luaScript = "redis.call('hset',KEYS[1],'lastPrice',ARGV[1])\n"
                + "redis.call('hset',KEYS[1],'minPriceChange',ARGV[2])\n"
                + "redis.call('hset',KEYS[1],'minMarginRate',ARGV[3])\n"
                + "redis.call('hset',KEYS[1],'maxDailyPriceFluctuation',ARGV[4])\n"
                + "redis.call('hset',KEYS[1],'multiplier',ARGV[5])\n"
                + "redis.call('hset',KEYS[1],'settlementPrice',ARGV[6])\n"
                + "redis.call('hset',KEYS[1],'state',ARGV[7])\n"
                + "return 1";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript, Long.class);
        redisTemplate.execute(redisScript, Collections.singletonList("instrument_" + instrument.getId()),
                instrument.getLastPrice().toString(), instrument.getMinPriceChange().toString(), instrument.getMinMarginRate().toString()
                , instrument.getMaxDailyPriceFluctuation().toString(),instrument.getMultiplier().toString(),instrument.getSettlementPrice().toString(),instrument.getState().toString());

    }
}




