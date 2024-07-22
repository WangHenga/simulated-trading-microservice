package com.cffex.simulatedtradinginstrumentservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cffex.simulatedtradinginstrumentservice.mapper.InstrumentMapper;
import com.cffex.simulatedtradingmodel.entity.Instrument;
import com.cffex.simulatedtradinginstrumentservice.service.InstrumentService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;

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




