package com.cffex.simulatedtradingorderservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cffex.simulatedtradingmodel.common.ThreadLocalUtil;
import com.cffex.simulatedtradingmodel.constant.CommonConstant;
import com.cffex.simulatedtradingmodel.dto.orders.OrderQueryRequest;
import com.cffex.simulatedtradingmodel.entity.Instrument;
import com.cffex.simulatedtradingmodel.entity.Orders;
import com.cffex.simulatedtradingmodel.entity.Positions;
import com.cffex.simulatedtradingmodel.entity.User;
import com.cffex.simulatedtradingmodel.enums.CombOffsetEnum;
import com.cffex.simulatedtradingmodel.enums.DirectionEnum;
import com.cffex.simulatedtradingmodel.enums.InstrumentStateEnum;
import com.cffex.simulatedtradingmodel.enums.OrderStatusEnum;
import com.cffex.simulatedtradingmodel.utils.SqlUtils;
import com.cffex.simulatedtradingmodel.vo.OrderVO;
import com.cffex.simulatedtradingorderservice.mapper.OrdersMapper;
import com.cffex.simulatedtradingorderservice.service.OrdersService;
import com.cffex.simulatedtradingserviceclient.InstrumentFeignClient;
import com.cffex.simulatedtradingserviceclient.PositionFeignClient;
import com.cffex.simulatedtradingserviceclient.UserFeignClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
* @author 17204
* @description 针对表【orders(订单信息表)】的数据库操作Service实现
* @createDate 2024-07-10 10:15:50
*/
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders>
    implements OrdersService{
    @Resource
    private UserFeignClient userFeignClient;
    @Resource
    private InstrumentFeignClient instrumentFeignClient;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private PositionFeignClient positionFeignClient;
    @Resource
    private OrdersMapper ordersMapper;

    /**
     * 验证订单信息是否合法
     *
     * @param orders 订单信息
     * @return 如果订单信息合法则返回true，否则返回false
     */
    @Override
    public Integer validate(Orders orders) {
        Integer instrumentId = orders.getInstrumentId();
        Instrument instrument= instrumentFeignClient.getByIdWithCache(instrumentId);
        // 验证合约是否存在且状态为上市
        if (instrument == null||!instrument.getState().equals(InstrumentStateEnum.LISTED.getCode())) {
            return -1;
        }

        // 验证用户是否存在
        if(!redisTemplate.hasKey("account_"+orders.getUserId())) {
            return -1;
        }
        User user = new User();
        user.setId(orders.getUserId());
        user.setBalance(new BigDecimal(redisTemplate.opsForHash().get("account_"+orders.getUserId(),"balance").toString()));
        user.setFrozenMargin(new BigDecimal(redisTemplate.opsForHash().get("account_"+orders.getUserId(),"frozenMargin").toString()));
        user.setUsedMargin(new BigDecimal(redisTemplate.opsForHash().get("account_"+orders.getUserId(),"usedMargin").toString()));

        // 价格合法性检查
        BigDecimal minPriceChange = instrument.getMinPriceChange();
        BigDecimal limitPrice = orders.getLimitPrice();
        try {
            limitPrice.divide(minPriceChange, 0, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            return -1;
        }

        // 价格范围检查
        BigDecimal settlementPrice = instrument.getSettlementPrice();
        BigDecimal maxDailyPriceFluctuation = instrument.getMaxDailyPriceFluctuation();
        BigDecimal maxPrice=settlementPrice.multiply(maxDailyPriceFluctuation.divide(new BigDecimal("100"),2,RoundingMode.HALF_UP).add(BigDecimal.ONE));
        maxPrice = maxPrice.setScale(2, RoundingMode.HALF_UP);
        BigDecimal minPrice=settlementPrice.multiply(BigDecimal.ONE.subtract(maxDailyPriceFluctuation.divide(new BigDecimal("100"),2,RoundingMode.HALF_UP)));
        minPrice = minPrice.setScale(2, RoundingMode.HALF_UP);
        if(limitPrice.compareTo(maxPrice) > 0 || limitPrice.compareTo(minPrice) < 0){
            return -1;
        }

        if(orders.getCombOffsetFlag().equals(CombOffsetEnum.OPEN.getCode())){
            // 开仓订单冻结保证金
            BigDecimal multiplier = instrument.getMultiplier();
            BigDecimal marginRate = instrument.getMinMarginRate();
            Integer volume = orders.getVolumeTotal();
            BigDecimal margin = limitPrice.multiply(multiplier).multiply(marginRate).divide(new BigDecimal("100")).multiply(new BigDecimal(volume)).setScale(2, RoundingMode.HALF_UP);
            boolean validBalance = this.frozenMargin(user.getId(), margin);
            if(!validBalance) return -1;
            user.setBalance(new BigDecimal(redisTemplate.opsForHash().get("account_"+user.getId(),"balance").toString()));
            user.setFrozenMargin(new BigDecimal(redisTemplate.opsForHash().get("account_"+user.getId(),"frozenMargin").toString()));
            userFeignClient.updateById(user);
            return 0;
        }else{
            // TODO 用redis优化
            // 判断(已存在平仓订单的手数+平仓订单手数)是否小于持仓手数
            Positions position= positionFeignClient.getPosition(orders.getUserId(),instrumentId,orders.getDirection()^1);
            if(position==null) return -1;
            Integer positionId=position.getId();
            boolean result = this.validClose(positionId, orders.getVolumeTotal());
            if(!result) return -1;
            return position.getId();
        }
    }
    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @return 是否成功取消订单，成功返回true，否则返回false
     */
    @Override
    public Boolean cancelOrder(Integer orderId) {
        Integer userId = ThreadLocalUtil.getUserId();
        Orders orders = this.getById(orderId);
        Integer positionId = orders.getPositionId();
        if(!userId.equals(orders.getUserId())){
            return false;
        }
        // TODO 使用lua脚本改写 从交易列表中删除订单
        // TODO 更改redis中的position.remainVolume
        String key = orders.getInstrumentId().toString() + "_" + (orders.getDirection());
        redisTemplate.opsForZSet().remove(key,orderId.toString());
        Integer newVolumeTotal=Integer.parseInt(redisTemplate.opsForValue().get("order_"+orderId).toString());
        redisTemplate.opsForHash().increment("position_"+positionId, "remainVolume", newVolumeTotal);
        orders.setVolumeTraded(orders.getVolumeTraded()+orders.getVolumeTotal()-newVolumeTotal);
        orders.setVolumeTotal(newVolumeTotal);
        redisTemplate.delete("order_"+orderId);
        Integer orderStatus = orders.getOrderStatus();
        if(orderStatus.equals(OrderStatusEnum.CANCEL.getCode())||orderStatus.equals(OrderStatusEnum.FULLY_FILLED.getCode())||newVolumeTotal==0){
            orders.setOrderStatus(OrderStatusEnum.FULLY_FILLED.getCode());
            orders.setVolumeTotal(newVolumeTotal);
            this.updateById(orders);
            return false;
        }
        orders.setOrderStatus(OrderStatusEnum.CANCEL.getCode());
        orders.setCancelTime(new Date());
        this.updateById(orders);
        this.removeById(orderId);
        if(orders.getCombOffsetFlag().equals(CombOffsetEnum.OPEN.getCode())){
            // 更新用户余额和保证金
            User user = userFeignClient.getById(userId);
            Integer volume = orders.getVolumeTotal();
            BigDecimal limitPrice = orders.getLimitPrice();
            Instrument instrument = instrumentFeignClient.getById(orders.getInstrumentId());
            BigDecimal multiplier = instrument.getMultiplier();
            BigDecimal marginRate = instrument.getMinMarginRate();
            BigDecimal margin=limitPrice.multiply(multiplier).multiply(marginRate).divide(new BigDecimal("100")).multiply(new BigDecimal(volume)).setScale(2, RoundingMode.HALF_UP);
            // redis中账户解冻保证金
            Boolean result = this.unfrozenMargin(user.getId(), margin);
            if(!result){
                return false;
            }
            user.setBalance(new BigDecimal(redisTemplate.opsForHash().get("account_"+user.getId(),"balance").toString()));
            user.setFrozenMargin(new BigDecimal(redisTemplate.opsForHash().get("account_"+user.getId(),"frozenMargin").toString()));
            userFeignClient.updateById(user);
        }
        return true;
    }

    @Override
    public void updateOrderVolume(Integer orderId, int volume) {
        ordersMapper.updateOrderStatusAndVolumes(orderId,volume);
        ordersMapper.updateOrderToCompleteIfZero(orderId);
    }



    private boolean frozenMargin(Integer userId, BigDecimal marginChange) {

        String luaScript = "local userIdKey = KEYS[1]\n"
                + "local marginStr = ARGV[1]\n"
                + "local margin = tonumber(marginStr)\n"
                + "local balanceStr = redis.call('HGET', userIdKey, 'balance')\n"
                + "local frozenMarginStr = redis.call('HGET', userIdKey, 'frozenMargin')\n"
                + "if balanceStr == false or frozenMarginStr == false then\n"
                + "  return 0\n"
                + "end\n"
                + "local balance = tonumber(balanceStr)\n"
                + "local frozenMargin = tonumber(frozenMarginStr)\n"
                + "if margin > balance then"
                + "  return 0\n"
                + "end\n"
                + "local newBalance = balance - margin\n"
                + "local newFrozenMargin = frozenMargin + margin\n"
                + "redis.call('HSET', userIdKey, 'balance', newBalance)\n"
                + "redis.call('HSET', userIdKey, 'frozenMargin', newFrozenMargin)\n"
                + "return 1";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript, Long.class);
        Integer result = Integer.parseInt(redisTemplate.execute(redisScript, Collections.singletonList("account_"+userId), marginChange.toString()).toString());

        return result==1;
    }

    private boolean unfrozenMargin(Integer userId, BigDecimal marginChange) {
        String luaScript = "local userIdKey = KEYS[1]\n"
                + "local marginStr = ARGV[1]\n"
                + "local margin = tonumber(marginStr)\n"
                + "local balanceStr = redis.call('HGET', userIdKey, 'balance')\n"
                + "local frozenMarginStr = redis.call('HGET', userIdKey, 'frozenMargin')\n"
                + "if balanceStr == false or frozenMarginStr == false then\n"
                + "  return 0\n"
                + "end\n"
                + "local balance = tonumber(balanceStr)\n"
                + "local frozenMargin = tonumber(frozenMarginStr)\n"
                + "if margin > frozenMargin then"
                + "  margin = frozenMargin\n"
                + "end\n"
                + "local newBalance = balance + margin\n"
                + "local newFrozenMargin = frozenMargin - margin\n"
                + "redis.call('HSET', userIdKey, 'balance', newBalance)\n"
                + "redis.call('HSET', userIdKey, 'frozenMargin', newFrozenMargin)\n"
                + "return 1";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript, Long.class);
        Integer result = Integer.parseInt(redisTemplate.execute(redisScript, Collections.singletonList("account_"+userId), marginChange.toString()).toString());
        return result==1;
    }




    @Override
    public boolean validClose(Integer positionId,Integer volume){
        String luaScript = "local positionIdKey = KEYS[1]\n"
                + "local volumeStr = ARGV[1]\n"
                + "local volume = tonumber(volumeStr)\n"
                + "local remainVolumeStr= redis.call('HGET', positionIdKey, 'remainVolume')\n"
                + "if remainVolumeStr == false then\n"
                + "  return 0\n"
                + "end\n"
                + "local remainVolume = tonumber(remainVolumeStr)\n"
                + "if remainVolume < volume then\n"
                + "  return 0\n"
                + "end\n"
                + "local newRemainVolume = remainVolume - volume\n"
                + "redis.call('HSET', positionIdKey, 'remainVolume', newRemainVolume)\n"
                + "return 1";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript, Long.class);
        Long result= Long.parseLong(redisTemplate.execute(redisScript, Collections.singletonList("position_" + positionId), volume.toString()).toString());
        return result==1;
    }

    @Override
    public QueryWrapper<Orders> getQueryWrapper(OrderQueryRequest orderQueryRequest, Integer userId) {
        QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
        if (orderQueryRequest == null|| userId==null) {
            return queryWrapper;
        }
        String sortField = orderQueryRequest.getSortField();
        String sortOrder = orderQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.and(wrapper -> wrapper.eq("orderStatus", OrderStatusEnum.UNFILLED.getCode())
                .or().eq("orderStatus", OrderStatusEnum.PARTIALLY_FILLED.getCode()));
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public Page<OrderVO> getOrderVOPage(Page<Orders> orderPage) {
        List<Orders> orderList = orderPage.getRecords();
        Page<OrderVO> orderVOPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        if (orderList.isEmpty()) {
            return orderVOPage;
        }
        Map<Integer,String> instrumentMap = new HashMap<>();
        List<OrderVO> orderVOList = new ArrayList<>();
        for(Orders order:orderList){
            instrumentMap.put(order.getInstrumentId(),null);
        }
        for(Integer instrumentId: instrumentMap.keySet()){
            Instrument instrument = instrumentFeignClient.getById(instrumentId);
            if (instrument != null) {
                instrumentMap.put(instrumentId, instrument.getName());
            }
        }
        for(Orders order:orderList){
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(order, orderVO);
            if(order.getDirection().equals(DirectionEnum.CALL.getCode())){
                orderVO.setDirectionStr(DirectionEnum.CALL.getMessage());
            }else{
                orderVO.setDirectionStr(DirectionEnum.PUT.getMessage());
            }
            if(order.getCombOffsetFlag().equals(CombOffsetEnum.OPEN.getCode())){
                orderVO.setCombOffsetStr(CombOffsetEnum.OPEN.getMessage());
            }else{
                orderVO.setCombOffsetStr(CombOffsetEnum.CLOSE.getMessage());
            }
            orderVO.setInstrumentName(instrumentMap.get(order.getInstrumentId()));
            orderVOList.add(orderVO);
        }
        orderVOPage.setRecords(orderVOList);
        return orderVOPage;
    }

}
