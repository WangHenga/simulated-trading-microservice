package com.cffex.simulatedtradingmodel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 合约信息表
 * @TableName instrument
 */
@TableName(value ="instrument")
@Data
public class Instrument implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 合约名称
     */
    private String name;

    /**
     * 合约代码
     */
    private String symbol;

    /**
     * 合约标的
     */
    private String subject;

    /**
     * 合约报价单位
     */
    private String quoteUnit;

    /**
     * 最小变动价位
     */
    private BigDecimal minPriceChange;

    /**
     * 最大变动幅度
     */
    private BigDecimal maxDailyPriceFluctuation;

    /**
     * 最小保证金率
     */
    private BigDecimal minMarginRate;

    /**
     * 合约乘数
     */
    private BigDecimal multiplier;

    /**
     * 合约状态
     */
    private Integer state;

    /**
     * 最新成交价
     */
    private BigDecimal lastPrice;

    /**
     * 结算价
     */
    private BigDecimal settlementPrice;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}