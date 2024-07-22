package com.cffex.simulatedtradingmodel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 客户持仓表
 * @TableName positions
 */
@TableName(value ="positions")
@Data
public class Positions implements Serializable {
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
     * 持仓类型
     */
    private Integer type;

    /**
     * 持仓数量
     */
    private Integer quantity;

    /**
     * 持仓平均成本
     */
    private BigDecimal avePrice;

    /**
     * 持仓市场价
     */
    private BigDecimal marketPrice;

    /**
     * 持仓盈亏
     */
    private BigDecimal profitLoss;

    /**
     * 持仓保证金占用
     */
    private BigDecimal marginOpe;

    /**
     * 持仓风险度
     */
    private BigDecimal riskRatio;

    /**
     * 开仓时间
     */
    private Date openTime;

    /**
     * 最后更新时间
     */
    private Date lastUpdateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}