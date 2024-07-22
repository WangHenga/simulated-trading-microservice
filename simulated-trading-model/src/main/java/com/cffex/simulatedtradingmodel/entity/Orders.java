package com.cffex.simulatedtradingmodel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单信息表
 * @TableName orders
 */
@TableName(value ="orders")
@Data
public class Orders implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 合约id
     */
    private Integer instrumentId;

    /**
     * 买卖方向
     */
    private Integer direction;

    /**
     * 组合开平标志
     */
    private Integer combOffsetFlag;

    /**
     * 持仓id（平仓需要）
     */
    private Integer positionId;
    /**
     * 报价
     */
    private BigDecimal limitPrice;

    /**
     * 订单状态
     */
    private Integer orderStatus;

    /**
     * 成交数量
     */
    private Integer volumeTraded;

    /**
     * 委托数量
     */
    private Integer volumeTotal;

    /**y
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 撤销时间
     */
    private Date cancelTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}