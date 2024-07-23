package com.cffex.simulatedtradingtradeservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cffex.simulatedtradingmodel.entity.*;
import com.cffex.simulatedtradingmodel.enums.CombOffsetEnum;
import com.cffex.simulatedtradingmodel.enums.DirectionEnum;
import com.cffex.simulatedtradingserviceclient.*;
import com.cffex.simulatedtradingtradeservice.mapper.TradesMapper;
import com.cffex.simulatedtradingtradeservice.service.TradesService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
* @author 17204
* @description 针对表【trades(成交记录表)】的数据库操作Service实现
* @createDate 2024-07-15 10:59:46
*/
@Service
public class TradesServiceImpl extends ServiceImpl<TradesMapper, Trades>
    implements TradesService{
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private UserFeignClient userFeignClient;
    @Resource
    private OrderFeignClient orderFeignClient;
    @Resource
    private TradeFeignClient tradeFeignClient;
    @Resource
    private InstrumentFeignClient instrumentFeignClient;
    @Resource
    private PositionFeignClient positionFeignClient;
    @Override
    public void trade(Integer orderId) {
        Orders order = orderFeignClient.getById(orderId);
        Integer direction = order.getDirection();
        Integer instrumentId = order.getInstrumentId();
        Integer volumeTotal = order.getVolumeTotal();
        Integer volumeTraded = order.getVolumeTraded();
        BigDecimal limitPrice = order.getLimitPrice();
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String key = instrumentId.toString() + "_" + (1 ^ direction);
        while(volumeTotal>0){
            Set<ZSetOperations.TypedTuple<String>> bestPriceOrderSet = zSetOperations.rangeWithScores(key,0,0);
            if(bestPriceOrderSet==null||bestPriceOrderSet.isEmpty()){
                break;
            }
            ZSetOperations.TypedTuple<String> bestPriceOrder = bestPriceOrderSet.iterator().next();
            long score = new BigDecimal(bestPriceOrder.getScore()).longValue();
            Integer listedOrderId = Integer.parseInt(bestPriceOrder.getValue());
            BigDecimal price=new BigDecimal((score>>32)+"").divide(new BigDecimal("100")).setScale(2,BigDecimal.ROUND_HALF_UP).abs();
            if(direction.equals(DirectionEnum.CALL.getCode())){
                if(price.compareTo(limitPrice)>0){
                    break;
                }
            }else if(direction.equals(DirectionEnum.PUT.getCode())){
                if(price.compareTo(limitPrice)<0){
                    break;
                }
            }
            Integer listedOrderVolume = Integer.parseInt(valueOperations.get("order_"+ listedOrderId).toString());
            if(listedOrderVolume==null){
                continue;
            }
            int volume=Math.min(volumeTotal,listedOrderVolume);
            int decrement=this.decrement(listedOrderId,volume);
            if("0".equals(valueOperations.get("order_"+listedOrderId.toString()))){
                zSetOperations.remove(key,listedOrderId.toString());
                redisTemplate.delete("order_"+listedOrderId);
            }
            if(decrement>0){
                volumeTotal-=decrement;
                volumeTraded+=decrement;
                matchMaking(orderId,listedOrderId,volume);
            }
        }
        Integer remain = volumeTotal;
        if(remain>0){
            // 订单有剩余加入队列
            long score=this.getScore(limitPrice,direction);
            zSetOperations.add(instrumentId.toString() + "_" + direction,orderId.toString(),score);
            valueOperations.set("order_"+orderId.toString(),remain.toString());
        }
    }

    private void matchMaking(Integer firstOrderId,Integer secondOrderId,Integer volume){
        Orders firstOrder = orderFeignClient.getById(firstOrderId);
        Orders secondOrder = orderFeignClient.getById(secondOrderId);
        Integer instrumentId = firstOrder.getInstrumentId();
        BigDecimal transactionPrice = instrumentFeignClient.getTransactionPrice(instrumentId,firstOrder.getLimitPrice().toString(),secondOrder.getLimitPrice().toString());
        this.deal(firstOrderId,transactionPrice,volume);
        this.deal(secondOrderId,transactionPrice,volume);
        instrumentFeignClient.updateLastPriceByIdWithCache(instrumentId,transactionPrice.toString());
        Trades trade = new Trades();
        trade.setPrice(transactionPrice);
        trade.setVolume(volume);
        Integer direction = firstOrder.getDirection();
        if(direction.equals(DirectionEnum.CALL.getCode())){
            trade.setBuyOrderId(firstOrderId);
            trade.setSellOrderId(secondOrderId);
        }else{
            trade.setBuyOrderId(secondOrderId);
            trade.setSellOrderId(firstOrderId);
        }
        tradeFeignClient.save(trade);
    }

    private void deal(Integer orderId, BigDecimal transactionPrice, int volume) {
        if(volume==0){
            return;
        }
        Orders order = orderFeignClient.getById(orderId);
        Integer userId = order.getUserId();
        User user = userFeignClient.getById(userId);
        Integer instrumentId = order.getInstrumentId();
        Instrument instrument = instrumentFeignClient.getById(instrumentId);
        Integer combOffsetFlag = order.getCombOffsetFlag();
        // 更新客户持仓
        Integer direction = order.getDirection();
        Integer type = direction;
        if(!combOffsetFlag.equals(CombOffsetEnum.OPEN.getCode())){
            type=direction^1;
        }
        Positions position = positionFeignClient.getPosition(userId, instrumentId, type);
        if(position==null){
            synchronized (PositionFeignClient.class){
                position = positionFeignClient.getPosition(userId, instrumentId, type);
                if(position==null){
                    if (combOffsetFlag.equals(CombOffsetEnum.CLOSE.getCode())) {
                        return;
                    }
                    position = new Positions();
                    position.setInstrumentId(instrumentId);
                    position.setType(type);
                    position.setUserId(userId);
                    position.setAvePrice(new BigDecimal("0.00"));
                    position.setMarginOpe(new BigDecimal("0.00"));
                    position.setQuantity(0);
                    position.setProfitLoss(new BigDecimal("0.00"));
                    position=positionFeignClient.save(position);
                    Integer positionId = position.getId();
                    redisTemplate.opsForHash().put("position_"+positionId,"avePrice",position.getAvePrice().toString());
                    redisTemplate.opsForHash().put("position_"+positionId,"marginOpe",position.getMarginOpe().toString());
                    redisTemplate.opsForHash().put("position_"+positionId,"quantity",position.getQuantity().toString());
                    redisTemplate.opsForHash().put("position_"+positionId,"profitLoss",position.getProfitLoss().toString());
                    redisTemplate.opsForHash().put("position_"+positionId,"remainVolume","0");
                }
            }
        }
        BigDecimal marginChange;
        BigDecimal tempProfitLoss = null;
        if(combOffsetFlag.equals(CombOffsetEnum.OPEN.getCode())){
            String[] result = this.open(position.getId(), transactionPrice, volume, instrument.getMultiplier(), instrument.getMinMarginRate());
            position.setQuantity(Integer.parseInt(result[0]));
            position.setAvePrice(new BigDecimal(result[1]));
            position.setMarginOpe(new BigDecimal(result[2]));
            marginChange=new BigDecimal(result[3]);
        }else{
            // 平仓操作
            String[] result = this.close(position.getId(), transactionPrice, volume, instrument.getMultiplier(), instrument.getMinMarginRate(), direction);
            position.setQuantity(Integer.parseInt(result[0]));
            position.setMarginOpe(new BigDecimal(result[1]));
            marginChange=new BigDecimal(result[2]);
            tempProfitLoss=new BigDecimal(result[3]);
            position.setProfitLoss(new BigDecimal(result[4]));
        }
        // 更新账户资金
        // 使用redis中的数据计算账户资金
        if(combOffsetFlag.equals(CombOffsetEnum.OPEN.getCode())){
            // 开仓操作账户更新
            BigDecimal limitPrice = order.getLimitPrice();
            BigDecimal multiplier = instrument.getMultiplier();
            BigDecimal marginRate = instrument.getMinMarginRate();
            // 解冻冻结保证金
            BigDecimal tempFrozenMargin=limitPrice.multiply(multiplier).multiply(marginRate)
                    .divide(new BigDecimal("100")).multiply(new BigDecimal(volume))
                    .setScale(2, RoundingMode.HALF_UP);
            this.occupyMargin(userId,marginChange,tempFrozenMargin);
        }else {
            // 平仓操作账户更新
            this.updateBalance(userId, marginChange, tempProfitLoss);
        }
        BigDecimal balance=new BigDecimal(redisTemplate.opsForHash().get("account_"+userId.toString(), "balance").toString());
        BigDecimal frozenMargin=new BigDecimal(redisTemplate.opsForHash().get("account_"+userId.toString(), "frozenMargin").toString());
        BigDecimal usedMargin=new BigDecimal(redisTemplate.opsForHash().get("account_"+userId.toString(), "usedMargin").toString());
        user.setBalance(balance);
        user.setFrozenMargin(frozenMargin);
        user.setUsedMargin(usedMargin);
        userFeignClient.updateById(user);
        positionFeignClient.updateById(position);
        // 更新订单
        orderFeignClient.updateOrderVolume(orderId,volume);
    }

    private long getScore(BigDecimal price,Integer direction){
        long time = System.currentTimeMillis();
        long l=0xffffffffL;
        long p=price.multiply(new BigDecimal("100")).intValue();
        if(direction.equals(DirectionEnum.CALL.getCode())){
            return -(p<<32)+(time&l);
        }else{
            return (p<<32)+(time&l);
        }
    }

    private Integer decrement(Integer listedOrderId, int volume) {
        String luaScript = "local currentValue = redis.call('get', KEYS[1])\n"
                + "if currentValue == false then return 0 end\n"
                + "local num = tonumber(currentValue)\n"
                + "local decrement = tonumber(ARGV[1])\n"
                + "if num - decrement >= 0 then\n"
                + "  redis.call('decrby', KEYS[1], decrement)\n"
                + "  return decrement\n"
                + "else\n"
                + "  return 0\n"
                + "end";

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript, Long.class);
        Integer result = Integer.parseInt(redisTemplate.execute(redisScript, Collections.singletonList("order_"+listedOrderId.toString()), ""+volume).toString());

        return result;
    }

    private String[] open(Integer positionId, BigDecimal transactionPrice, Integer volume, BigDecimal multiplier, BigDecimal marginRate){
        String luaScript = "local positionIdKey = KEYS[1]\n"
                + "local transactionPriceStr = ARGV[1]\n"
                + "local volumeStr = ARGV[2]\n"
                + "local multiplierStr = ARGV[3]\n"
                + "local marginRateStr = ARGV[4]\n"
                + "local multiplier = tonumber(multiplierStr)\n"
                + "local marginRate = tonumber(marginRateStr)\n"
                + "local transactionPrice = tonumber(transactionPriceStr)\n"
                + "local volume = tonumber(volumeStr)\n"
                + "local quantityStr = redis.call('HGET', positionIdKey, 'quantity')\n"
                + "local avePriceStr = redis.call('HGET', positionIdKey, 'avePrice')\n"
                + "local marginOpeStr = redis.call('HGET', positionIdKey, 'marginOpe')\n"
                + "local remainVolumeStr= redis.call('HGET', positionIdKey, 'remainVolume')\n"
                + "if quantityStr == false or avePriceStr == false or marginOpeStr == false or remainVolumeStr==false then\n"
                + "  return {}\n"
                + "end\n"
                + "local quantity = tonumber(quantityStr)\n"
                + "local avePrice = tonumber(avePriceStr)\n"
                + "local marginOpe = tonumber(marginOpeStr)\n"
                + "local remainVolume = tonumber(remainVolumeStr)\n"
                + "local newQuantity = quantity + volume\n"
                + "local newAvePrice = (avePrice * quantity + transactionPrice * volume) / newQuantity\n"
                + "local marginChange = volume * transactionPrice * marginRate * multiplier / 100.0\n"
                + "local newMarginOpe = marginOpe + marginChange\n"
                + "local newRemainVolume = remainVolume + volume\n"
                + "redis.call('HSET', positionIdKey, 'quantity', newQuantity)\n"
                + "redis.call('HSET', positionIdKey, 'avePrice', newAvePrice)\n"
                + "redis.call('HSET', positionIdKey, 'marginOpe', newMarginOpe)\n"
                + "redis.call('HSET', positionIdKey, 'remainVolume', newRemainVolume)\n"
                + "return {tostring(newQuantity), tostring(newAvePrice), tostring(newMarginOpe),tostring(marginChange)}";
        DefaultRedisScript<List> redisScript = new DefaultRedisScript<>(luaScript, List.class);
        List<String> result= (List<String>) redisTemplate.execute(redisScript, Collections.singletonList("position_" + positionId), transactionPrice.toString(), volume.toString(), multiplier.toString(), marginRate.toString());
        return result.stream().toArray(String[]::new);
    }
    private String[] close(Integer positionId, BigDecimal transactionPrice, Integer volume, BigDecimal multiplier, BigDecimal marginRate, Integer direction) {
        String luaScript = "local positionIdKey = KEYS[1]\n"
                + "local transactionPriceStr = ARGV[1]\n"
                + "local volumeStr = ARGV[2]\n"
                + "local multiplierStr = ARGV[3]\n"
                + "local marginRateStr = ARGV[4]\n"
                + "local directionStr = ARGV[5]\n"
                + "local multiplier = tonumber(multiplierStr)\n"
                + "local marginRate = tonumber(marginRateStr)\n"
                + "local transactionPrice = tonumber(transactionPriceStr)\n"
                + "local volume = tonumber(volumeStr)\n"
                + "local direction = tonumber(directionStr)\n"
                + "local quantityStr = redis.call('HGET', positionIdKey, 'quantity')\n"
                + "local avePriceStr = redis.call('HGET', positionIdKey, 'avePrice')\n"
                + "local marginOpeStr = redis.call('HGET', positionIdKey, 'marginOpe')\n"
                + "local profitLossStr = redis.call('HGET', positionIdKey, 'profitLoss')\n"
                + "if quantityStr == false or avePriceStr == false or marginOpeStr == false or profitLossStr == false then\n"
                + "  return {}\n"
                + "end\n"
                + "local quantity = tonumber(quantityStr)\n"
                + "local avePrice = tonumber(avePriceStr)\n"
                + "local marginOpe = tonumber(marginOpeStr)\n"
                + "local profitLoss = tonumber(profitLossStr)\n"
                + "local newQuantity = quantity - volume\n"
                + "local marginChange = volume * avePrice * marginRate * multiplier / 100\n"
                + "local newMarginOpe = marginOpe - marginChange\n"
                + "redis.call('HSET', positionIdKey, 'quantity', newQuantity)\n"
                + "redis.call('HSET', positionIdKey, 'marginOpe', newMarginOpe)\n"
                + "local tempProfitLoss = 0\n"
                + "if 0 == direction then\n"
                + "  tempProfitLoss = (avePrice-transactionPrice)*volume*multiplier\n"
                + "else\n"
                + "  tempProfitLoss = (transactionPrice-avePrice)*volume*multiplier\n"
                + "end\n"
                + "local newProfitLoss = profitLoss + tempProfitLoss\n"
                + "redis.call('HSET', positionIdKey, 'profitLoss', newProfitLoss)\n"
                + "return {tostring(newQuantity), tostring(newMarginOpe), tostring(marginChange), tostring(tempProfitLoss),tostring(newProfitLoss)}";
        DefaultRedisScript<List> redisScript = new DefaultRedisScript<>(luaScript, List.class);
        List<String> result= (List<String>) redisTemplate.execute(redisScript, Collections.singletonList("position_" + positionId),
                transactionPrice.toString(), volume.toString(), multiplier.toString(), marginRate.toString(),direction.toString());
        return result.toArray(new String[5]);
    }

    private boolean occupyMargin(Integer userId, BigDecimal marginChange, BigDecimal tempFrozenMargin) {
        String luaScript = "local userIdKey = KEYS[1]\n"
                + "local marginStr = ARGV[1]\n"
                + "local tempFrozenMarginStr = ARGV[2]\n"
                + "local margin = tonumber(marginStr)\n"
                + "local tempFrozenMargin = tonumber(tempFrozenMarginStr)\n"
                + "local balanceStr = redis.call('HGET', userIdKey, 'balance')\n"
                + "local frozenMarginStr = redis.call('HGET', userIdKey, 'frozenMargin')\n"
                + "local usedMarginStr = redis.call('HGET', userIdKey, 'usedMargin')\n"
                + "if balanceStr == false or frozenMarginStr == false then\n"
                + "  return 0\n"
                + "end\n"
                + "local balance = tonumber(balanceStr)\n"
                + "local frozenMargin = tonumber(frozenMarginStr)\n"
                + "local usedMargin = tonumber(usedMarginStr)\n"
                + "if margin > frozenMargin+balance then"
                + "  return 0\n"
                + "end\n"
                + "local newBalance = balance + tempFrozenMargin - margin\n"
                + "local newFrozenMargin = frozenMargin - tempFrozenMargin\n"
                + "local newUsedMargin = usedMargin + margin\n"
                + "redis.call('HSET', userIdKey, 'balance', newBalance)\n"
                + "redis.call('HSET', userIdKey, 'frozenMargin', newFrozenMargin)\n"
                + "redis.call('HSET', userIdKey, 'usedMargin', newUsedMargin)\n"
                + "return 1";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript, Long.class);
        Integer result = Integer.parseInt(redisTemplate.execute(redisScript, Collections.singletonList("account_" + userId), marginChange.toString(),tempFrozenMargin.toString()).toString());
        return result == 1;
    }

    private boolean updateBalance(Integer userId, BigDecimal marginChange, BigDecimal profitLoss) {
        String luaScript = "local userIdKey = KEYS[1]\n"
                + "local marginStr = ARGV[1]\n"
                + "local profitLossStr = ARGV[2]\n"
                + "local margin = tonumber(marginStr)\n"
                + "local profitLoss = tonumber(profitLossStr)\n"
                + "local balanceStr = redis.call('HGET', userIdKey, 'balance')\n"
                + "local usedMarginStr = redis.call('HGET', userIdKey, 'usedMargin')\n"
                + "if balanceStr == false or usedMarginStr == false then\n"
                + "  return 0\n"
                + "end\n"
                + "local balance = tonumber(balanceStr)\n"
                + "local newBalance = balance + margin + profitLoss\n"
                + "local newUsedMargin = tonumber(usedMarginStr) - margin\n"
                + "redis.call('HSET', userIdKey, 'balance', newBalance)\n"
                + "redis.call('HSET', userIdKey, 'usedMargin', newUsedMargin)\n"
                + "return 1";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript, Long.class);
        Integer result = Integer.parseInt(redisTemplate.execute(redisScript, Collections.singletonList("account_" + userId), marginChange.toString(),profitLoss.toString()).toString());
        return result == 1;
    }
}




