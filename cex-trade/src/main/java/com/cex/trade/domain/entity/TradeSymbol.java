package com.cex.trade.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cex.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 交易对实体
 * 
 * @author cex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trade_symbol")
public class TradeSymbol extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 交易对名称 */
    private String symbol;

    /** 基础币种 */
    private String baseCoin;

    /** 计价币种 */
    private String quoteCoin;

    /** 交易对状态（0正常 1停用） */
    private Integer status;

    /** 最小交易数量 */
    private BigDecimal minTradeAmount;

    /** 最大交易数量 */
    private BigDecimal maxTradeAmount;

    /** 价格精度 */
    private Integer pricePrecision;

    /** 数量精度 */
    private Integer amountPrecision;

    /** 手续费率 */
    private BigDecimal feeRate;

    /** 排序 */
    private Integer sort;

    /** 是否热门（0否 1是） */
    private Integer isHot;

    /** 是否推荐（0否 1是） */
    private Integer isRecommend;

    /** 交易对类型（0现货 1期货） */
    private Integer symbolType;

    /** 是否启用市价买（0否 1是） */
    private Integer enableMarketBuy;

    /** 是否启用市价卖（0否 1是） */
    private Integer enableMarketSell;

    /** 最小成交额 */
    private BigDecimal minTurnover;

    /** 最小卖出价格 */
    private BigDecimal minSellPrice;

    /** 最高买入价格 */
    private BigDecimal maxBuyPrice;

    /** 最大同时交易订单数（0不限制） */
    private Integer maxTradingOrder;

    /** 委托超时时间（秒，0不过期） */
    private Integer maxTradingTime;

    /** 是否可见（0不可见 1可见） */
    private Integer visible;

    /** 是否可交易（0不可交易 1可交易） */
    private Integer tradeable;

    /** 交易区域（0默认 1创新区 2主板区） */
    private Integer zone;

    /** 备注 */
    private String remark;
}
