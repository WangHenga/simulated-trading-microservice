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
 * 成交记录表
 * @TableName trades
 */
@TableName(value ="trades")
@Data
public class Trades implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 卖单id
     */
    private Integer sellOrderId;

    /**
     * 买单id
     */
    private Integer buyOrderId;

    /**
     * 成交价
     */
    private BigDecimal price;

    /**
     * 成交量
     */
    private Integer volume;

    /**
     * 创建时间
     */
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}